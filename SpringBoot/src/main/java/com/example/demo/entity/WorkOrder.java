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

    private String orderNumber;
    private String orderType; // 出库/退库
    private Integer applicantId;
    private String applicantName;
    private String employeeId;
    private Integer productId; // 物料ID
    private String productName;
    private String drawingNumber;
    private Integer numberOfSets;
    private Integer perSetQuantity; // 单套用量快照
    private Integer quantity;
    private String purpose;
    private String status; // 待审批/已通过/已拒绝
    private String rejectReason;
    private Integer actualDeliveryQuantity;
    private String returnDestination;
    private String relatedOrderNumber;
    private Integer approverId;
    private String approverName;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date completeTime;
}
