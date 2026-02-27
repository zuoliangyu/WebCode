package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.commom.Result;
import com.example.demo.entity.Material;
import com.example.demo.entity.User;
import com.example.demo.entity.WorkOrder;
import com.example.demo.mapper.MaterialMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.mapper.WorkOrderMapper;
import org.springframework.web.bind.annotation.*;

import com.example.demo.utils.TokenUtils;

import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    @Resource
    private UserMapper userMapper;

    @Resource
    private MaterialMapper materialMapper;

    @Resource
    private WorkOrderMapper workOrderMapper;

    @GetMapping
    public Result<?> dashboardData() {
        Map<String, Object> map = new HashMap<>();

        // 物料总数
        long materialCount = materialMapper.selectCount(null);
        map.put("materialCount", materialCount);

        // 员工数（role=3）
        LambdaQueryWrapper<User> empWrapper = Wrappers.lambdaQuery();
        empWrapper.eq(User::getRole, 3);
        long employeeCount = userMapper.selectCount(empWrapper);
        map.put("employeeCount", employeeCount);

        // 待审批工单数
        LambdaQueryWrapper<WorkOrder> pendingWrapper = Wrappers.lambdaQuery();
        pendingWrapper.eq(WorkOrder::getStatus, "待审批");
        long pendingCount = workOrderMapper.selectCount(pendingWrapper);
        map.put("pendingCount", pendingCount);

        // 总工单数
        long totalOrderCount = workOrderMapper.selectCount(null);
        map.put("totalOrderCount", totalOrderCount);

        // 库存预警数
        List<Material> allMaterials = materialMapper.selectList(null);
        long lowStockCount = allMaterials.stream()
                .filter(m -> m.getStockAlertThreshold() != null && m.getTotalQuantity() != null
                        && m.getTotalQuantity() <= m.getStockAlertThreshold())
                .count();
        map.put("lowStockCount", lowStockCount);

        // 即将过期物料数（30天内）
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 30);
        Date thirtyDaysLater = cal.getTime();
        LambdaQueryWrapper<Material> expiringWrapper = Wrappers.lambdaQuery();
        expiringWrapper.le(Material::getExpiryDate, thirtyDaysLater);
        long expiringCount = materialMapper.selectCount(expiringWrapper);
        map.put("expiringCount", expiringCount);

        // 各分类物料数量（供柱状图）
        int[] categoryCount = new int[4];
        for (Material m : allMaterials) {
            if (m.getCategory() != null && m.getCategory() >= 0 && m.getCategory() <= 3) {
                categoryCount[m.getCategory()]++;
            }
        }
        map.put("categoryCounts", categoryCount);

        return Result.success(map);
    }

    // 获取员工个人仪表盘数据
    @GetMapping("/my")
    public Result<?> myDashboardData() {
        User currentUser = TokenUtils.getUser();
        if (currentUser == null) {
            return Result.error("-1", "未登录");
        }
        Map<String, Object> map = new HashMap<>();

        // 我的待审批工单数
        LambdaQueryWrapper<WorkOrder> myPendingWrapper = Wrappers.lambdaQuery();
        myPendingWrapper.eq(WorkOrder::getApplicantId, currentUser.getId())
                .eq(WorkOrder::getStatus, "待审批");
        long myPendingCount = workOrderMapper.selectCount(myPendingWrapper);
        map.put("myPendingCount", myPendingCount);

        // 我的工单总数
        LambdaQueryWrapper<WorkOrder> myTotalWrapper = Wrappers.lambdaQuery();
        myTotalWrapper.eq(WorkOrder::getApplicantId, currentUser.getId());
        long myTotalCount = workOrderMapper.selectCount(myTotalWrapper);
        map.put("myTotalCount", myTotalCount);

        return Result.success(map);
    }

    // 获取过期预警物料列表
    @GetMapping("/expiring-materials")
    public Result<?> getExpiringMaterials() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 30);
        Date thirtyDaysLater = cal.getTime();

        LambdaQueryWrapper<Material> wrapper = Wrappers.lambdaQuery();
        wrapper.le(Material::getExpiryDate, thirtyDaysLater);
        wrapper.orderByAsc(Material::getExpiryDate);
        List<Material> list = materialMapper.selectList(wrapper);
        return Result.success(list);
    }
}
