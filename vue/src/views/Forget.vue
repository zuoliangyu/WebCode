<template>
  <div class="login-container">
    <el-form ref="form" :model="form" :rules="rules" class="login-page">
      <h2 class="title" style="margin-bottom: 20px; text-align: center; color: #409EFF">密码重置</h2>
      <el-form-item prop="username">
        <el-input v-model="form.username" placeholder="请输入您的用户名" clearable>
          <template #prefix>
            <el-icon class="el-input__icon"><User /></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input v-model="form.password" placeholder="请输入新密码" clearable show-password>
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
      <el-form-item>
        <el-button type="primary" style="width: 100%; font-size: 16px" @click="resetPassword">重置密码</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="text" style="font-size: 14px" @click="$router.push('/login')">返回登录</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import request from "../utils/request";
import { ElMessage } from "element-plus";

export default {
  name: "Forget",
  data() {
    return {
      form: {},
      rules: {
        username: [
          { required: true, message: "请输入用户名", trigger: "blur" },
          { min: 2, max: 13, message: "长度要求为2到13位", trigger: "blur" },
        ],
        password: [{ required: true, message: "请输入新密码", trigger: "blur" }],
        confirm: [{ required: true, message: "请确认密码", trigger: "blur" }],
      },
    };
  },
  methods: {
    resetPassword() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          if (this.form.password !== this.form.confirm) {
            ElMessage.error("两次密码输入不一致");
            return;
          }
          request.post("/forget/reset", {
            username: this.form.username,
            password: this.form.password,
          }).then((res) => {
            if (res.code == 0) {
              ElMessage.success("密码重置成功");
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
