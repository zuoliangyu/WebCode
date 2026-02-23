package com.example.demo.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("ElectronicComponent")
@Data
public class ElectronicComponent {
    private ProductBase productBase;

    // 添加其他属性和关联
}
