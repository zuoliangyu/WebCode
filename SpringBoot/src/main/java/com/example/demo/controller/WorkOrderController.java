package com.example.demo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.commom.Result;
import com.example.demo.entity.User;
import com.example.demo.entity.WorkOrder;
import com.example.demo.service.WorkOrderService;
import com.example.demo.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/workorder")
public class WorkOrderController {

    @Resource
    private WorkOrderService workOrderService;

    /**
     * 申请人创建工单
     */
    @PostMapping("/create")
    public Result<?> createWorkOrder(@RequestBody WorkOrder workOrder) {
        try {
            // 获取当前登录用户
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

    /**
     * 申请人查询自己的工单
     */
    @GetMapping("/myorders")
    public Result<?> getMyWorkOrders(@RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            User currentUser = TokenUtils.getUser();
            if (currentUser == null) {
                return Result.error("-1", "请先登录");
            }

            Page<WorkOrder> orders = workOrderService.getWorkOrdersByApplicant(
                    currentUser.getId(), pageNum, pageSize);
            return Result.success(orders);
        } catch (Exception e) {
            return Result.error("-1", "查询工单失败: " + e.getMessage());
        }
    }

    /**
     * 管理员查询所有工单
     */
    @GetMapping("/admin/all")
    public Result<?> getAllWorkOrders(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                      @RequestParam(required = false) String status,
                                      @RequestParam(required = false) String applicantName) {
        try {
            // 权限验证
            User currentUser = TokenUtils.getUser();
            if (currentUser == null || currentUser.getRole() != 1) {
                return Result.error("-1", "权限不足");
            }

            Page<WorkOrder> orders = workOrderService.getAllWorkOrders(pageNum, pageSize, status, applicantName);
            return Result.success(orders);
        } catch (Exception e) {
            return Result.error("-1", "查询工单失败: " + e.getMessage());
        }
    }

    /**
     * 管理员审批工单 - 同意配送
     */
    @PostMapping("/admin/approve")
    public Result<?> approveWorkOrder(@RequestBody Map<String, Object> params) {
        try {
            // 权限验证
            User currentUser = TokenUtils.getUser();
            if (currentUser == null || currentUser.getRole() != 1) {
                return Result.error("-1", "权限不足");
            }

            Integer orderId = (Integer) params.get("orderId");
            Integer actualQuantity = (Integer) params.get("actualQuantity");
            String deliveryPerson = (String) params.get("deliveryPerson");

            WorkOrder workOrder = new WorkOrder();
            workOrder.setId(orderId);

            boolean success = workOrderService.approveAndDeliver(workOrder, actualQuantity, deliveryPerson, currentUser);

            if (success) {
                return Result.success("工单审批成功");
            } else {
                return Result.error("-1", "工单审批失败");
            }
        } catch (Exception e) {
            return Result.error("-1", "审批工单失败: " + e.getMessage());
        }
    }

    /**
     * 管理员拒绝工单
     */
    @PostMapping("/admin/reject")
    public Result<?> rejectWorkOrder(@RequestBody Map<String, Object> params) {
        try {
            // 权限验证
            User currentUser = TokenUtils.getUser();
            if (currentUser == null || currentUser.getRole() != 1) {
                return Result.error("-1", "权限不足");
            }

            Integer orderId = (Integer) params.get("orderId");
            String rejectReason = (String) params.get("rejectReason");

            boolean success = workOrderService.rejectWorkOrder(orderId, rejectReason, currentUser);

            if (success) {
                return Result.success("工单拒绝成功");
            } else {
                return Result.error("-1", "工单拒绝失败");
            }
        } catch (Exception e) {
            return Result.error("-1", "拒绝工单失败: " + e.getMessage());
        }
    }

    /**
     * 根据工单号查询工单详情
     */
    @GetMapping("/detail/{orderNumber}")
    public Result<?> getWorkOrderDetail(@PathVariable String orderNumber) {
        try {
            WorkOrder order = workOrderService.getWorkOrderByNumber(orderNumber);
            if (order == null) {
                return Result.error("-1", "工单不存在");
            }
            return Result.success(order);
        } catch (Exception e) {
            return Result.error("-1", "查询工单详情失败: " + e.getMessage());
        }
    }
}
