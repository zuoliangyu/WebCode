package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.commom.Result;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/forget")
public class ForgetController {

    @Resource
    private UserMapper userMapper;

    // 管理员直接重置密码（不需要验证码）
    @PostMapping("/reset")
    public Result<?> resetPassword(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            return Result.error("-1", "用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return Result.error("-1", "新密码不能为空");
        }

        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(User::getUsername, user.getUsername());
        User existingUser = userMapper.selectOne(wrapper);
        if (existingUser == null) {
            return Result.error("-1", "用户名不存在");
        }

        existingUser.setPassword(user.getPassword());
        userMapper.updateById(existingUser);
        return Result.success();
    }
}
