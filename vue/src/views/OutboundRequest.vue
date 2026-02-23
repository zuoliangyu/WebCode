<template>
  <div class="page-container">
    <div class="card-container">
      <h3 style="margin-bottom: 20px; color: #303133">出库申请</h3>
      <el-form :model="form" label-width="100px" style="max-width: 600px">
        <el-form-item label="选择物料" required>
          <el-select v-model="form.productId" filterable placeholder="请搜索选择物料" style="width: 100%" @change="onMaterialChange">
            <el-option
                v-for="item in materials"
                :key="item.id"
                :label="item.name + ' (' + item.specification + ')'"
                :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="物料信息" v-if="selectedMaterial">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="库存">{{ selectedMaterial.totalQuantity }} {{ selectedMaterial.unit }}</el-descriptions-item>
            <el-descriptions-item label="单套用量">{{ selectedMaterial.perSetQuantity }} {{ selectedMaterial.unit }}</el-descriptions-item>
            <el-descriptions-item label="分类">{{ categoryName(selectedMaterial.category) }}</el-descriptions-item>
            <el-descriptions-item label="存放位置">{{ selectedMaterial.storageLocation }}</el-descriptions-item>
          </el-descriptions>
        </el-form-item>

        <el-form-item label="图号" required>
          <el-input v-model="form.drawingNumber" placeholder="请输入图号" />
        </el-form-item>

        <el-form-item label="套数" required>
          <el-input-number v-model="form.numberOfSets" :min="1" @change="calcQuantity" />
        </el-form-item>

        <el-form-item label="申请数量">
          <el-tag type="info" size="large">
            {{ computedQuantity }} {{ selectedMaterial ? selectedMaterial.unit : '个' }}
          </el-tag>
          <span style="color: #909399; margin-left: 10px; font-size: 12px">
            = 套数({{ form.numberOfSets }}) × 单套用量({{ selectedMaterial ? selectedMaterial.perSetQuantity : 0 }})
          </span>
        </el-form-item>

        <el-form-item label="用途说明">
          <el-input v-model="form.purpose" type="textarea" :rows="3" placeholder="请输入用途说明" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submit" :loading="submitting">提交申请</el-button>
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
  name: "OutboundRequest",
  data() {
    return {
      form: { numberOfSets: 1 },
      materials: [],
      selectedMaterial: null,
      submitting: false,
    };
  },
  computed: {
    computedQuantity() {
      if (!this.selectedMaterial) return 0;
      return (this.form.numberOfSets || 1) * (this.selectedMaterial.perSetQuantity || 0);
    },
  },
  created() {
    this.loadMaterials();
  },
  methods: {
    loadMaterials() {
      request.get("/material/all").then((res) => {
        if (res.code === "0") {
          this.materials = res.data;
        }
      });
    },
    onMaterialChange(id) {
      this.selectedMaterial = this.materials.find((m) => m.id === id) || null;
    },
    calcQuantity() {
      // 触发 computed 更新
    },
    categoryName(c) {
      return ["标件", "金工件", "元器件", "物资"][c] || "未知";
    },
    submit() {
      if (!this.form.productId) {
        ElMessage.warning("请选择物料");
        return;
      }
      if (!this.form.drawingNumber) {
        ElMessage.warning("请输入图号");
        return;
      }

      this.submitting = true;
      const user = JSON.parse(sessionStorage.getItem("user") || "{}");

      request.post("/workorder/create", this.form, {
        headers: { token: user.token }
      }).then((res) => {
        this.submitting = false;
        if (res.code === "0") {
          ElMessage.success("出库申请提交成功，工单号: " + res.data.orderNumber);
          this.resetForm();
        } else {
          ElMessage.error(res.msg);
        }
      }).catch(() => {
        this.submitting = false;
      });
    },
    resetForm() {
      this.form = { numberOfSets: 1 };
      this.selectedMaterial = null;
    },
  },
};
</script>
