import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: '/api',
  timeout: 10000
})

const authErrorCodes = new Set(['401', 'A0230', 'A0231', 'A0300', 'A0301'])

const handleAuthFailure = (config) => {
  if (config?.url?.includes('/user/login')) return

  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  if (window.location.pathname !== '/auth/login') {
    const redirect = encodeURIComponent(window.location.pathname + window.location.search)
    window.location.href = `/auth/login?redirect=${redirect}`
  }
}

service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.token = token
    }
    return config
  },
  (error) => Promise.reject(error)
)

service.interceptors.response.use(
  (response) => {
    const { data, config } = response
    if (!data || typeof data !== 'object' || data.code === undefined) {
      const error = new Error('服务响应格式异常')
      error.handled = true
      ElMessage.error(error.message)
      return Promise.reject(error)
    }

    if (String(data.code) === '200') {
      return data.data
    }

    const code = String(data.code)
    const error = new Error(data.msg || '请求失败')
    error.code = code
    error.handled = true
    if (authErrorCodes.has(code)) {
      handleAuthFailure(config)
    }
    ElMessage.error(error.message)
    return Promise.reject(error)
  },
  (error) => {
    const status = error.response?.status
    const code = String(error.response?.data?.code || status || '')
    if (status === 401 || authErrorCodes.has(code)) {
      handleAuthFailure(error.config)
    }

    if (error.response?.data?.msg) {
      error.message = error.response.data.msg
    } else if (error.code === 'ECONNABORTED') {
      error.message = '请求超时，请稍后重试'
    } else if (!error.response) {
      error.message = '无法连接服务器，请检查后端服务是否已启动'
    } else {
      error.message = `请求失败（${status}）`
    }
    error.handled = true
    ElMessage.error(error.message)
    return Promise.reject(error)
  }
)

export default service