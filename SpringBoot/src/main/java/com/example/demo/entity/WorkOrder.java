package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@TableName("work_order")
@Data
public class WorkOrder {
    @TableId(type = IdType.AUTO)
    private Integer id;

    // 工单号
    private String orderNumber;

    // 申请人ID
    private Integer applicantId;

    // 申请人姓名
    private String applicantName;

    // 申请物品ID
    private Integer productId;

    // 申请物品名称
    private String productName;

    // 物品类型
    private String productType;

    // 型号
    private String model;

    // 批次
    private String batch;

    // 申请数量
    private Integer quantity;

    // 用途说明
    private String purpose;

    // 期望配送时间
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expectedDeliveryTime;

    // 工单状态：待处理、配送中、已完成、已拒绝
    private String status;

    // 拒绝理由
    private String rejectReason;

    // 实际配送数量
    private Integer actualDeliveryQuantity;

    // 配送人
    private String deliveryPerson;

    // 创建时间
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    // 更新时间
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    // 完成时间
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date completeTime;
}
