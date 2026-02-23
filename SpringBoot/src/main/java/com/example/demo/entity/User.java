package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("user")
@Data
public class User {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String username;
    private String nickName;
    private String password;
    private String employeeId;
    private String sex;
    private String address;
    private String phone;
    @TableField(exist = false)
    private String token;
    private Integer role; // 1=系统管理员, 2=仓库管理员, 3=员工
    @TableField(exist = false)
    private String confirm;
    private String alow; // 账号状态: 1=正常, 0=禁用
}
