package com.example.demo.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("Tooling")
@Data
public class Tooling {
    private ProductBase productBase;

    // 添加其他属性
}
