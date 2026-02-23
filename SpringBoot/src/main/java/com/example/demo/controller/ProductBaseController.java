package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.commom.Result;
import com.example.demo.entity.ProductBase;
import com.example.demo.mapper.ProductBaseMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/productbase")
public class ProductBaseController {
    @Resource
    ProductBaseMapper productBaseMapper;

    @PostMapping
    public Result<?> save(@RequestBody ProductBase productBase) {
        LambdaQueryWrapper<ProductBase> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ProductBase::getID, productBase.getID());
        ProductBase selectOne = productBaseMapper.selectOne(wrapper);
        if (selectOne != null) {
            return Result.error("-1", "物品编号已存在!");
        }
        productBaseMapper.insert(productBase);
        return Result.success();
    }

    @PutMapping
    public Result<?> update(@RequestBody ProductBase productBase) {
        LambdaQueryWrapper<ProductBase> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ProductBase::getID, productBase.getID()).ne(ProductBase::getID, productBase.getID());
        ProductBase selectOne = productBaseMapper.selectOne(wrapper);
        if (selectOne != null) {
            return Result.error("-1", "物品编号已存在!");
        }
        productBaseMapper.updateById(productBase);
        return Result.success();
    }

    //    批量删除
    @PostMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestBody List<Integer> ids) {
        productBaseMapper.deleteBatchIds(ids);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        productBaseMapper.deleteById(id);
        return Result.success();
    }

    @GetMapping
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search1,
                              @RequestParam(defaultValue = "") String search2,
                              @RequestParam(defaultValue = "") String search3) {
        LambdaQueryWrapper<ProductBase> wrappers = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(search1)) {
            wrappers.like(ProductBase::getID, search1);
        }
        if (StringUtils.isNotBlank(search2)) {
            wrappers.like(ProductBase::getName, search2);
        }
        if (StringUtils.isNotBlank(search3)) {
            wrappers.like(ProductBase::getCategory, search3);
        }
        wrappers.orderByDesc(ProductBase::getStockQuantity);    //按库存数量排序
        Page<ProductBase> productBasePage = productBaseMapper.selectPage(new Page<>(pageNum, pageSize), wrappers);
        return Result.success(productBasePage);
    }
}
