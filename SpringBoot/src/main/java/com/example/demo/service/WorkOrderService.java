package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.entity.Material;
import com.example.demo.entity.User;
import com.example.demo.entity.WorkOrder;
import com.example.demo.mapper.MaterialMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.mapper.WorkOrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class WorkOrderService {

    @Resource
    private WorkOrderMapper workOrderMapper;

    @Resource
    private MaterialMapper materialMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private OperationLogService operationLogService;

    /**
     * 创建出库工单
     */
    @Transactional
    public WorkOrder createWorkOrder(WorkOrder workOrder, User currentUser) {
        String orderNumber = generateOrderNumber();
        workOrder.setOrderNumber(orderNumber);
        workOrder.setOrderType("出库");
        workOrder.setStatus("待审批");
        workOrder.setCreateTime(new Date());
        workOrder.setUpdateTime(new Date());
        workOrder.setApplicantId(currentUser.getId());
        workOrder.setApplicantName(currentUser.getNickName());
        workOrder.setEmployeeId(currentUser.getEmployeeId());

        // 获取物料信息，快照单套用量
        Material material = materialMapper.selectById(workOrder.getProductId());
        if (material != null) {
            workOrder.setProductName(material.getName());
            workOrder.setPerSetQuantity(material.getPerSetQuantity());
            // 自动计算数量 = 套数 x 单套用量
            if (workOrder.getNumberOfSets() != null && material.getPerSetQuantity() != null) {
                workOrder.setQuantity(workOrder.getNumberOfSets() * material.getPerSetQuantity());
            }
        }

        workOrderMapper.insert(workOrder);

        operationLogService.recordOperation("创建出库工单",
                "用户" + currentUser.getNickName() + "创建了出库工单：" + orderNumber,
                currentUser, orderNumber);

        return workOrder;
    }

    /**
     * 创建退库工单
     */
    @Transactional
    public WorkOrder createReturnOrder(WorkOrder workOrder, User currentUser) {
        // 校验关联的原出库工单
        if (workOrder.getRelatedOrderNumber() == null || workOrder.getRelatedOrderNumber().isEmpty()) {
            throw new RuntimeException("退库工单必须关联原出库工单号");
        }

        // 查找原出库工单
        LambdaQueryWrapper<WorkOrder> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(WorkOrder::getOrderNumber, workOrder.getRelatedOrderNumber());
        WorkOrder originalOrder = workOrderMapper.selectOne(wrapper);
        if (originalOrder == null) {
            throw new RuntimeException("关联的原出库工单不存在");
        }
        if (!"已通过".equals(originalOrder.getStatus())) {
            throw new RuntimeException("关联的原出库工单状态不是已通过");
        }

        // 校验退库数量
        if (workOrder.getQuantity() == null || workOrder.getQuantity() <= 0) {
            throw new RuntimeException("退库数量必须大于0");
        }
        int originalQty = originalOrder.getActualDeliveryQuantity() != null ?
                originalOrder.getActualDeliveryQuantity() : originalOrder.getQuantity();
        if (workOrder.getQuantity() > originalQty) {
            throw new RuntimeException("退库数量不能超过原出库数量(" + originalQty + ")");
        }

        // 校验退库去向
        if (workOrder.getReturnDestination() == null || workOrder.getReturnDestination().isEmpty()) {
            throw new RuntimeException("退库去向不能为空");
        }

        String orderNumber = generateOrderNumber();
        workOrder.setOrderNumber(orderNumber);
        workOrder.setOrderType("退库");
        workOrder.setStatus("待审批");
        workOrder.setCreateTime(new Date());
        workOrder.setUpdateTime(new Date());
        workOrder.setApplicantId(currentUser.getId());
        workOrder.setApplicantName(currentUser.getNickName());
        workOrder.setEmployeeId(currentUser.getEmployeeId());
        workOrder.setProductId(originalOrder.getProductId());
        workOrder.setProductName(originalOrder.getProductName());
        workOrder.setDrawingNumber(originalOrder.getDrawingNumber());

        workOrderMapper.insert(workOrder);

        operationLogService.recordOperation("创建退库工单",
                "用户" + currentUser.getNickName() + "创建了退库工单：" + orderNumber,
                currentUser, orderNumber);

        return workOrder;
    }

    private String generateOrderNumber() {
        String dateStr = java.time.LocalDate.now().toString().replace("-", "");
        String prefix = "WO" + dateStr;

        LambdaQueryWrapper<WorkOrder> wrapper = Wrappers.lambdaQuery();
        wrapper.likeRight(WorkOrder::getOrderNumber, prefix);
        wrapper.orderByDesc(WorkOrder::getOrderNumber);
        wrapper.last("LIMIT 1");

        WorkOrder lastOrder = workOrderMapper.selectOne(wrapper);
        int sequence = 1;
        if (lastOrder != null) {
            String lastNumber = lastOrder.getOrderNumber();
            String sequenceStr = lastNumber.substring(prefix.length());
            sequence = Integer.parseInt(sequenceStr) + 1;
        }

        return prefix + String.format("%06d", sequence);
    }

    /**
     * 审批通过：出库扣库存，退库加库存
     */
    @Transactional
    public boolean approveAndDeliver(Integer orderId, Integer actualQuantity, User adminUser) {
        WorkOrder existingOrder = workOrderMapper.selectById(orderId);
        if (existingOrder == null || !"待审批".equals(existingOrder.getStatus())) {
            return false;
        }

        int qty = actualQuantity != null ? actualQuantity : existingOrder.getQuantity();

        existingOrder.setStatus("已通过");
        existingOrder.setActualDeliveryQuantity(qty);
        existingOrder.setApproverId(adminUser.getId());
        existingOrder.setApproverName(adminUser.getNickName());
        existingOrder.setCompleteTime(new Date());
        existingOrder.setUpdateTime(new Date());

        workOrderMapper.updateById(existingOrder);

        // 更新库存
        Material material = materialMapper.selectById(existingOrder.getProductId());
        if (material != null) {
            if ("出库".equals(existingOrder.getOrderType())) {
                // 出库减库存
                int newStock = material.getTotalQuantity() - qty;
                material.setTotalQuantity(Math.max(newStock, 0));
            } else if ("退库".equals(existingOrder.getOrderType())) {
                // 退库加库存
                material.setTotalQuantity(material.getTotalQuantity() + qty);
            }
            material.setUpdateTime(new Date());
            materialMapper.updateById(material);
        }

        operationLogService.recordOperation("审批工单",
                adminUser.getNickName() + "通过了工单：" + existingOrder.getOrderNumber() +
                        "，实际数量：" + qty,
                adminUser, existingOrder.getOrderNumber());

        return true;
    }

    @Transactional
    public boolean rejectWorkOrder(Integer orderId, String rejectReason, User adminUser) {
        WorkOrder existingOrder = workOrderMapper.selectById(orderId);
        if (existingOrder == null || !"待审批".equals(existingOrder.getStatus())) {
            return false;
        }

        existingOrder.setStatus("已拒绝");
        existingOrder.setRejectReason(rejectReason);
        existingOrder.setApproverId(adminUser.getId());
        existingOrder.setApproverName(adminUser.getNickName());
        existingOrder.setUpdateTime(new Date());

        workOrderMapper.updateById(existingOrder);

        operationLogService.recordOperation("拒绝工单",
                adminUser.getNickName() + "拒绝了工单：" + existingOrder.getOrderNumber() +
                        "，原因：" + rejectReason,
                adminUser, existingOrder.getOrderNumber());

        return true;
    }

    public Page<WorkOrder> getWorkOrdersByApplicant(Integer applicantId, Integer pageNum, Integer pageSize,
                                                     String status, String orderType) {
        LambdaQueryWrapper<WorkOrder> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(WorkOrder::getApplicantId, applicantId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(WorkOrder::getStatus, status);
        }
        if (orderType != null && !orderType.isEmpty()) {
            wrapper.eq(WorkOrder::getOrderType, orderType);
        }
        wrapper.orderByDesc(WorkOrder::getCreateTime);
        return workOrderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public Page<WorkOrder> getAllWorkOrders(Integer pageNum, Integer pageSize, String status,
                                            String orderType, String applicantName) {
        LambdaQueryWrapper<WorkOrder> wrapper = Wrappers.lambdaQuery();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(WorkOrder::getStatus, status);
        }
        if (orderType != null && !orderType.isEmpty()) {
            wrapper.eq(WorkOrder::getOrderType, orderType);
        }
        if (applicantName != null && !applicantName.isEmpty()) {
            wrapper.like(WorkOrder::getApplicantName, applicantName);
        }
        wrapper.orderByDesc(WorkOrder::getCreateTime);
        return workOrderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }
}
