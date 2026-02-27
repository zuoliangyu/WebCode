<template>
  <div class="login-container">
    <div class="floating-orbs">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
    </div>

    <el-form ref="form" :model="form" :rules="rules" class="login-page">
      <h2 class="title">密码重置</h2>
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
        <el-button type="primary" class="login-btn" @click="resetPassword">重置密码</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="text" class="link-btn" @click="$router.push('/login')">返回登录</el-button>
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
  overflow: hidden;
}

.floating-orbs {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.4;
  animation: float 8s ease-in-out infinite;
}

.orb-1 {
  width: 300px;
  height: 300px;
  background: rgba(99, 102, 241, 0.5);
  top: 10%;
  left: 15%;
  animation-delay: 0s;
}

.orb-2 {
  width: 250px;
  height: 250px;
  background: rgba(139, 92, 246, 0.5);
  top: 60%;
  right: 10%;
  animation-delay: -3s;
}

.orb-3 {
  width: 200px;
  height: 200px;
  background: rgba(236, 72, 153, 0.4);
  bottom: 10%;
  left: 40%;
  animation-delay: -5s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(30px, -30px) scale(1.05); }
  50% { transform: translate(-20px, 20px) scale(0.95); }
  75% { transform: translate(15px, 10px) scale(1.02); }
}

.login-page {
  border-radius: 20px;
  width: 420px;
  padding: 40px 40px 20px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.2);
  position: relative;
  z-index: 1;
}

.title {
  margin-bottom: 28px;
  text-align: center;
  font-size: 22px;
  font-weight: 700;
  background: linear-gradient(135deg, #fff 0%, #e0e7ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 2px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  border-radius: 12px !important;
  letter-spacing: 4px;
}

.link-btn {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8) !important;
}

.link-btn:hover {
  color: #fff !important;
}

:deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.15) !important;
  border-radius: 12px !important;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.2) inset !important;
  height: 44px;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.4) inset !important;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.5) inset, 0 0 0 3px rgba(255, 255, 255, 0.1) !important;
}

:deep(.el-input__inner) {
  color: #fff;
  height: 44px;
}

:deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.6);
}

:deep(.el-input__prefix .el-icon) {
  color: rgba(255, 255, 255, 0.7);
}

:deep(.el-form-item__error) {
  color: #fca5a5;
}
</style>
