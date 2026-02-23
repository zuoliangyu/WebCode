<template>
  <div class="page-container">
    <div class="card-container">
      <el-form :inline="true" size="small">
        <el-form-item label="搜索">
          <el-input v-model="search" placeholder="姓名/用户名/工号" clearable @keyup.enter="load">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="search = ''; load()">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="card-container">
      <div style="margin-bottom: 12px">
        <el-button type="primary" size="small" @click="handleAdd">新增用户</el-button>
        <el-upload
            style="display: inline-block; margin-left: 10px"
            action=""
            :http-request="importUsers"
            accept=".xlsx,.xls"
            :show-file-list="false"
        >
          <el-button type="success" size="small">导入用户(xlsx)</el-button>
        </el-upload>
        <el-popconfirm title="确认批量禁用?" @confirm="deleteBatch">
          <template #reference>
            <el-button type="danger" size="small" style="margin-left: 10px">批量禁用</el-button>
          </template>
        </el-popconfirm>
      </div>

      <el-table :data="tableData" stripe border @selection-change="handleSelectionChange" style="width: 100%">
        <el-table-column type="selection" width="45" />
        <el-table-column prop="id" label="ID" width="60" sortable />
        <el-table-column prop="username" label="用户名" width="100" />
        <el-table-column prop="nickName" label="姓名" width="90" />
        <el-table-column prop="employeeId" label="工号" width="90" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="scope">
            <el-tag :type="roleTagType(scope.row.role)" size="small">{{ roleName(scope.row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" width="120" />
        <el-table-column prop="sex" label="性别" width="60" />
        <el-table-column prop="alow" label="状态" width="70">
          <template #default="scope">
            <el-tag :type="scope.row.alow === '1' ? 'success' : 'danger'" size="small">
              {{ scope.row.alow === '1' ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="240">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="warning" @click="handleResetPwd(scope.row.id)">重置密码</el-button>
            <el-button size="small" :type="scope.row.alow === '1' ? 'danger' : 'success'" @click="handleToggle(scope.row.id)">
              {{ scope.row.alow === '1' ? '禁用' : '启用' }}
            </el-button>
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

    <!-- 编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="450px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" v-if="!form.id">
          <el-input v-model="form.password" placeholder="默认 123456" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.nickName" />
        </el-form-item>
        <el-form-item label="工号">
          <el-input v-model="form.employeeId" />
        </el-form-item>
        <el-form-item label="角色" required>
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="仓库管理员" :value="2" />
            <el-option label="员工" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.sex">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" type="textarea" />
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
import router from "@/router";

export default {
  name: "User",
  data() {
    return {
      tableData: [],
      search: "",
      currentPage: 1,
      pageSize: 10,
      total: 0,
      dialogVisible: false,
      form: {},
      ids: [],
      user: {},
    };
  },
  created() {
    let userJson = sessionStorage.getItem("user");
    if (!userJson) {
      router.push("/login");
      return;
    }
    this.user = JSON.parse(userJson);
    this.load();
  },
  methods: {
    load() {
      request.get("/user", {
        params: {
          pageNum: this.currentPage,
          pageSize: this.pageSize,
          search: this.search,
        },
      }).then((res) => {
        if (res.code === "0") {
          this.tableData = res.data.records;
          this.total = res.data.total;
        }
      });
    },
    handleAdd() {
      this.form = { role: 3, password: "123456" };
      this.dialogVisible = true;
    },
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row));
      this.dialogVisible = true;
    },
    save() {
      if (!this.form.username) {
        ElMessage.warning("请填写用户名");
        return;
      }
      if (this.form.id) {
        request.put("/user/update", this.form).then((res) => {
          if (res.code === "0") {
            ElMessage.success("更新成功");
            this.dialogVisible = false;
            this.load();
          } else {
            ElMessage.error(res.msg);
          }
        });
      } else {
        request.post("/user", this.form).then((res) => {
          if (res.code === "0") {
            ElMessage.success("添加成功");
            this.dialogVisible = false;
            this.load();
          } else {
            ElMessage.error(res.msg);
          }
        });
      }
    },
    handleToggle(id) {
      request.put("/user/toggle/" + id).then((res) => {
        if (res.code === "0") {
          ElMessage.success("状态已切换");
          this.load();
        } else {
          ElMessage.error(res.msg);
        }
      });
    },
    handleResetPwd(id) {
      request.put("/user/resetPassword/" + id).then((res) => {
        if (res.code === "0") {
          ElMessage.success("密码已重置为 123456");
        } else {
          ElMessage.error(res.msg);
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
      request.post("/user/deleteBatch", this.ids).then((res) => {
        if (res.code === "0") {
          ElMessage.success("批量操作成功");
          this.load();
        }
      });
    },
    importUsers(param) {
      const formData = new FormData();
      formData.append("file", param.file);
      request.post("/user/import", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      }).then((res) => {
        if (res.code === "0") {
          ElMessage.success("导入完成: 成功 " + res.data.successCount + " 条, 跳过 " + res.data.skipCount + " 条");
          this.load();
        } else {
          ElMessage.error(res.msg);
        }
      });
    },
    roleName(role) {
      const map = { 1: "系统管理员", 2: "仓库管理员", 3: "员工" };
      return map[role] || "未知";
    },
    roleTagType(role) {
      const map = { 1: "danger", 2: "warning", 3: "" };
      return map[role] || "info";
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
