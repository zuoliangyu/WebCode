<template>
  <div class="login-container">
    <el-form ref="form" :model="form" :rules="rules" class="login-page">
      <h2 class="title" style="margin-bottom: 20px; text-align: center; color: #409EFF">总成车间物料管理系统</h2>
      <el-form-item prop="username">
        <el-input v-model="form.username" placeholder="用户名" clearable>
          <template #prefix>
            <el-icon class="el-input__icon"><User /></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input v-model="form.password" placeholder="密码" clearable show-password>
          <template #prefix>
            <el-icon class="el-input__icon"><Lock /></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" style="width: 100%; height: 40px; font-size: 16px" @click="login">登 录</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="text" style="font-size: 14px; color: #F56C6C" @click="$router.push('/forget')">忘记密码?</el-button>
        <el-button type="text" style="font-size: 14px; float: right" @click="$router.push('/register')">没有账号? 前往注册</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import request from "../utils/request";
import { ElMessage } from "element-plus";

export default {
  name: "Login",
  data() {
    return {
      form: {},
      rules: {
        username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
        password: [{ required: true, message: "请输入密码", trigger: "blur" }],
      },
    };
  },
  methods: {
    login() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          request.post("user/login", this.form).then((res) => {
            if (res.code == 0) {
              ElMessage.success("登录成功");
              sessionStorage.setItem("user", JSON.stringify(res.data));
              this.$router.push("/dashboard");
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
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}
.login-page {
  border-radius: 8px;
  width: 400px;
  padding: 35px 35px 15px;
  background: #fff;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}
::v-deep(.el-input__inner) {
  height: 40px;
}
</style>
