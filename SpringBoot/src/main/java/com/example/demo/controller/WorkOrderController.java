package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.commom.Result;
import com.example.demo.entity.User;
import com.example.demo.entity.WorkOrder;
import com.example.demo.service.WorkOrderService;
import com.example.demo.utils.TokenUtils;
import com.example.demo.mapper.WorkOrderMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/workorder")
public class WorkOrderController {

    @Resource
    private WorkOrderService workOrderService;

    @Resource
    private WorkOrderMapper workOrderMapper;

    // 员工创建出库申请
    @PostMapping("/create")
    public Result<?> createWorkOrder(@RequestBody WorkOrder workOrder) {
        try {
            User currentUser = TokenUtils.getUser();
            if (currentUser == null) {
                return Result.error("-1", "请先登录");
            }
            WorkOrder createdOrder = workOrderService.createWorkOrder(workOrder, currentUser);
            return Result.success(createdOrder);
        } catch (Exception e) {
            return Result.error("-1", "创建工单失败: " + e.getMessage());
        }
    }

    // 员工创建退库申请
    @PostMapping("/return")
    public Result<?> createReturnOrder(@RequestBody WorkOrder workOrder) {
        try {
            User currentUser = TokenUtils.getUser();
            if (currentUser == null) {
                return Result.error("-1", "请先登录");
            }
            WorkOrder createdOrder = workOrderService.createReturnOrder(workOrder, currentUser);
            return Result.success(createdOrder);
        } catch (Exception e) {
            return Result.error("-1", "创建退库工单失败: " + e.getMessage());
        }
    }

    // 员工查看自己的工单
    @GetMapping("/myorders")
    public Result<?> getMyWorkOrders(@RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize,
                                     @RequestParam(required = false) String status,
                                     @RequestParam(required = false) String orderType) {
        try {
            User currentUser = TokenUtils.getUser();
            if (currentUser == null) {
                return Result.error("-1", "请先登录");
            }
            Page<WorkOrder> orders = workOrderService.getWorkOrdersByApplicant(
                    currentUser.getId(), pageNum, pageSize, status, orderType);
            return Result.success(orders);
        } catch (Exception e) {
            return Result.error("-1", "查询工单失败: " + e.getMessage());
        }
    }

    // 管理员/仓管查看所有工单（待审批）
    @GetMapping("/admin/all")
    public Result<?> getAllWorkOrders(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                      @RequestParam(required = false) String status,
                                      @RequestParam(required = false) String orderType,
                                      @RequestParam(required = false) String applicantName) {
        try {
            User currentUser = TokenUtils.getUser();
            if (currentUser == null || currentUser.getRole() > 2) {
                return Result.error("-1", "权限不足");
            }
            Page<WorkOrder> orders = workOrderService.getAllWorkOrders(pageNum, pageSize, status, orderType, applicantName);
            return Result.success(orders);
        } catch (Exception e) {
            return Result.error("-1", "查询工单失败: " + e.getMessage());
        }
    }

    // 管理员/仓管审批通过
    @PostMapping("/admin/approve")
    public Result<?> approveWorkOrder(@RequestBody Map<String, Object> params) {
        try {
            User currentUser = TokenUtils.getUser();
            if (currentUser == null || currentUser.getRole() > 2) {
                return Result.error("-1", "权限不足");
            }
            Integer orderId = (Integer) params.get("orderId");
            Integer actualQuantity = params.get("actualQuantity") != null ? (Integer) params.get("actualQuantity") : null;

            boolean success = workOrderService.approveAndDeliver(orderId, actualQuantity, currentUser);
            if (success) {
                return Result.success("审批通过");
            } else {
                return Result.error("-1", "审批失败");
            }
        } catch (Exception e) {
            return Result.error("-1", "审批失败: " + e.getMessage());
        }
    }

    // 管理员/仓管拒绝工单
    @PostMapping("/admin/reject")
    public Result<?> rejectWorkOrder(@RequestBody Map<String, Object> params) {
        try {
            User currentUser = TokenUtils.getUser();
            if (currentUser == null || currentUser.getRole() > 2) {
                return Result.error("-1", "权限不足");
            }
            Integer orderId = (Integer) params.get("orderId");
            String rejectReason = (String) params.get("rejectReason");

            boolean success = workOrderService.rejectWorkOrder(orderId, rejectReason, currentUser);
            if (success) {
                return Result.success("已拒绝");
            } else {
                return Result.error("-1", "拒绝失败");
            }
        } catch (Exception e) {
            return Result.error("-1", "拒绝失败: " + e.getMessage());
        }
    }

    // 工单统计（供 Dashboard）
    @GetMapping("/statistics")
    public Result<?> getStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 待审批工单数
            LambdaQueryWrapper<WorkOrder> pendingWrapper = Wrappers.lambdaQuery();
            pendingWrapper.eq(WorkOrder::getStatus, "待审批");
            stats.put("pendingCount", workOrderMapper.selectCount(pendingWrapper));

            // 总工单数
            stats.put("totalCount", workOrderMapper.selectCount(null));

            // 今日工单数
            LambdaQueryWrapper<WorkOrder> todayWrapper = Wrappers.lambdaQuery();
            todayWrapper.apply("DATE(create_time) = CURDATE()");
            stats.put("todayCount", workOrderMapper.selectCount(todayWrapper));

            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("-1", "获取统计失败: " + e.getMessage());
        }
    }

    // 获取员工已通过的出库工单（用于退库选择）
    @GetMapping("/approved-outbound")
    public Result<?> getApprovedOutbound() {
        try {
            User currentUser = TokenUtils.getUser();
            if (currentUser == null) {
                return Result.error("-1", "请先登录");
            }
            LambdaQueryWrapper<WorkOrder> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(WorkOrder::getApplicantId, currentUser.getId());
            wrapper.eq(WorkOrder::getOrderType, "出库");
            wrapper.eq(WorkOrder::getStatus, "已通过");
            wrapper.orderByDesc(WorkOrder::getCreateTime);
            return Result.success(workOrderMapper.selectList(wrapper));
        } catch (Exception e) {
            return Result.error("-1", "查询失败: " + e.getMessage());
        }
    }
}
