package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.commom.Result;
import com.example.demo.entity.Material;
import com.example.demo.mapper.MaterialMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/material")
public class MaterialController {

    @Resource
    private MaterialMapper materialMapper;

    @PostMapping
    public Result<?> save(@RequestBody Material material) {
        material.setCreateTime(new Date());
        material.setUpdateTime(new Date());
        materialMapper.insert(material);
        return Result.success();
    }

    @PutMapping
    public Result<?> update(@RequestBody Material material) {
        material.setUpdateTime(new Date());
        materialMapper.updateById(material);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        materialMapper.deleteById(id);
        return Result.success();
    }

    @PostMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestBody List<Integer> ids) {
        materialMapper.deleteBatchIds(ids);
        return Result.success();
    }

    @GetMapping
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search,
                              @RequestParam(required = false) Integer category) {
        LambdaQueryWrapper<Material> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(search)) {
            wrapper.and(w -> w.like(Material::getName, search)
                    .or().like(Material::getManufacturer, search)
                    .or().like(Material::getDrawingNumber, search));
        }
        if (category != null) {
            wrapper.eq(Material::getCategory, category);
        }
        wrapper.orderByDesc(Material::getUpdateTime);
        Page<Material> page = materialMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    @GetMapping("/all")
    public Result<?> findAll() {
        List<Material> list = materialMapper.selectList(Wrappers.lambdaQuery(Material.class).orderByAsc(Material::getName));
        return Result.success(list);
    }

    @GetMapping("/expiring")
    public Result<?> findExpiring() {
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 30);
        Date thirtyDaysLater = cal.getTime();

        LambdaQueryWrapper<Material> wrapper = Wrappers.lambdaQuery();
        wrapper.le(Material::getExpiryDate, thirtyDaysLater);
        wrapper.orderByAsc(Material::getExpiryDate);
        List<Material> list = materialMapper.selectList(wrapper);
        return Result.success(list);
    }

    @GetMapping("/lowstock")
    public Result<?> findLowStock() {
        List<Material> all = materialMapper.selectList(null);
        List<Material> lowStock = new java.util.ArrayList<>();
        for (Material m : all) {
            if (m.getStockAlertThreshold() != null && m.getTotalQuantity() != null
                    && m.getTotalQuantity() <= m.getStockAlertThreshold()) {
                lowStock.add(m);
            }
        }
        return Result.success(lowStock);
    }
}
