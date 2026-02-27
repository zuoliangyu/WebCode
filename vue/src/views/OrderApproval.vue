<template>
  <div class="page-container">
    <div class="card-container">
      <!-- 筛选 -->
      <el-form :inline="true" size="small" class="filter-form">
        <el-form-item label="状态">
          <el-select v-model="filterStatus" placeholder="全部" clearable>
            <el-option label="待审批" value="待审批" />
            <el-option label="已通过" value="已通过" />
            <el-option label="已拒绝" value="已拒绝" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="filterType" placeholder="全部" clearable>
            <el-option label="出库" value="出库" />
            <el-option label="退库" value="退库" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请人">
          <el-input v-model="filterName" placeholder="申请人姓名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="card-container">
      <el-table :data="tableData" stripe border style="width: 100%">
        <el-table-column prop="orderNumber" label="工单号" width="170" />
        <el-table-column prop="orderType" label="类型" width="65">
          <template #default="scope">
            <el-tag :type="scope.row.orderType === '出库' ? 'warning' : 'success'" size="small">
              {{ scope.row.orderType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="80" />
        <el-table-column prop="employeeId" label="工号" width="75" />
        <el-table-column prop="productName" label="物料" min-width="100" />
        <el-table-column prop="drawingNumber" label="图号" width="80" />
        <el-table-column prop="quantity" label="数量" width="60" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="statusTag(scope.row.status)" size="small">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="155" />
        <el-table-column fixed="right" label="操作" width="155">
          <template #default="scope">
            <template v-if="scope.row.status === '待审批'">
              <el-button type="success" size="small" @click="approve(scope.row)">通过</el-button>
              <el-button type="danger" size="small" @click="openReject(scope.row)">拒绝</el-button>
            </template>
            <template v-else>
              <el-button size="small" @click="viewDetail(scope.row)">详情</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

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

    <!-- 拒绝对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝工单" width="400px">
      <el-form label-width="80px">
        <el-form-item label="拒绝原因" required>
          <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="工单详情" width="500px">
      <el-descriptions :column="2" border v-if="detailOrder">
        <el-descriptions-item label="工单号" :span="2">{{ detailOrder.orderNumber }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detailOrder.orderType }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detailOrder.status }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ detailOrder.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="工号">{{ detailOrder.employeeId }}</el-descriptions-item>
        <el-descriptions-item label="物料">{{ detailOrder.productName }}</el-descriptions-item>
        <el-descriptions-item label="图号">{{ detailOrder.drawingNumber }}</el-descriptions-item>
        <el-descriptions-item label="套数">{{ detailOrder.numberOfSets }}</el-descriptions-item>
        <el-descriptions-item label="申请数量">{{ detailOrder.quantity }}</el-descriptions-item>
        <el-descriptions-item label="实际数量">{{ detailOrder.actualDeliveryQuantity }}</el-descriptions-item>
        <el-descriptions-item label="用途" :span="2">{{ detailOrder.purpose }}</el-descriptions-item>
        <el-descriptions-item label="退库去向" :span="2" v-if="detailOrder.orderType === '退库'">{{ detailOrder.returnDestination }}</el-descriptions-item>
        <el-descriptions-item label="关联工单" :span="2" v-if="detailOrder.relatedOrderNumber">{{ detailOrder.relatedOrderNumber }}</el-descriptions-item>
        <el-descriptions-item label="拒绝原因" :span="2" v-if="detailOrder.rejectReason">{{ detailOrder.rejectReason }}</el-descriptions-item>
        <el-descriptions-item label="审批人">{{ detailOrder.approverName }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ detailOrder.completeTime }}</el-descriptions-item>
        <el-descriptions-item label="申请时间" :span="2">{{ detailOrder.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import request from "@/utils/request";
import { ElMessage } from "element-plus";

export default {
  name: "OrderApproval",
  data() {
    return {
      tableData: [],
      filterStatus: "",
      filterType: "",
      filterName: "",
      currentPage: 1,
      pageSize: 10,
      total: 0,
      rejectDialogVisible: false,
      rejectReason: "",
      currentRejectOrder: null,
      detailDialogVisible: false,
      detailOrder: null,
    };
  },
  created() {
    this.load();
  },
  methods: {
    load() {
      const user = JSON.parse(sessionStorage.getItem("user") || "{}");
      request.get("/workorder/admin/all", {
        params: {
          pageNum: this.currentPage,
          pageSize: this.pageSize,
          status: this.filterStatus,
          orderType: this.filterType,
          applicantName: this.filterName,
        },
        headers: { token: user.token },
      }).then((res) => {
        if (res.code === "0") {
          this.tableData = res.data.records;
          this.total = res.data.total;
        }
      });
    },
    resetFilter() {
      this.filterStatus = "";
      this.filterType = "";
      this.filterName = "";
      this.load();
    },
    approve(row) {
      const user = JSON.parse(sessionStorage.getItem("user") || "{}");
      request.post("/workorder/admin/approve",
          { orderId: row.id, actualQuantity: row.quantity },
          { headers: { token: user.token } }
      ).then((res) => {
        if (res.code === "0") {
          ElMessage.success("审批通过");
          this.load();
        } else {
          ElMessage.error(res.msg);
        }
      });
    },
    openReject(row) {
      this.currentRejectOrder = row;
      this.rejectReason = "";
      this.rejectDialogVisible = true;
    },
    confirmReject() {
      if (!this.rejectReason) {
        ElMessage.warning("请填写拒绝原因");
        return;
      }
      const user = JSON.parse(sessionStorage.getItem("user") || "{}");
      request.post("/workorder/admin/reject",
          { orderId: this.currentRejectOrder.id, rejectReason: this.rejectReason },
          { headers: { token: user.token } }
      ).then((res) => {
        if (res.code === "0") {
          ElMessage.success("已拒绝");
          this.rejectDialogVisible = false;
          this.load();
        } else {
          ElMessage.error(res.msg);
        }
      });
    },
    viewDetail(row) {
      this.detailOrder = row;
      this.detailDialogVisible = true;
    },
    statusTag(status) {
      const map = { "待审批": "warning", "已通过": "success", "已拒绝": "danger" };
      return map[status] || "info";
    },
    handleSizeChange(size) {
      this.pageSize = size;
      this.load();
    },
    handleCurrentChange(page) {
      this.currentPage = page;
      this.load();
    },
  },
};
</script>
