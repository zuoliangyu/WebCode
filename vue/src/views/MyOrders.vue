<template>
  <div class="page-container">
    <div class="card-container">
      <el-form :inline="true" size="small">
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
        <el-table-column prop="productName" label="物料名称" min-width="120" />
        <el-table-column prop="drawingNumber" label="图号" width="90" />
        <el-table-column prop="numberOfSets" label="套数" width="60" />
        <el-table-column prop="quantity" label="数量" width="60" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="statusTag(scope.row.status)" size="small">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="approverName" label="审批人" width="80" />
        <el-table-column prop="createTime" label="申请时间" width="155" />
        <el-table-column fixed="right" label="操作" width="80">
          <template #default="scope">
            <el-button size="small" @click="viewDetail(scope.row)">详情</el-button>
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

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="工单详情" width="500px">
      <el-descriptions :column="2" border v-if="detailOrder">
        <el-descriptions-item label="工单号" :span="2">{{ detailOrder.orderNumber }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detailOrder.orderType }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detailOrder.status }}</el-descriptions-item>
        <el-descriptions-item label="物料">{{ detailOrder.productName }}</el-descriptions-item>
        <el-descriptions-item label="图号">{{ detailOrder.drawingNumber }}</el-descriptions-item>
        <el-descriptions-item label="套数">{{ detailOrder.numberOfSets }}</el-descriptions-item>
        <el-descriptions-item label="单套用量">{{ detailOrder.perSetQuantity }}</el-descriptions-item>
        <el-descriptions-item label="申请数量">{{ detailOrder.quantity }}</el-descriptions-item>
        <el-descriptions-item label="实际数量">{{ detailOrder.actualDeliveryQuantity }}</el-descriptions-item>
        <el-descriptions-item label="用途" :span="2">{{ detailOrder.purpose }}</el-descriptions-item>
        <el-descriptions-item label="退库去向" :span="2" v-if="detailOrder.orderType === '退库'">{{ detailOrder.returnDestination }}</el-descriptions-item>
        <el-descriptions-item label="关联工单" :span="2" v-if="detailOrder.relatedOrderNumber">{{ detailOrder.relatedOrderNumber }}</el-descriptions-item>
        <el-descriptions-item label="拒绝原因" :span="2" v-if="detailOrder.rejectReason">
          <span style="color: #F56C6C">{{ detailOrder.rejectReason }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="审批人">{{ detailOrder.approverName }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ detailOrder.completeTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import request from "@/utils/request";

export default {
  name: "MyOrders",
  data() {
    return {
      tableData: [],
      filterStatus: "",
      filterType: "",
      currentPage: 1,
      pageSize: 10,
      total: 0,
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
      request.get("/workorder/myorders", {
        params: {
          pageNum: this.currentPage,
          pageSize: this.pageSize,
          status: this.filterStatus,
          orderType: this.filterType,
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
      this.load();
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
