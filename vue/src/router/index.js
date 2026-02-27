import { createRouter, createWebHistory } from "vue-router";
import Layout from "../layout/Layout";
import { ElMessage } from "element-plus";

const routes = [
    {
        path: "/",
        name: "Layout",
        redirect: "dashboard",
        component: Layout,
        children: [
            {
                path: "dashboard",
                name: "Dashboard",
                component: () => import("@/views/Dashboard"),
            },
            {
                path: "material",
                name: "Material",
                meta: { roles: [1, 2] },
                component: () => import("@/views/Material"),
            },
            {
                path: "outbound",
                name: "OutboundRequest",
                meta: { roles: [3] },
                component: () => import("@/views/OutboundRequest"),
            },
            {
                path: "return",
                name: "ReturnRequest",
                meta: { roles: [3] },
                component: () => import("@/views/ReturnRequest"),
            },
            {
                path: "order-approval",
                name: "OrderApproval",
                meta: { roles: [1, 2] },
                component: () => import("@/views/OrderApproval"),
            },
            {
                path: "my-orders",
                name: "MyOrders",
                meta: { roles: [3] },
                component: () => import("@/views/MyOrders"),
            },
            {
                path: "user",
                name: "User",
                meta: { roles: [1] },
                component: () => import("@/views/User"),
            },
            {
                path: "person",
                name: "Person",
                component: () => import("@/views/Person"),
            },
            {
                path: "password",
                name: "Password",
                component: () => import("@/views/Password"),
            },
        ],
    },
    {
        path: "/login",
        name: "Login",
        component: () => import("@/views/Login"),
    },
    {
        path: "/register",
        name: "Register",
        component: () => import("@/views/Register"),
    },
    {
        path: "/forget",
        name: "Forget",
        component: () => import("@/views/Forget"),
    },
];

const router = createRouter({
    history: createWebHistory(process.env.BASE_URL),
    routes,
});

// 路由守卫：角色权限校验
router.beforeEach((to, from, next) => {
    const userStr = sessionStorage.getItem("user");

    // 公开页面（登录、注册、忘记密码）直接放行
    if (["/login", "/register", "/forget"].includes(to.path)) {
        next();
        return;
    }

    // 未登录则跳转到登录页
    if (!userStr) {
        next("/login");
        return;
    }

    // 如果路由配置了角色限制，校验当前用户角色
    const roles = to.meta.roles;
    if (roles) {
        const user = JSON.parse(userStr);
        if (!roles.includes(user.role)) {
            ElMessage.error("您没有权限访问该页面");
            next("/dashboard");
            return;
        }
    }

    next();
});

export default router;
