<template>
  <div class="aside-container">
    <el-menu
        :default-active="path"
        class="aside-menu"
        router
    >
      <el-menu-item index="/dashboard">
        <el-icon><Odometer /></el-icon>
        <span>数据概览</span>
      </el-menu-item>

      <!-- 系统管理员 & 仓库管理员 -->
      <el-menu-item index="/material" v-if="user.role === 1 || user.role === 2">
        <el-icon><Box /></el-icon>
        <span>物料管理</span>
      </el-menu-item>

      <el-menu-item index="/order-approval" v-if="user.role === 1 || user.role === 2">
        <el-icon><Checked /></el-icon>
        <span>工单审批</span>
      </el-menu-item>

      <!-- 员工 -->
      <el-menu-item index="/outbound" v-if="user.role === 3">
        <el-icon><Upload /></el-icon>
        <span>出库申请</span>
      </el-menu-item>

      <el-menu-item index="/return" v-if="user.role === 3">
        <el-icon><Download /></el-icon>
        <span>退库申请</span>
      </el-menu-item>

      <el-menu-item index="/my-orders" v-if="user.role === 3">
        <el-icon><Document /></el-icon>
        <span>我的工单</span>
      </el-menu-item>

      <!-- 系统管理员 -->
      <el-menu-item index="/user" v-if="user.role === 1">
        <el-icon><UserFilled /></el-icon>
        <span>用户管理</span>
      </el-menu-item>

      <el-sub-menu index="personal">
        <template #title>
          <el-icon><Setting /></el-icon>
          <span>个人设置</span>
        </template>
        <el-menu-item index="/person">
          <el-icon><EditPen /></el-icon>
          <span>个人信息</span>
        </el-menu-item>
        <el-menu-item index="/password">
          <el-icon><Lock /></el-icon>
          <span>修改密码</span>
        </el-menu-item>
      </el-sub-menu>
    </el-menu>
  </div>
</template>

<script>
export default {
  name: "Aside",
  created() {
    let userStr = sessionStorage.getItem("user") || "{}";
    this.user = JSON.parse(userStr);
  },
  data() {
    return {
      user: {},
      path: this.$route.path,
    };
  },
};
</script>

<style scoped>
.aside-container {
  width: 200px;
  min-height: calc(100vh - 50px);
  background: #fff;
  border-right: 1px solid #EBEEF5;
}

.aside-menu {
  border-right: none;
  background-color: #fff;
}

.aside-menu .el-menu-item {
  color: #606266;
  font-size: 14px;
}

.aside-menu .el-menu-item:hover {
  background-color: #ECF5FF;
}

.aside-menu .el-menu-item.is-active {
  color: #409EFF;
  background-color: #ECF5FF;
  border-left: 3px solid #409EFF;
}

:deep(.el-sub-menu__title) {
  color: #606266;
}

:deep(.el-sub-menu__title:hover) {
  background-color: #ECF5FF;
}
</style>
