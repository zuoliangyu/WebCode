package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.entity.ProductBase;
import com.example.demo.entity.User;
import com.example.demo.entity.WorkOrder;
import com.example.demo.mapper.ProductBaseMapper;
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
    private ProductBaseMapper productBaseMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private OperationLogService operationLogService;

    /**
     * 创建工单
     */
    @Transactional
    public WorkOrder createWorkOrder(WorkOrder workOrder, User currentUser) {
        // 生成唯一工单号
        String orderNumber = generateOrderNumber();
        workOrder.setOrderNumber(orderNumber);
        workOrder.setStatus("待处理");
        workOrder.setCreateTime(new Date());
        workOrder.setUpdateTime(new Date());

        // 设置申请人信息
        workOrder.setApplicantId(currentUser.getId());
        workOrder.setApplicantName(currentUser.getNickName());

        // 设置物品信息
        ProductBase product = productBaseMapper.selectById(workOrder.getProductId());
        if (product != null) {
            workOrder.setProductName(product.getName());
        }

        workOrderMapper.insert(workOrder);

        // 记录操作日志
        operationLogService.recordOperation("创建工单",
                "用户" + currentUser.getNickName() + "创建了工单：" + orderNumber,
                currentUser, orderNumber);

        return workOrder;
    }

    /**
     * 生成工单号：WO + 年月日 + 6位序号
     */
    private String generateOrderNumber() {
        String dateStr = java.time.LocalDate.now().toString().replace("-", "");
        String prefix = "WO" + dateStr;

        LambdaQueryWrapper<WorkOrder> wrapper = Wrappers.lambdaQuery();
        wrapper.likeRight(WorkOrder::getOrderNumber, prefix);
        wrapper.orderByDesc(WorkOrder::getOrderNumber);

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
     * 管理员审批工单 - 同意配送
     */
    @Transactional
    public boolean approveAndDeliver(WorkOrder workOrder, Integer actualQuantity, String deliveryPerson, User adminUser) {
        WorkOrder existingOrder = workOrderMapper.selectById(workOrder.getId());
        if (existingOrder == null || !"待处理".equals(existingOrder.getStatus())) {
            return false;
        }

        // 更新工单状态
        existingOrder.setStatus("已完成");
        existingOrder.setActualDeliveryQuantity(actualQuantity);
        existingOrder.setDeliveryPerson(deliveryPerson);
        existingOrder.setCompleteTime(new Date());
        existingOrder.setUpdateTime(new Date());

        workOrderMapper.updateById(existingOrder);

        // 减少库存
        ProductBase product = productBaseMapper.selectById(existingOrder.getProductId());
        if (product != null && product.getStockQuantity() >= actualQuantity) {
            product.setStockQuantity(product.getStockQuantity() - actualQuantity);
            productBaseMapper.updateById(product);
        }

        // 记录操作日志
        operationLogService.recordOperation("审批工单",
                "管理员" + adminUser.getNickName() + "同意了工单：" + existingOrder.getOrderNumber() +
                        "，实际配送数量：" + actualQuantity,
                adminUser, existingOrder.getOrderNumber());

        return true;
    }

    /**
     * 管理员拒绝工单
     */
    @Transactional
    public boolean rejectWorkOrder(Integer orderId, String rejectReason, User adminUser) {
        WorkOrder existingOrder = workOrderMapper.selectById(orderId);
        if (existingOrder == null || !"待处理".equals(existingOrder.getStatus())) {
            return false;
        }

        existingOrder.setStatus("已拒绝");
        existingOrder.setRejectReason(rejectReason);
        existingOrder.setUpdateTime(new Date());

        workOrderMapper.updateById(existingOrder);

        // 记录操作日志
        operationLogService.recordOperation("拒绝工单",
                "管理员" + adminUser.getNickName() + "拒绝了工单：" + existingOrder.getOrderNumber() +
                        "，拒绝理由：" + rejectReason,
                adminUser, existingOrder.getOrderNumber());

        return true;
    }

    /**
     * 根据申请人ID查询工单
     */
    public Page<WorkOrder> getWorkOrdersByApplicant(Integer applicantId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<WorkOrder> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(WorkOrder::getApplicantId, applicantId);
        wrapper.orderByDesc(WorkOrder::getCreateTime);
        return workOrderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 管理员查询所有工单
     */
    public Page<WorkOrder> getAllWorkOrders(Integer pageNum, Integer pageSize, String status, String applicantName) {
        LambdaQueryWrapper<WorkOrder> wrapper = Wrappers.lambdaQuery();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(WorkOrder::getStatus, status);
        }
        if (applicantName != null && !applicantName.isEmpty()) {
            wrapper.like(WorkOrder::getApplicantName, applicantName);
        }
        wrapper.orderByDesc(WorkOrder::getCreateTime);
        return workOrderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 根据工单号查询工单
     */
    public WorkOrder getWorkOrderByNumber(String orderNumber) {
        LambdaQueryWrapper<WorkOrder> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(WorkOrder::getOrderNumber, orderNumber);
        return workOrderMapper.selectOne(wrapper);
    }
}
