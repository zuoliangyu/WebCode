package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("MetalWorkpiece")
@Data
public class MetalWorkpiece {
    private ProductBase productBase;

    private String material;
    private String precisionGrade;
}
