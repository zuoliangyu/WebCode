package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@TableName("material")
@Data
public class Material {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;
    private String manufacturer;
    private Integer category; // 0=标件, 1=金工件, 2=元器件, 3=物资
    private Integer perSetQuantity;
    private Integer totalQuantity;
    private Integer stockAlertThreshold;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date expiryDate;

    private String drawingNumber;
    private String specification;
    private String unit;
    private String storageLocation;
    private String remark;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
