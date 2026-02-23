import { createRouter, createWebHistory } from "vue-router";
import Layout from "../layout/Layout";

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
                component: () => import("@/views/Material"),
            },
            {
                path: "outbound",
                name: "OutboundRequest",
                component: () => import("@/views/OutboundRequest"),
            },
            {
                path: "return",
                name: "ReturnRequest",
                component: () => import("@/views/ReturnRequest"),
            },
            {
                path: "order-approval",
                name: "OrderApproval",
                component: () => import("@/views/OrderApproval"),
            },
            {
                path: "my-orders",
                name: "MyOrders",
                component: () => import("@/views/MyOrders"),
            },
            {
                path: "user",
                name: "User",
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

export default router;
