<template>
    <div class="container">
        <div class="title">
            <router-link to="/" class="back-home">
                <el-icon><Back /></el-icon>
                <span>返回首页</span>
            </router-link>
            <div class="title-text">
                <h2>登录您的账户</h2>
                <p>请输入您的登录信息</p>
            </div>
        </div>
        <div class="form-container">
            <el-form
                ref="ruleFormRef"
                :model="formData"
                :rules="rules"
                label-position="top"
            >
                <el-form-item label="用户名或邮箱" prop="username">
                    <el-input v-model="formData.username" size="large" placeholder="请输入用户名" />
                </el-form-item>
                <el-form-item label="密码" prop="password">
                    <el-input v-model="formData.password" size="large" placeholder="请输入密码" type="password" show-password />
                </el-form-item>
                <el-button class="btn" size="large" type="primary" :loading="submitting" @click="submitForm(ruleFormRef)">登录</el-button>
            </el-form>
            <div class="footer">
                <p>还没有账户？<router-link to="/auth/register">去注册</router-link></p>
            </div>
        </div>
    </div>
</template>
<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getCurrentUser, login } from '@/api/admin'
import { useRouter } from 'vue-router'

const router = useRouter()
const ruleFormRef = ref()
const submitting = ref(false)
const formData = reactive({
    username: '',
    password: ''
})
const rules = reactive({
    username: [
        { required: true, message: '请输入用户名或邮箱', trigger: 'blur' },
        { max: 100, message: '用户名或邮箱不能超过100个字符', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 50, message: '密码长度必须在6到50个字符之间', trigger: 'blur' }
    ]
})

const submitForm = async (formEl) => {
    if (!formEl || submitting.value) return
    const valid = await formEl.validate().catch(() => false)
    if (!valid) return

    submitting.value = true
    try {
        const data = await login({
            username: formData.username.trim(),
            password: formData.password
        })
        if (!data?.token) throw new Error('登录响应缺少 token')

        localStorage.setItem('token', data.token)
        const userInfo = data.userInfo || await getCurrentUser()
        const userType = Number(userInfo?.userType)
        if (![1, 2].includes(userType)) throw new Error('未能获取有效的用户信息')

        localStorage.setItem('userInfo', JSON.stringify(userInfo))
        await router.push(userType === 2 ? '/back/dashboard' : '/')
    } catch (error) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        if (!error.handled) ElMessage.error(error.message || '登录失败，请稍后重试')
    } finally {
        submitting.value = false
    }
}
</script>
<style scoped lang="scss">
    .container {
        width: 384px;
        .title {
            .back-home {
                margin-bottom: 60px;
            }
            .title-text {
                text-align: center;
                h2 {
                    font-size: 36px;
                    margin-bottom: 10px;
                }
                p {
                    font-size: 18px;
                    color: #6b7280;
                }
            }
        }
        .form-container {
            margin-top: 30px;
            .btn {
                margin-top: 40px;
                width: 100%;
            }
            .footer {
                padding: 30px;
                text-align: center;
            }
        }
    }
</style>