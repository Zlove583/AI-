import { createRouter, createWebHistory } from 'vue-router'
import BackendLayout from '@/components/BackendLayout.vue'
import AuthLayout from '@/components/AuthLayout.vue'
import FrontendLayout from '@/components/FrontendLayout.vue'


// 路由配置
const backendRoutes = [
    {
        path: '/back',
        redirect: '/back/dashboard',
        component: BackendLayout,
        children: [
            {
                path: 'dashboard',
                component: () => import('@/views/dashboard.vue'),
                meta: {
                    title: '数据分析',
                    icon: 'PieChart'
                }
            },
            {
                path: 'knowledge',
                component: () => import('@/views/knowledge.vue'),
                meta: {
                    title: '知识文章',
                    icon: 'ChatLineSquare'
                }
            },
            {
                path: 'consultations',
                component: () => import('@/views/consultations.vue'),
                meta: {
                    title: '咨询记录',
                    icon: 'Message'
                }
            },
            {
                path: 'emotional',
                component: () => import('@/views/emotional.vue'),
                meta: {
                    title: '情绪日志',
                    icon: 'User'
                }
            }
        ]
    },
    {
        path: '/auth',
        component: AuthLayout,
        children: [
            {
                path: 'login',
                component: () => import('@/views/login.vue'),
                meta: {
                    title: '登录'
                }
            },
            {
                path: 'register',
                component: () => import('@/views/register.vue'),
                meta: {
                    title: '注册'
                }
            }
        ]
    }
]

const frontendRoutes = [
    {
        path: '/',
        component: FrontendLayout,
        children: [
            {
                path: '',
                component: () => import('@/views/home.vue')
            },
            {
                path: 'consultation',
                component: () => import('@/views/consultation.vue'),
                meta: { requiresAuth: true }
            },
            {
                path: 'emotion-diary',
                component: () => import('@/views/emotionDiary.vue'),
                meta: { requiresAuth: true }
            },
            {
                path: 'knowledge',
                component: () => import('@/views/frontendKnowledge.vue')
            },
            {
                path: 'knowledge/article/:id',
                component: () => import('@/views/articleDetail.vue'),
                props: true
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes: [ ...backendRoutes, ...frontendRoutes]
})

// 路由前置守卫
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    let userInfo = null

    if (token) {
        try {
            userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
        } catch {
            localStorage.removeItem('token')
            localStorage.removeItem('userInfo')
        }
    }

    const userType = Number(userInfo?.userType)
    if (token && [1, 2].includes(userType)) {
        if (userType === 2) {
            next(to.path.startsWith('/back') ? undefined : '/back/dashboard')
        } else {
            next(to.path.startsWith('/back') || to.path.startsWith('/auth') ? '/' : undefined)
        }
        return
    }

    if (token || localStorage.getItem('userInfo')) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
    }

    if (to.path.startsWith('/back') || to.meta.requiresAuth) {
        next({ path: '/auth/login', query: { redirect: to.fullPath } })
    } else {
        next()
    }
})

export default router
