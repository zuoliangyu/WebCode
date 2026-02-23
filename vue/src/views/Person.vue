<template>
  <div class="page-container">
    <div class="card-container" style="max-width: 600px; margin: 20px auto">
      <h3 style="margin-bottom: 20px; color: #303133">个人信息</h3>
      <el-form :model="form" ref="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.nickName"></el-input>
        </el-form-item>
        <el-form-item label="工号">
          <el-input v-model="form.employeeId" placeholder="请输入工号"></el-input>
        </el-form-item>
        <el-form-item label="角色">
          <el-tag :type="roleTagType">{{ roleName }}</el-tag>
        </el-form-item>
        <el-form-item label="电话号码">
          <el-input v-model="form.phone" placeholder="请输入电话号码"></el-input>
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.sex">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="地址">
          <el-input type="textarea" v-model="form.address" placeholder="请输入地址"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="update">保存</el-button>
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
  name: "Person",
  data() {
    return { form: {} };
  },
  computed: {
    roleName() {
      const map = { 1: "系统管理员", 2: "仓库管理员", 3: "员工" };
      return map[this.form.role] || "未知";
    },
    roleTagType() {
      const map = { 1: "danger", 2: "warning", 3: "" };
      return map[this.form.role] || "info";
    },
  },
  created() {
    let userJson = sessionStorage.getItem("user");
    if (!userJson) {
      router.push("/login");
      return;
    }
    this.form = JSON.parse(userJson);
  },
  methods: {
    update() {
      request.put("/user/update", this.form).then((res) => {
        if (res.code === "0") {
          ElMessage.success("更新成功");
          sessionStorage.setItem("user", JSON.stringify(this.form));
        } else {
          ElMessage.error(res.msg);
        }
      });
    },
  },
};
</script>
