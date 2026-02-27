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
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-right: 1px solid rgba(255, 255, 255, 0.15);
}

.aside-menu {
  border-right: none;
  background-color: transparent !important;
}

.aside-menu .el-menu-item {
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
  font-weight: 500;
  margin: 4px 8px;
  border-radius: 10px;
  height: 44px;
  line-height: 44px;
  transition: all 0.3s ease;
}

.aside-menu .el-menu-item .el-icon {
  color: rgba(255, 255, 255, 0.7);
  font-size: 18px;
  transition: all 0.3s ease;
}

.aside-menu .el-menu-item:hover {
  background: rgba(255, 255, 255, 0.15) !important;
  color: #fff;
}

.aside-menu .el-menu-item:hover .el-icon {
  color: #fff;
}

.aside-menu .el-menu-item.is-active {
  color: #fff !important;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.6) 0%, rgba(139, 92, 246, 0.6) 100%) !important;
  border-left: none;
  box-shadow: 0 4px 15px rgba(99, 102, 241, 0.3);
}

.aside-menu .el-menu-item.is-active .el-icon {
  color: #fff;
}

:deep(.el-sub-menu__title) {
  color: rgba(255, 255, 255, 0.85) !important;
  font-weight: 500;
  margin: 4px 8px;
  border-radius: 10px;
  height: 44px !important;
  line-height: 44px !important;
  transition: all 0.3s ease;
}

:deep(.el-sub-menu__title .el-icon) {
  color: rgba(255, 255, 255, 0.7);
  font-size: 18px;
}

:deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.15) !important;
  color: #fff !important;
}

:deep(.el-sub-menu__title:hover .el-icon) {
  color: #fff;
}

:deep(.el-sub-menu .el-menu) {
  background: transparent !important;
}

:deep(.el-sub-menu__icon-arrow) {
  color: rgba(255, 255, 255, 0.5);
}
</style>
