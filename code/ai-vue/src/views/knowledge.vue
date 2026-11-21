<template>
    <div>
        <PageHead title="知识文章">
            <template #buttons>
                <el-button @click="handleEdit({})" type="primary">新增</el-button>
            </template>
        </PageHead>
        <TableSearch :formItem="formItem" @search="handleSearch" />
        <el-table :data="tableData" v-loading="loading" empty-text="暂无知识文章或加载失败" style="width: 100%;margin-top: 25px">
            <el-table-column width="450" label="文章标题" fixed="left">
                <template #default="scope">
                    <div style="display: flex; align-items: center">
                        <el-icon><timer /></el-icon>
                        <span>{{ scope.row.title }}</span>
                    </div>
                </template>
            </el-table-column>
             <el-table-column  label="分类" width="200">
                <template #default="scope">
                    <div style="display: flex; align-items: center">
                        <el-icon><timer /></el-icon>
                        <span>{{ categoryMap[scope.row.categoryId] }}</span>
                    </div>
                </template>
            </el-table-column>
            <el-table-column prop="authorName" label="作者" width="150" />
            <el-table-column prop="readCount" label="阅读量" width="150" />
            <el-table-column prop="updatedAt" label="发布时间" width="150" />
            <el-table-column  label="操作" width="240" fixed="right">
                <template #default="scope">
                    <el-button @click="handleEdit(scope.row)" text type="primary">编辑</el-button>
                    <el-button @click="handlePublish(scope.row)" v-if="scope.row.status === 0 || scope.row.status === 2" text type="success">发布</el-button>
                    <el-button @click="handleUnpublish(scope.row)" v-if="scope.row.status === 1" text type="warning">下线</el-button>
                    <el-button @click="handleDelete(scope.row)" text type="danger">删除</el-button>
                </template>
            </el-table-column>
        </el-table>
        <el-pagination
         style="margin-top: 25px"
         :page-size="pagination.size"
         layout="prev, pager, next"
         :total="pagination.total"
         @change="handleChange"
         />
         <ArticleDialog v-model:modelValue="dialogVisible" :article="currentArticle" :categories="categories" @success="handleSuccess" />
    </div>
</template>
<script setup>
import { onMounted, ref, reactive } from 'vue'
import PageHead from '@/components/PageHead.vue'
import TableSearch from '@/components/TableSearch.vue'
import { categoryTree, articlePage, getArticleDetail, changeArticleStatus, deleteArticle } from '@/api/admin'
import ArticleDialog from '@/components/ArticleDialog.vue'
import { ElMessageBox, ElMessage } from 'element-plus'


const formItem = [
    { comp: 'input', prop: 'title', label: '文章标题', placeholder: '请输入文章标题' },
    { comp: 'select', prop: 'categoryId', label: '分类',placeholder: '请选择分类' },
    { comp: 'select', prop: 'status', label: '状态', placeholder: '请输入文章内容', options: [{
       label: '草稿',
       value: '0'
    },{
       label: '已发布',
       value: '1'
    },{
       label: '已下线',
       value: '2'
    }] }
]

// 分页参数
const pagination = reactive({
    currentPage: 1,
    size: 10,
    total: 0
})

const handleSearch = async (formData = {}) => {
    loading.value = true
    try {
        const data = await articlePage({ ...pagination, ...formData })
        tableData.value = data?.records || []
        pagination.total = data?.total || 0
    } catch {
        tableData.value = []
        pagination.total = 0
    } finally {
        loading.value = false
    }
}

const handleChange = (page) => {
    pagination.currentPage = page
    handleSearch()
}

// 分类映射
const categoryMap = reactive({})
// 分类列表
const categories = ref([])

// 列表数据
const tableData = ref([])
const loading = ref(false)

// 新增和编辑
const dialogVisible = ref(false)
const currentArticle = ref(null)
const handleSuccess = () => {
    dialogVisible.value = false
    // 刷新列表
    handleSearch()
}
const handleEdit = async (row) => {
    if (!row.id) {
        currentArticle.value = null
        dialogVisible.value = true
        return
    }
    try {
        currentArticle.value = await getArticleDetail(row.id)
        dialogVisible.value = true
    } catch {
        currentArticle.value = null
    }
}

// 发布
const handlePublish = async (row) => {
    try {
        await ElMessageBox.confirm(`确认发布文章${row.title}吗？`, '确认', {
            confirmButtonText: '确认发布',
            cancelButtonText: '取消',
            type: 'info'
        })
        await changeArticleStatus(row.id, { status: 1 })
        ElMessage.success('发布成功')
        await handleSearch()
    } catch {
        // 用户取消或请求失败时不改变当前列表
    }
}

const handleUnpublish = async (row) => {
    try {
        await ElMessageBox.confirm(`确认下线文章${row.title}吗？`, '确认', {
            confirmButtonText: '确认下线',
            cancelButtonText: '取消',
            type: 'warning'
        })
        await changeArticleStatus(row.id, { status: 2 })
        ElMessage.success('下线成功')
        await handleSearch()
    } catch {
        // 用户取消或请求失败时不改变当前列表
    }
}

const handleDelete = async (row) => {
    try {
        await ElMessageBox.confirm(`确认删除文章${row.title}吗？`, '确认', {
            confirmButtonText: '确认删除',
            cancelButtonText: '取消',
            type: 'danger'
        })
        await deleteArticle(row.id)
        ElMessage.success('删除成功')
        await handleSearch()
    } catch {
        // 用户取消或请求失败时不改变当前列表
    }
}

onMounted(async () => {
    try {
        const data = await categoryTree()
        categories.value = (data || []).map(item => {
            categoryMap[item.id] = item.categoryName
            return { label: item.categoryName, value: item.id }
        })
        formItem[1].options = categories.value
    } catch {
        categories.value = []
        formItem[1].options = []
    }
    handleSearch()
})
</script>
