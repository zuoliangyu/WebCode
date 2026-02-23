<template>
  <div class="page-container">
    <div class="card-container">
      <!-- 搜索栏 -->
      <el-form :inline="true" size="small">
        <el-form-item label="搜索">
          <el-input v-model="search" placeholder="名称/厂家/图号" clearable @keyup.enter="load">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="category" placeholder="全部" clearable>
            <el-option label="标件" :value="0" />
            <el-option label="金工件" :value="1" />
            <el-option label="元器件" :value="2" />
            <el-option label="物资" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="card-container">
      <!-- 操作按钮 -->
      <div style="margin-bottom: 12px">
        <el-button type="primary" @click="handleAdd" size="small">新增物料</el-button>
        <el-popconfirm title="确认批量删除?" @confirm="deleteBatch">
          <template #reference>
            <el-button type="danger" size="small">批量删除</el-button>
          </template>
        </el-popconfirm>
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" stripe border @selection-change="handleSelectionChange"
                :row-class-name="tableRowClassName" style="width: 100%">
        <el-table-column type="selection" width="45" />
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column prop="category" label="分类" width="80">
          <template #default="scope">
            <el-tag :type="categoryTag(scope.row.category)" size="small">
              {{ categoryName(scope.row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="specification" label="规格型号" min-width="100" />
        <el-table-column prop="drawingNumber" label="图号" width="90" />
        <el-table-column prop="manufacturer" label="厂家" width="100" />
        <el-table-column prop="totalQuantity" label="库存" width="70">
          <template #default="scope">
            <span :style="{ color: scope.row.totalQuantity <= scope.row.stockAlertThreshold ? '#F56C6C' : '' }">
              {{ scope.row.totalQuantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="perSetQuantity" label="单套用量" width="80" />
        <el-table-column prop="unit" label="单位" width="55" />
        <el-table-column prop="storageLocation" label="存放位置" width="90" />
        <el-table-column prop="expiryDate" label="有效期" width="100">
          <template #default="scope">
            <span :style="{ color: isExpiringSoon(scope.row.expiryDate) ? '#F56C6C' : '' }">
              {{ scope.row.expiryDate }}
            </span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="140">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-popconfirm title="确认删除?" @confirm="handleDelete(scope.row.id)">
              <template #reference>
                <el-button type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div style="margin-top: 12px">
        <el-pagination
            v-model:currentPage="currentPage"
            :page-sizes="[10, 20, 50]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑物料' : '新增物料'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="名称" required>
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" required>
              <el-select v-model="form.category" style="width:100%">
                <el-option label="标件" :value="0" />
                <el-option label="金工件" :value="1" />
                <el-option label="元器件" :value="2" />
                <el-option label="物资" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="规格型号">
              <el-input v-model="form.specification" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="图号">
              <el-input v-model="form.drawingNumber" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="厂家">
              <el-input v-model="form.manufacturer" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位">
              <el-input v-model="form.unit" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="总数量">
              <el-input-number v-model="form.totalQuantity" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单套用量">
              <el-input-number v-model="form.perSetQuantity" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="预警阈值">
              <el-input-number v-model="form.stockAlertThreshold" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="有效期">
              <el-date-picker v-model="form.expiryDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="存放位置">
          <el-input v-model="form.storageLocation" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import request from "@/utils/request";
import { ElMessage } from "element-plus";

export default {
  name: "Material",
  data() {
    return {
      tableData: [],
      search: "",
      category: null,
      currentPage: 1,
      pageSize: 10,
      total: 0,
      dialogVisible: false,
      form: {},
      ids: [],
    };
  },
  created() {
    this.load();
  },
  methods: {
    load() {
      request.get("/material", {
        params: {
          pageNum: this.currentPage,
          pageSize: this.pageSize,
          search: this.search,
          category: this.category,
        },
      }).then((res) => {
        if (res.code === "0") {
          this.tableData = res.data.records;
          this.total = res.data.total;
        }
      });
    },
    resetSearch() {
      this.search = "";
      this.category = null;
      this.load();
    },
    handleAdd() {
      this.form = { unit: "个", stockAlertThreshold: 10 };
      this.dialogVisible = true;
    },
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row));
      this.dialogVisible = true;
    },
    save() {
      if (!this.form.name) {
        ElMessage.warning("请填写物料名称");
        return;
      }
      const req = this.form.id
          ? request.put("/material", this.form)
          : request.post("/material", this.form);
      req.then((res) => {
        if (res.code === "0") {
          ElMessage.success(this.form.id ? "更新成功" : "添加成功");
          this.dialogVisible = false;
          this.load();
        } else {
          ElMessage.error(res.msg);
        }
      });
    },
    handleDelete(id) {
      request.delete("/material/" + id).then((res) => {
        if (res.code === "0") {
          ElMessage.success("删除成功");
          this.load();
        }
      });
    },
    handleSelectionChange(val) {
      this.ids = val.map((v) => v.id);
    },
    deleteBatch() {
      if (!this.ids.length) {
        ElMessage.warning("请选择数据");
        return;
      }
      request.post("/material/deleteBatch", this.ids).then((res) => {
        if (res.code === "0") {
          ElMessage.success("批量删除成功");
          this.load();
        }
      });
    },
    handleSizeChange(size) {
      this.pageSize = size;
      this.load();
    },
    handleCurrentChange(page) {
      this.currentPage = page;
      this.load();
    },
    categoryName(c) {
      return ["标件", "金工件", "元器件", "物资"][c] || "未知";
    },
    categoryTag(c) {
      return ["", "warning", "success", "info"][c] || "";
    },
    isExpiringSoon(dateStr) {
      if (!dateStr) return false;
      const diff = (new Date(dateStr) - new Date()) / (1000 * 60 * 60 * 24);
      return diff <= 30;
    },
    tableRowClassName({ row }) {
      if (row.expiryDate) {
        const diff = (new Date(row.expiryDate) - new Date()) / (1000 * 60 * 60 * 24);
        if (diff < 0) return "expired-row";
        if (diff <= 7) return "warning-row";
        if (diff <= 30) return "caution-row";
      }
      return "";
    },
  },
};
</script>

<style>
.el-table .expired-row { background-color: #FEF0F0 !important; }
.el-table .warning-row { background-color: #FDF6EC !important; }
.el-table .caution-row { background-color: #FEFCE8 !important; }
</style>
