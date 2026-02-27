<template>
  <div class="page-container">
    <!-- 管理员/仓库管理员：完整统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 20px" v-if="user.role !== 3">
      <el-col :span="4" v-for="(item, index) in statCards" :key="index">
        <div class="stat-card">
          <div class="stat-icon" :style="{ background: item.gradient }">
            <span class="stat-icon-text">{{ item.icon }}</span>
          </div>
          <div class="stat-number">{{ item.value }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 员工：个人统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 20px" v-if="user.role === 3">
      <el-col :span="6" v-for="(item, index) in myStatCards" :key="index">
        <div class="stat-card">
          <div class="stat-icon" :style="{ background: item.gradient }">
            <span class="stat-icon-text">{{ item.icon }}</span>
          </div>
          <div class="stat-number">{{ item.value }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 有效期预警区域（仅管理员/仓库管理员可见） -->
    <div class="card-container" v-if="user.role !== 3 && expiringMaterials.length > 0">
      <h4 class="section-title">
        <el-icon style="color: #f59e0b; vertical-align: middle"><WarningFilled /></el-icon>
        有效期预警
      </h4>
      <el-row :gutter="12">
        <el-col :span="6" v-for="item in expiringMaterials" :key="item.id" style="margin-bottom: 12px">
          <div :class="['expiry-card', getExpiryClass(item.expiryDate)]">
            <div style="font-weight: 600; font-size: 14px; margin-bottom: 6px">{{ item.name }}</div>
            <div style="font-size: 12px; color: #94a3b8; margin-bottom: 4px">
              {{ categoryName(item.category) }} | {{ item.specification }}
            </div>
            <div style="font-size: 12px; margin-bottom: 4px">
              有效期: {{ item.expiryDate }}
            </div>
            <div :style="{ fontSize: '13px', fontWeight: 600, color: getExpiryColor(item.expiryDate) }">
              {{ getExpiryText(item.expiryDate) }}
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域（仅管理员/仓库管理员可见） -->
    <el-row :gutter="16" v-if="user.role !== 3">
      <el-col :span="12">
        <div class="card-container">
          <h4 class="section-title">物料分类统计</h4>
          <div id="categoryChart" style="height: 300px"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="card-container">
          <h4 class="section-title">系统信息</h4>
          <div style="padding: 20px">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="当前时间">{{ currentTime }}</el-descriptions-item>
              <el-descriptions-item label="当前用户">{{ user.nickName }}</el-descriptions-item>
              <el-descriptions-item label="用户角色">{{ roleName }}</el-descriptions-item>
              <el-descriptions-item label="工号">{{ user.employeeId || '-' }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 员工的系统信息（全宽显示） -->
    <el-row :gutter="16" v-if="user.role === 3">
      <el-col :span="24">
        <div class="card-container">
          <h4 class="section-title">系统信息</h4>
          <div style="padding: 20px">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="当前时间">{{ currentTime }}</el-descriptions-item>
              <el-descriptions-item label="当前用户">{{ user.nickName }}</el-descriptions-item>
              <el-descriptions-item label="用户角色">{{ roleName }}</el-descriptions-item>
              <el-descriptions-item label="工号">{{ user.employeeId || '-' }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from "echarts";
import request from "@/utils/request";
import router from "@/router";

export default {
  name: "Dashboard",
  data() {
    return {
      user: {},
      currentTime: "",
      statCards: [
        { label: "物料总数", value: 0, icon: "📦", gradient: "linear-gradient(135deg, #6366f1, #8b5cf6)" },
        { label: "员工数", value: 0, icon: "👥", gradient: "linear-gradient(135deg, #10b981, #34d399)" },
        { label: "待审批", value: 0, icon: "⏳", gradient: "linear-gradient(135deg, #f59e0b, #fbbf24)" },
        { label: "总工单", value: 0, icon: "📋", gradient: "linear-gradient(135deg, #64748b, #94a3b8)" },
        { label: "库存预警", value: 0, icon: "⚠️", gradient: "linear-gradient(135deg, #ef4444, #f87171)" },
        { label: "即将过期", value: 0, icon: "⏰", gradient: "linear-gradient(135deg, #f59e0b, #fbbf24)" },
      ],
      myStatCards: [
        { label: "我的待审批", value: 0, icon: "⏳", gradient: "linear-gradient(135deg, #f59e0b, #fbbf24)" },
        { label: "我的工单", value: 0, icon: "📋", gradient: "linear-gradient(135deg, #6366f1, #8b5cf6)" },
      ],
      expiringMaterials: [],
      categoryCounts: [0, 0, 0, 0],
    };
  },
  computed: {
    roleName() {
      const map = { 1: "系统管理员", 2: "仓库管理员", 3: "员工" };
      return map[this.user.role] || "";
    },
  },
  created() {
    let userJson = sessionStorage.getItem("user");
    if (!userJson) {
      router.push("/login");
      return;
    }
    this.user = JSON.parse(userJson);
  },
  mounted() {
    this.startTimer();
    if (this.user.role === 3) {
      this.loadMyDashboard();
    } else {
      this.loadDashboard();
      this.loadExpiringMaterials();
    }
  },
  methods: {
    startTimer() {
      this.updateTime();
      setInterval(() => this.updateTime(), 1000);
    },
    updateTime() {
      this.currentTime = new Date().toLocaleString();
    },
    loadDashboard() {
      request.get("/dashboard").then((res) => {
        if (res.code === "0") {
          const d = res.data;
          this.statCards[0].value = d.materialCount || 0;
          this.statCards[1].value = d.employeeCount || 0;
          this.statCards[2].value = d.pendingCount || 0;
          this.statCards[3].value = d.totalOrderCount || 0;
          this.statCards[4].value = d.lowStockCount || 0;
          this.statCards[5].value = d.expiringCount || 0;
          this.categoryCounts = d.categoryCounts || [0, 0, 0, 0];
          this.$nextTick(() => this.initChart());
        }
      });
    },
    loadMyDashboard() {
      request.get("/dashboard/my").then((res) => {
        if (res.code === "0") {
          const d = res.data;
          this.myStatCards[0].value = d.myPendingCount || 0;
          this.myStatCards[1].value = d.myTotalCount || 0;
        }
      });
    },
    loadExpiringMaterials() {
      request.get("/dashboard/expiring-materials").then((res) => {
        if (res.code === "0") {
          this.expiringMaterials = res.data || [];
        }
      });
    },
    initChart() {
      const el = document.getElementById("categoryChart");
      if (!el) return;
      const chart = echarts.init(el);
      chart.setOption({
        tooltip: { trigger: "axis" },
        xAxis: {
          type: "category",
          data: ["标件", "金工件", "元器件", "物资"],
          axisTick: { alignWithLabel: true },
          axisLine: { lineStyle: { color: "rgba(255,255,255,0.3)" } },
          axisLabel: { color: "#475569" },
        },
        yAxis: {
          type: "value",
          axisLine: { lineStyle: { color: "rgba(255,255,255,0.3)" } },
          axisLabel: { color: "#475569" },
          splitLine: { lineStyle: { color: "rgba(255,255,255,0.15)" } },
        },
        series: [
          {
            type: "bar",
            barWidth: "40%",
            itemStyle: { borderRadius: [6, 6, 0, 0] },
            data: [
              { value: this.categoryCounts[0], itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#6366f1' }, { offset: 1, color: '#8b5cf6' }]) } },
              { value: this.categoryCounts[1], itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#f59e0b' }, { offset: 1, color: '#fbbf24' }]) } },
              { value: this.categoryCounts[2], itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#10b981' }, { offset: 1, color: '#34d399' }]) } },
              { value: this.categoryCounts[3], itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#64748b' }, { offset: 1, color: '#94a3b8' }]) } },
            ],
            label: { show: true, position: "top", color: "#475569" },
          },
        ],
        grid: { left: "3%", right: "4%", bottom: "3%", containLabel: true },
        backgroundColor: "transparent",
      });
      window.addEventListener("resize", () => chart.resize());
    },
    categoryName(c) {
      return ["标件", "金工件", "元器件", "物资"][c] || "未知";
    },
    getExpiryClass(dateStr) {
      if (!dateStr) return "";
      const diff = (new Date(dateStr) - new Date()) / (1000 * 60 * 60 * 24);
      if (diff < 0) return "expired";
      if (diff <= 7) return "warning-7";
      return "warning-30";
    },
    getExpiryColor(dateStr) {
      if (!dateStr) return "";
      const diff = (new Date(dateStr) - new Date()) / (1000 * 60 * 60 * 24);
      if (diff < 0) return "#ef4444";
      if (diff <= 7) return "#f59e0b";
      return "#fbbf24";
    },
    getExpiryText(dateStr) {
      if (!dateStr) return "";
      const diff = Math.ceil((new Date(dateStr) - new Date()) / (1000 * 60 * 60 * 24));
      if (diff < 0) return "已过期 " + Math.abs(diff) + " 天";
      if (diff === 0) return "今日到期";
      return "剩余 " + diff + " 天";
    },
  },
};
</script>

<style scoped>
.section-title {
  margin-bottom: 16px;
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 600;
}

.stat-card {
  position: relative;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-icon-text {
  font-size: 22px;
}
</style>
