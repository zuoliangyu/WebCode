# 总成车间物料管理系统

基于 SpringBoot + Vue 3 + MySQL 的装配车间物料管理系统，支持物料台账管理、出库/退库工单流转、多角色权限控制。

## 技术栈

| 层次 | 技术 |
|------|------|
| 后端 | SpringBoot 2.6、MyBatis-Plus 3.5、MySQL 8.0、Redis、JWT |
| 前端 | Vue 3、Vue Router 4、Element Plus、ECharts、Axios |
| 部署 | Docker Compose（MySQL + Redis + SpringBoot + Nginx） |

## 角色与权限

| 角色 | role 值 | 可用功能 |
|------|---------|----------|
| 系统管理员 | 1 | 数据概览、物料管理、工单审批、用户管理（含 xlsx 批量导入）、个人设置 |
| 仓库管理员 | 2 | 数据概览、物料管理、工单审批、个人设置 |
| 员工 | 3 | 数据概览、出库申请、退库申请、我的工单、个人设置 |

## 核心功能

- **物料管理** — 增删改查、分类筛选（标件/金工件/元器件/物资）、库存预警、有效期预警
- **出库申请** — 选择物料 → 输入图号 → 选套数 → 自动计算数量（单套用量 x 套数）→ 提交
- **退库申请** — 选择已通过的出库工单 → 输入退库数量 → 填写退库去向 → 提交
- **工单审批** — 通过（自动扣/加库存）或拒绝（填写理由）
- **我的工单** — 员工查看自己所有工单及审批状态
- **用户管理** — 用户增删改查、角色分配、xlsx 批量导入、账号禁用/启用、密码重置
- **数据概览** — 统计卡片、有效期预警、分类柱状图
- **通用** — 注册/登录（图形验证码）、个人信息编辑、修改密码、忘记密码重置

## 项目结构

```
WebCode/
├── SpringBoot/                  # 后端
│   └── src/main/java/com/example/demo/
│       ├── controller/
│       │   ├── MaterialController.java    # 物料管理接口
│       │   ├── WorkOrderController.java   # 工单接口
│       │   ├── UserController.java        # 用户接口（含 xlsx 导入）
│       │   ├── DashboardController.java   # 数据统计接口
│       │   └── ForgetController.java      # 密码重置接口
│       ├── entity/
│       │   ├── Material.java     # 物料实体
│       │   ├── WorkOrder.java    # 工单实体
│       │   └── User.java         # 用户实体
│       ├── mapper/               # MyBatis-Plus Mapper
│       ├── service/              # 业务逻辑层
│       ├── commom/Result.java    # 统一响应封装
│       └── utils/TokenUtils.java # JWT 工具类
├── vue/                          # 前端
│   └── src/
│       ├── views/
│       │   ├── Dashboard.vue         # 数据概览
│       │   ├── Material.vue          # 物料管理
│       │   ├── OutboundRequest.vue   # 出库申请
│       │   ├── ReturnRequest.vue     # 退库申请
│       │   ├── OrderApproval.vue     # 工单审批
│       │   ├── MyOrders.vue          # 我的工单
│       │   ├── User.vue              # 用户管理
│       │   ├── Person.vue            # 个人信息
│       │   ├── Password.vue          # 修改密码
│       │   ├── Login.vue             # 登录
│       │   ├── Register.vue          # 注册
│       │   └── Forget.vue            # 密码重置
│       ├── components/               # 公共组件
│       ├── layout/Layout.vue         # 页面布局
│       ├── router/index.js           # 路由配置
│       └── utils/request.js          # Axios 封装
├── docker/                       # Docker 配置
│   ├── nginx.conf                # Nginx 反向代理配置
│   └── init.sql                  # 数据库初始化
├── docker-compose.yml            # Docker Compose 编排
└── springboot-vue.sql            # 数据库建表与初始数据
```

## 数据库表

| 表名 | 说明 |
|------|------|
| user | 用户表（用户名、姓名、工号、角色、账号状态等） |
| material | 物料表（名称、分类、库存、有效期、图号、规格、单位等） |
| work_order | 工单表（出库/退库类型、审批状态、关联工单号等） |
| operation_log | 操作日志表 |

## 启动说明

### Docker 部署（推荐）

```bash
docker compose up -d --build

# 前端: http://localhost:9876
# 后端 API: http://localhost:9090
```

### 本地开发

**后端：**

1. 启动 MySQL，执行 `springboot-vue.sql` 初始化数据库
2. 启动 Redis
3. 修改 `SpringBoot/src/main/resources/application.properties` 中的数据库和 Redis 连接
4. 运行 SpringBoot 启动类

**前端：**

```bash
cd vue
npm install --legacy-peer-deps
npm run serve     # 开发模式，默认 http://localhost:8080
npm run build     # 生产构建，输出到 dist/
```

## 测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 系统管理员 |
| warehouse | 123456 | 仓库管理员 |
| worker | 123456 | 员工 |
