package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.commom.Result;
import com.example.demo.entity.ProductBase;
import com.example.demo.mapper.ProductBaseMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/StockAlert")
public class StockAlertController {
    @Resource
    private ProductBaseMapper productBaseMapper;

    @GetMapping("/getIfStockAlert")
    public Result<?> getIfStockAlert() {
        List<ProductBase> alertProducts = this.getAlertProducts();
        if (alertProducts == null) {
            return Result.error("-1", "查询失败,检查数据库");
        }

        if (alertProducts.isEmpty()) {
            return Result.success("无数量预警");
        }

        return Result.success(alertProducts);
    }

    public List<ProductBase> getAlertProducts() {
        return productBaseMapper.selectList(
                new LambdaQueryWrapper<ProductBase>()
                        .apply("stock_quantity > stock_alert_threshold")
        );
    }
}
