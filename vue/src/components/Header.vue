<template>
  <div class="header-container">
    <div class="header-left">
      <img :src="imgUrl" class="header-logo" />
      <span class="header-title">总成车间物料管理系统</span>
    </div>
    <div class="header-right">
      <el-tag :type="roleTagType" size="small" style="margin-right: 12px">{{ roleName }}</el-tag>
      <el-dropdown>
        <span class="el-dropdown-link">
          {{ user.nickName || user.username }}
          <el-icon class="el-icon--right"><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="$router.push('/person')">个人信息</el-dropdown-item>
            <el-dropdown-item @click="$router.push('/password')">修改密码</el-dropdown-item>
            <el-dropdown-item divided @click="exit">退出系统</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script>
import { ElMessage } from "element-plus";

export default {
  name: "Header",
  data() {
    return {
      user: {},
      imgUrl: require("../assets/icon/login.png"),
    };
  },
  computed: {
    roleName() {
      const roleMap = { 1: "系统管理员", 2: "仓库管理员", 3: "员工" };
      return roleMap[this.user.role] || "未知角色";
    },
    roleTagType() {
      const typeMap = { 1: "danger", 2: "warning", 3: "" };
      return typeMap[this.user.role] || "info";
    },
  },
  created() {
    let userStr = sessionStorage.getItem("user") || "{}";
    this.user = JSON.parse(userStr);
  },
  methods: {
    exit() {
      sessionStorage.removeItem("user");
      this.$router.push("/login");
      ElMessage.success("退出系统成功");
    },
  },
};
</script>

<style scoped>
.header-container {
  height: 50px;
  line-height: 50px;
  background: #fff;
  border-bottom: 1px solid #EBEEF5;
  display: flex;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-logo {
  width: 32px;
  height: 32px;
  margin-right: 10px;
}

.header-title {
  font-weight: bold;
  font-size: 16px;
  color: #409EFF;
}

.header-right {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  cursor: pointer;
  color: #606266;
  font-size: 14px;
}
</style>
