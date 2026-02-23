package com.example.demo.controller;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.commom.Result;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    UserMapper userMapper;

    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        User res = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, user.getUsername()));
        if (res != null) {
            return Result.error("-1", "用户名已重复");
        }
        if (user.getRole() == null) {
            user.setRole(3); // 默认为员工
        }
        user.setAlow("1"); // 默认正常状态
        userMapper.insert(user);
        return Result.success();
    }

    @CrossOrigin
    @PostMapping("/login")
    public Result<?> login(@RequestBody User user) {
        User res = userMapper.selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, user.getUsername())
                .eq(User::getPassword, user.getPassword()));
        if (res == null) {
            return Result.error("-1", "用户名或密码错误");
        }
        if ("0".equals(res.getAlow())) {
            return Result.error("-1", "账号已被禁用，请联系管理员");
        }
        String token = TokenUtils.genToken(res);
        res.setToken(token);
        return Result.success(res);
    }

    @PostMapping
    public Result<?> save(@RequestBody User user) {
        if (user.getPassword() == null) {
            user.setPassword("123456");
        }
        if (user.getAlow() == null) {
            user.setAlow("1");
        }
        userMapper.insert(user);
        return Result.success();
    }

    @PutMapping("/password")
    public Result<?> updatePassword(@RequestParam Integer id,
                                    @RequestParam String oldPassword,
                                    @RequestParam String newPassword) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("-1", "用户不存在");
        }
        if (!user.getPassword().equals(oldPassword)) {
            return Result.error("-1", "旧密码错误");
        }
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);
        User updateUser = new User();
        updateUser.setPassword(newPassword);
        userMapper.update(updateUser, updateWrapper);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody User user) {
        userMapper.updateById(user);
        return Result.success();
    }

    @PostMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestBody List<Integer> ids) {
        userMapper.deleteBatchIds(ids);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        userMapper.deleteById(id);
        return Result.success();
    }

    // 切换账号状态（正常/禁用）
    @PutMapping("/toggle/{id}")
    public Result<?> toggleStatus(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("-1", "用户不存在");
        }
        user.setAlow("1".equals(user.getAlow()) ? "0" : "1");
        userMapper.updateById(user);
        return Result.success();
    }

    // 管理员重置用户密码
    @PutMapping("/resetPassword/{id}")
    public Result<?> resetPassword(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("-1", "用户不存在");
        }
        user.setPassword("123456");
        userMapper.updateById(user);
        return Result.success();
    }

    @GetMapping
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search) {
        LambdaQueryWrapper<User> wrappers = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(search)) {
            wrappers.and(w -> w.like(User::getNickName, search)
                    .or().like(User::getUsername, search)
                    .or().like(User::getEmployeeId, search));
        }
        // 查询 role=2,3 的用户
        wrappers.in(User::getRole, 2, 3);
        wrappers.orderByAsc(User::getId);
        Page<User> userPage = userMapper.selectPage(new Page<>(pageNum, pageSize), wrappers);
        return Result.success(userPage);
    }

    // xlsx 批量导入用户
    @PostMapping("/import")
    public Result<?> importUsers(@RequestParam("file") MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            ExcelReader reader = ExcelUtil.getReader(inputStream);
            List<Map<String, Object>> readAll = reader.readAll();

            int successCount = 0;
            int skipCount = 0;

            for (Map<String, Object> row : readAll) {
                String username = row.get("用户名") != null ? row.get("用户名").toString().trim() : null;
                String password = row.get("密码") != null ? row.get("密码").toString().trim() : "123456";
                String nickName = row.get("姓名") != null ? row.get("姓名").toString().trim() : null;
                String employeeId = row.get("工号") != null ? row.get("工号").toString().trim() : null;
                String roleStr = row.get("角色") != null ? row.get("角色").toString().trim() : "员工";

                if (username == null || username.isEmpty()) {
                    skipCount++;
                    continue;
                }

                // 检查用户名是否重复
                User existing = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username));
                if (existing != null) {
                    skipCount++;
                    continue;
                }

                User user = new User();
                user.setUsername(username);
                user.setPassword(password);
                user.setNickName(nickName);
                user.setEmployeeId(employeeId);
                user.setAlow("1");

                if ("仓库管理员".equals(roleStr)) {
                    user.setRole(2);
                } else {
                    user.setRole(3);
                }

                userMapper.insert(user);
                successCount++;
            }

            Map<String, Object> result = new HashMap<>();
            result.put("successCount", successCount);
            result.put("skipCount", skipCount);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("-1", "导入失败: " + e.getMessage());
        }
    }
}
