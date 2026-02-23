<template>
  <div class="login-container">
    <el-form ref="form" :model="form" :rules="rules" class="login-page">
      <h2 class="title" style="margin-bottom: 20px; text-align: center; color: #409EFF">用户注册</h2>
      <el-form-item prop="username">
        <el-input v-model="form.username" placeholder="请输入用户名" clearable>
          <template #prefix>
            <el-icon class="el-input__icon"><User /></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input v-model="form.password" placeholder="请输入密码" clearable show-password>
          <template #prefix>
            <el-icon class="el-input__icon"><Lock /></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="confirm">
        <el-input v-model="form.confirm" placeholder="请再次确认密码" clearable show-password>
          <template #prefix>
            <el-icon class="el-input__icon"><Lock /></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="nickName">
        <el-input v-model="form.nickName" placeholder="请输入姓名" clearable>
          <template #prefix>
            <el-icon class="el-input__icon"><Postcard /></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="role">
        <el-radio-group v-model="form.role">
          <el-radio :label="3">员工</el-radio>
          <el-radio :label="2">仓库管理员</el-radio>
          <el-radio :label="1">系统管理员</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item prop="authorize" v-if="form.role === 1">
        <el-input v-model="form.authorize" placeholder="请输入管理员注册码" clearable show-password>
          <template #prefix>
            <el-icon class="el-input__icon"><Key /></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" style="width: 100%; font-size: 16px" @click="register">注 册</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="text" style="font-size: 14px" @click="$router.push('/login')">已有账号? 前往登录</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import request from "../utils/request";
import { ElMessage } from "element-plus";

export default {
  name: "Register",
  data() {
    return {
      form: { role: 3 },
      rules: {
        username: [
          { required: true, message: "请输入用户名", trigger: "blur" },
          { min: 2, max: 13, message: "长度要求为2到13位", trigger: "blur" },
        ],
        password: [{ required: true, message: "请输入密码", trigger: "blur" }],
        confirm: [{ required: true, message: "请确认密码", trigger: "blur" }],
        nickName: [{ required: true, message: "请输入姓名", trigger: "blur" }],
      },
    };
  },
  methods: {
    register() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          if (this.form.password !== this.form.confirm) {
            ElMessage.error("两次密码输入不一致");
            return;
          }
          if (this.form.role === 1 && this.form.authorize !== "2236") {
            ElMessage.error("请输入正确的管理员注册码");
            return;
          }
          request.post("user/register", this.form).then((res) => {
            if (res.code == 0) {
              ElMessage.success("注册成功");
              this.$router.push("/login");
            } else {
              ElMessage.error(res.msg);
            }
          });
        }
      });
    },
  },
};
</script>

<style scoped>
.login-container {
  position: fixed;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}
.login-page {
  border-radius: 8px;
  width: 380px;
  padding: 35px 35px 15px;
  background: #fff;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}
</style>
