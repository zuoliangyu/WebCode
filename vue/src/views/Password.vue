<template>
  <div class="page-container">
    <div class="card-container" style="max-width: 500px; margin: 40px auto">
      <h3 class="section-title">修改密码</h3>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="form.oldPassword" type="password" show-password placeholder="请输入当前密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitForm">确认修改</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import request from "@/utils/request";
import { ElMessage } from "element-plus";
import router from "@/router";

export default {
  name: "Password",
  data() {
    const validateConfirm = (rule, value, callback) => {
      if (value === "") {
        callback(new Error("请再次输入新密码"));
      } else if (value !== this.form.newPassword) {
        callback(new Error("两次密码不一致"));
      } else {
        callback();
      }
    };
    return {
      userId: null,
      form: {
        oldPassword: "",
        newPassword: "",
        confirmPassword: "",
      },
      rules: {
        oldPassword: [{ required: true, message: "请输入旧密码", trigger: "blur" }],
        newPassword: [{ required: true, message: "请输入新密码", trigger: "blur" }],
        confirmPassword: [{ required: true, validator: validateConfirm, trigger: "blur" }],
      },
    };
  },
  created() {
    let userJson = sessionStorage.getItem("user");
    if (!userJson) {
      router.push("/login");
      return;
    }
    let user = JSON.parse(userJson);
    this.userId = user.id;
  },
  methods: {
    submitForm() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          request.put("/user/password", null, {
            params: {
              id: this.userId,
              oldPassword: this.form.oldPassword,
              newPassword: this.form.newPassword,
            },
          }).then((res) => {
            if (res.code === "0") {
              ElMessage.success("密码修改成功，请重新登录");
              sessionStorage.removeItem("user");
              this.$router.push("/login");
            } else {
              ElMessage.error(res.msg);
            }
          });
        }
      });
    },
    resetForm() {
      this.$refs["form"].resetFields();
    },
  },
};
</script>

<style scoped>
.section-title {
  margin-bottom: 20px;
  color: var(--text-primary);
  font-size: 18px;
  font-weight: 600;
}
</style>
