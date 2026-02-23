import axios from 'axios'
import router from "../router";

const request = axios.create({
    baseURL: '/api',
    timeout: 5000
})

// request 拦截器
request.interceptors.request.use(config => {
    config.headers['Content-Type'] = config.headers['Content-Type'] || 'application/json;charset=utf-8';

    // 自动注入 token
    let userStr = sessionStorage.getItem("user");
    if (userStr) {
        try {
            let user = JSON.parse(userStr);
            if (user.token) {
                config.headers['token'] = user.token;
            }
        } catch (e) {
            // ignore
        }
    }

    return config
}, error => {
    return Promise.reject(error)
});

// response 拦截器
request.interceptors.response.use(
    response => {
        let res = response.data;
        if (response.config.responseType === 'blob') {
            return res
        }
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res
        }
        return res;
    },
    error => {
        console.log('err' + error)
        return Promise.reject(error)
    }
)

export default request
