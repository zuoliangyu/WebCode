<template>
  <div class="page-container">
    <div class="card-container">
      <h3 class="section-title">退库申请</h3>
      <el-form :model="form" label-width="100px" style="max-width: 600px">
        <el-form-item label="原出库工单" required>
          <el-select v-model="form.relatedOrderNumber" filterable placeholder="请选择已通过的出库工单" style="width: 100%" @change="onOrderChange">
            <el-option
                v-for="item in approvedOrders"
                :key="item.orderNumber"
                :label="item.orderNumber + ' - ' + item.productName + ' (数量:' + (item.actualDeliveryQuantity || item.quantity) + ')'"
                :value="item.orderNumber"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="原工单信息" v-if="selectedOrder">
          <div class="order-info-card">
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="物料名称">{{ selectedOrder.productName }}</el-descriptions-item>
              <el-descriptions-item label="出库数量">{{ selectedOrder.actualDeliveryQuantity || selectedOrder.quantity }}</el-descriptions-item>
              <el-descriptions-item label="图号">{{ selectedOrder.drawingNumber }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ selectedOrder.createTime }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-form-item>

        <el-form-item label="退库数量" required>
          <el-input-number v-model="form.quantity" :min="1" :max="maxReturnQty" />
          <span style="color: #94a3b8; margin-left: 10px; font-size: 12px" v-if="selectedOrder">
            最大可退: {{ maxReturnQty }}
          </span>
        </el-form-item>

        <el-form-item label="退库去向" required>
          <el-input v-model="form.returnDestination" placeholder="请填写退库去向（如：退回仓库A区）" />
        </el-form-item>

        <el-form-item label="说明">
          <el-input v-model="form.purpose" type="textarea" :rows="3" placeholder="请输入退库原因或说明" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submit" :loading="submitting">提交退库申请</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import request from "@/utils/request";
import { ElMessage } from "element-plus";

export default {
  name: "ReturnRequest",
  data() {
    return {
      form: { quantity: 1 },
      approvedOrders: [],
      selectedOrder: null,
      submitting: false,
    };
  },
  computed: {
    maxReturnQty() {
      if (!this.selectedOrder) return 1;
      return this.selectedOrder.actualDeliveryQuantity || this.selectedOrder.quantity || 1;
    },
  },
  created() {
    this.loadApprovedOrders();
  },
  methods: {
    loadApprovedOrders() {
      const user = JSON.parse(sessionStorage.getItem("user") || "{}");
      request.get("/workorder/approved-outbound", {
        headers: { token: user.token }
      }).then((res) => {
        if (res.code === "0") {
          this.approvedOrders = res.data;
        }
      });
    },
    onOrderChange(orderNumber) {
      this.selectedOrder = this.approvedOrders.find((o) => o.orderNumber === orderNumber) || null;
      if (this.selectedOrder) {
        this.form.quantity = 1;
      }
    },
    submit() {
      if (!this.form.relatedOrderNumber) {
        ElMessage.warning("请选择原出库工单");
        return;
      }
      if (!this.form.quantity || this.form.quantity <= 0) {
        ElMessage.warning("请输入有效的退库数量");
        return;
      }
      if (!this.form.returnDestination) {
        ElMessage.warning("请填写退库去向");
        return;
      }

      this.submitting = true;
      const user = JSON.parse(sessionStorage.getItem("user") || "{}");

      request.post("/workorder/return", this.form, {
        headers: { token: user.token }
      }).then((res) => {
        this.submitting = false;
        if (res.code === "0") {
          ElMessage.success("退库申请提交成功，工单号: " + res.data.orderNumber);
          this.resetForm();
          this.loadApprovedOrders();
        } else {
          ElMessage.error(res.msg);
        }
      }).catch(() => {
        this.submitting = false;
      });
    },
    resetForm() {
      this.form = { quantity: 1 };
      this.selectedOrder = null;
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

.order-info-card {
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  padding: 4px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}
</style>
