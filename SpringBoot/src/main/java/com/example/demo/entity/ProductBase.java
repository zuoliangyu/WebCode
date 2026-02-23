package com.example.demo.entity;

import lombok.Data;

@Data
public class ProductBase {

    //通用属性：物品编号、名称、型号、批次、分类、库存量、库存预警阈值、存放位置（如库位号）。
    //
    //- ID, Name, ModelId, Batch, Category, Stock Quantity, Stock Alert Threshold, Storage Location

    /*
     * ID: 物品唯一标识符
     * name: 物品名称
     * ModeId: 型号ID，关联型号信息
     * Batch: 批次号，用于区分同类型物品的不同生产批次
     * Category: 分类ID，表示物品所属的类别 0: 金工 ,1: 元器件 ,2: 仪器仪表 ,3: 工装
     * StockQuantity: 当前库存数量
     * StockAlertThreshold: 库存预警阈值，当库存低于此值时触发预警
     * StorageLocation: 存储位置信息，如库位号或货架位置
     */

    private Integer ID;
    private String name;
    private Integer Category;
    private Integer StockQuantity;
    private Integer StockAlertThreshold;

    // private Integer ModeId;
    // private Integer Batch;
    // private String StorageLocation;


}
