# GitHub 上传流程

本文档记录本项目从本地代码目录上传到 GitHub 的标准流程。项目远程仓库：`https://github.com/Zlove583/AI-.git`。

## 1. 上传前检查

1. 确认当前目录是项目根目录。
2. 确认没有提交 `node_modules`、`dist`、`target`、日志、安装包、破解工具或原始视频。
3. 确认 `application.yml` 只读取环境变量，不包含真实 API Key、数据库密码或 JWT 密钥。
4. 确认本地已经安装 Git，并已配置 GitHub 登录凭据（HTTPS 凭据管理器或 SSH Key）。

## 2. 初始化仓库

```powershell
cd "D:\AI全栈企业级心理健康助手项目"
git init
git branch -M main
git remote add origin https://github.com/Zlove583/AI-.git
```

如果已经存在同名远程地址，可使用下面命令修正：

```powershell
git remote set-url origin https://github.com/Zlove583/AI-.git
git remote -v
```

## 3. 检查待上传文件

```powershell
git status --short
git check-ignore -v code/ai-vue/node_modules code/ai-vue/dist code/ai-spingboot/target
```

正常情况下，提交内容主要包括：

- `code/ai-vue`：Vue 3 + Vite 前端源码及依赖锁定文件；
- `code/ai-spingboot`：Spring Boot 后端源码、Maven 配置和环境变量示例；
- `database/schema.sql`：不包含用户种子数据的数据库结构脚本；
- `docs/`：项目说明和本文档；
- `README.md`：项目总览与运行说明。

## 4. 配置本地环境变量

不要把真实配置写回 Git 跟踪文件。Windows PowerShell 示例：

```powershell
$env:DB_URL = "jdbc:mysql://127.0.0.1:3306/mental_health_assistant?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "你的本地数据库密码"
$env:OPENAI_API_KEY = "你的模型服务 API Key"
$env:JWT_SECRET = "长度足够的随机字符串"
```

也可以参考 `code/ai-spingboot/.env.example`，但真实 `.env` 文件必须保持在 `.gitignore` 中。

## 5. 提交并推送

```powershell
git add .
git diff --cached --stat
git diff --cached --name-only
git commit -m "docs: organize mental health assistant project"
git push -u origin main
```

如果远程仓库已有 README 或其他初始提交，先同步再推送：

```powershell
git pull --rebase origin main
git push -u origin main
```

## 6. 上传后验证

```powershell
git status --short --branch
git log --oneline -1
git ls-remote --heads origin
```

打开 GitHub 仓库页面，重点确认：

- 根目录 README 是否正常渲染；
- 没有出现 API Key、数据库密码、JWT 密钥、个人手机号或原始视频；
- 前后端目录、数据库结构脚本和文档均已上传。

## 7. 如果密钥曾经被上传过

仅修改本地文件并不能让旧提交中的密钥失效。应立即在对应模型服务后台撤销/轮换旧 Key，并根据需要清理 Git 历史。不要继续使用已经公开过的密钥。
