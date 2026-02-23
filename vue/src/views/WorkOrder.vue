<template>
  <div>
    <div style="margin: 10px 0;">
      <el-button type="primary" @click="add" v-if="user.role === 2">申请工单</el-button>
      <el-button type="success" @click="refresh" v-if="user.role === 1">刷新工单</el-button>
    </div>

    <!-- 工单搜索 -->
    <div style="margin: 10px 0;">
      <el-input v-model="searchStatus" placeholder="按状态搜索" style="width: 200px;" clearable></el-input>
      <el-input v-model="searchApplicant" placeholder="按申请人搜索" style="width: 200px; margin-left: 5px;" clearable></el-input>
      <el-button type="primary" style="margin-left: 5px;" @click="load">搜索</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <!-- 工单表格 -->
    <el-table :data="tableData" stripe border>
      <el-table-column prop="orderNumber" label="工单号" width="150"></el-table-column>
      <el-table-column prop="productName" label="物品名称"></el-table-column>
      <el-table-column prop="productType" label="物品类型"></el-table-column>
      <el-table-column prop="model" label="型号"></el-table-column>
      <el-table-column prop="batch" label="批次"></el-table-column>
      <el-table-column prop="quantity" label="申请数量"></el-table-column>
      <el-table-column prop="applicantName" label="申请人"></el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.status === '待处理'" type="warning">{{ scope.row.status }}</el-tag>
          <el-tag v-else-if="scope.row.status === '已完成'" type="success">{{ scope.row.status }}</el-tag>
          <el-tag v-else-if="scope.row.status === '已拒绝'" type="danger">{{ scope.row.status }}</el-tag>
          <el-tag v-else>{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
      <el-table-column fixed="right" label="操作" width="200">
        <template #default="scope">
          <el-button size="mini" @click="viewDetail(scope.row)">查看详情</el-button>
          <el-button size="mini" @click="approveOrder(scope.row)" v-if="user.role === 1 && scope.row.status === '待处理'">审批</el-button>
          <el-button size="mini" @click="rejectOrder(scope.row)" v-if="user.role === 1 && scope.row.status === '待处理'">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div style="margin: 10px 0;">
      <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[5, 10, 20]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total">
      </el-pagination>
    </div>

    <!-- 创建工单对话框 -->
    <el-dialog title="申请工单" v-model="dialogFormVisible" width="50%">
      <el-form :model="form" label-width="120px">
        <el-form-item label="物品名称">
          <el-select v-model="form.productId" placeholder="请选择物品" @change="onProductChange">
            <el-option
                v-for="item in products"
                :key="item.ID"
                :label="item.name"
                :value="item.ID">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="物品类型">
          <el-input v-model="form.productType" disabled></el-input>
        </el-form-item>
        <el-form-item label="型号">
          <el-input v-model="form.model"></el-input>
        </el-form-item>
        <el-form-item label="批次">
          <el-input v-model="form.batch"></el-input>
        </el-form-item>
        <el-form-item label="申请数量">
          <el-input-number v-model="form.quantity" :min="1" :max="9999"></el-input-number>
          <span style="margin-left: 10px;">当前库存: {{ currentStock }}</span>
        </el-form-item>
        <el-form-item label="用途说明">
          <el-input type="textarea" v-model="form.purpose"></el-input>
        </el-form-item>
        <el-form-item label="期望配送时间">
          <el-date-picker
              v-model="form.expectedDeliveryTime"
              type="datetime"
              placeholder="选择日期时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss">
          </el-date-picker>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogFormVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog title="审批工单" v-model="approveDialogVisible" width="40%">
      <el-form :model="approveForm" label-width="120px">
        <el-form-item label="工单号">
          <el-input v-model="currentOrder.orderNumber" disabled></el-input>
        </el-form-item>
        <el-form-item label="物品名称">
          <el-input v-model="currentOrder.productName" disabled></el-input>
        </el-form-item>
        <el-form-item label="申请数量">
          <el-input v-model="currentOrder.quantity" disabled></el-input>
        </el-form-item>
        <el-form-item label="实际配送数量">
          <el-input-number v-model="approveForm.actualQuantity" :min="0" :max="currentOrder.quantity"></el-input-number>
        </el-form-item>
        <el-form-item label="配送人">
          <el-input v-model="approveForm.deliveryPerson"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="approveDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="doApprove">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 拒绝对话框 -->
    <el-dialog title="拒绝工单" v-model="rejectDialogVisible" width="40%">
      <el-form :model="rejectForm" label-width="120px">
        <el-form-item label="工单号">
          <el-input v-model="currentOrder.orderNumber" disabled></el-input>
        </el-form-item>
        <el-form-item label="拒绝理由">
          <el-input type="textarea" v-model="rejectForm.rejectReason" placeholder="请输入拒绝理由"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取 消</el-button>
          <el-button type="danger" @click="doReject">拒 绝</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 工单详情对话框 -->
    <el-dialog title="工单详情" v-model="detailDialogVisible" width="50%">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="工单号">{{ detailData.orderNumber }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="detailData.status === '待处理'" type="warning">{{ detailData.status }}</el-tag>
          <el-tag v-else-if="detailData.status === '已完成'" type="success">{{ detailData.status }}</el-tag>
          <el-tag v-else-if="detailData.status === '已拒绝'" type="danger">{{ detailData.status }}</el-tag>
          <el-tag v-else>{{ detailData.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请人">{{ detailData.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="物品名称">{{ detailData.productName }}</el-descriptions-item>
        <el-descriptions-item label="物品类型">{{ detailData.productType }}</el-descriptions-item>
        <el-descriptions-item label="型号">{{ detailData.model }}</el-descriptions-item>
        <el-descriptions-item label="批次">{{ detailData.batch }}</el-descriptions-item>
        <el-descriptions-item label="申请数量">{{ detailData.quantity }}</el-descriptions-item>
        <el-descriptions-item label="实际配送数量" v-if="detailData.actualDeliveryQuantity">{{ detailData.actualDeliveryQuantity }}</el-descriptions-item>
        <el-descriptions-item label="配送人" v-if="detailData.deliveryPerson">{{ detailData.deliveryPerson }}</el-descriptions-item>
        <el-descriptions-item label="用途说明" :span="2">{{ detailData.purpose }}</el-descriptions-item>
        <el-descriptions-item label="期望配送时间">{{ detailData.expectedDeliveryTime }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="完成时间" v-if="detailData.completeTime">{{ detailData.completeTime }}</el-descriptions-item>
        <el-descriptions-item label="拒绝理由" v-if="detailData.rejectReason" :span="2">
          <el-alert :title="detailData.rejectReason" type="error" :closable="false"></el-alert>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关 闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import request from "@/utils/request";
import {ElMessage} from "element-plus";

export default {
  name: "WorkOrder",
  data() {
    return {
      form: {},
      approveForm: {},
      rejectForm: {},
      searchStatus: '',
      searchApplicant: '',
      total: 0,
      currentPage: 1,
      pageSize: 10,
      tableData: [],
      user: {},
      dialogFormVisible: false,
      approveDialogVisible: false,
      rejectDialogVisible: false,
      detailDialogVisible: false,
      currentOrder: {},
      detailData: {},
      products: [],
      currentStock: 0
    }
  },
  created() {
    this.load();
    this.loadUserInfo();
    this.loadProducts();
  },
  methods: {
    load() {
      let url = '/workorder/myorders';
      if (this.user.role === 1) {
        url = '/workorder/admin/all';
      }
      
      request.get(url, {
        params: {
          pageNum: this.currentPage,
          pageSize: this.pageSize,
          status: this.searchStatus,
          applicantName: this.searchApplicant
        }
      }).then(res => {
        if (res.code === '0') {
          this.tableData = res.data.records;
          this.total = res.data.total;
        } else {
          ElMessage.error(res.msg);
        }
      });
    },
    loadUserInfo() {
      let str = sessionStorage.getItem("user") || "{}";
      this.user = JSON.parse(str);
    },
    loadProducts() {
      request.get('/productbase', {
        params: {
          pageNum: 1,
          pageSize: 1000
        }
      }).then(res => {
        if (res.code === '0') {
          this.products = res.data.records;
        }
      });
    },
    onProductChange(productId) {
      const product = this.products.find(p => p.ID === productId);
      if (product) {
        this.form.productType = this.getCategoryName(product.Category);
        this.currentStock = product.StockQuantity || 0;
      }
    },
    getCategoryName(categoryId) {
      const categories = {
        0: '金工',
        1: '元器件',
        2: '仪器仪表',
        3: '工装'
      };
      return categories[categoryId] || '未知';
    },
    reset() {
      this.searchStatus = '';
      this.searchApplicant = '';
      this.load();
    },
    refresh() {
      this.load();
    },
    add() {
      this.dialogFormVisible = true;
      this.form = {};
      this.currentStock = 0;
    },
    save() {
      request.post('/workorder/create', this.form).then(res => {
        if (res.code === '0') {
          ElMessage.success('工单创建成功');
          this.dialogFormVisible = false;
          this.load();
        } else {
          ElMessage.error(res.msg);
        }
      });
    },
    viewDetail(row) {
      this.detailData = row;
      this.detailDialogVisible = true;
    },
    approveOrder(row) {
      this.currentOrder = row;
      this.approveForm = {
        actualQuantity: row.quantity,
        deliveryPerson: ''
      };
      this.approveDialogVisible = true;
    },
    doApprove() {
      request.post('/workorder/admin/approve', {
        orderId: this.currentOrder.id,
        actualQuantity: this.approveForm.actualQuantity,
        deliveryPerson: this.approveForm.deliveryPerson
      }).then(res => {
        if (res.code === '0') {
          ElMessage.success('工单审批成功');
          this.approveDialogVisible = false;
          this.load();
        } else {
          ElMessage.error(res.msg);
        }
      });
    },
    rejectOrder(row) {
      this.currentOrder = row;
      this.rejectForm = {
        rejectReason: ''
      };
      this.rejectDialogVisible = true;
    },
    doReject() {
      if (!this.rejectForm.rejectReason) {
        ElMessage.warning('请输入拒绝理由');
        return;
      }
      
      request.post('/workorder/admin/reject', {
        orderId: this.currentOrder.id,
        rejectReason: this.rejectForm.rejectReason
      }).then(res => {
        if (res.code === '0') {
          ElMessage.success('工单拒绝成功');
          this.rejectDialogVisible = false;
          this.load();
        } else {
          ElMessage.error(res.msg);
        }
      });
    },
    handleSizeChange(val) {
      this.pageSize = val;
      this.load();
    },
    handleCurrentChange(val) {
      this.currentPage = val;
      this.load();
    }
  }
}
</script>
