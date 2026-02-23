package com.example.demo.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@TableName("operation_log")
@Data
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Integer id;

    // 操作类型
    private String operationType;

    // 操作描述
    private String description;

    // 操作人ID
    private Integer operatorId;

    // 操作人姓名
    private String operatorName;

    // 关联工单号
    private String orderNumber;

    // 操作时间
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operateTime;
}
