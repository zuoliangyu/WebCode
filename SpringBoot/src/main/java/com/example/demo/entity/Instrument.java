package com.example.demo.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("Instrument")
@Data
public class Instrument {
    private ProductBase productBase;

    // 添加其他属性
}
