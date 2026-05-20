# 智能校园设备管理系统

## 项目概述

智能校园设备管理系统是一个完整的 B/S 架构应用，用于校园设备的监控、管理、维护和故障处理。

## 技术栈

### 前端
- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite
- **UI 库**: Element Plus
- **图表**: ECharts + vue-echarts
- **路由**: Vue Router 4
- **状态管理**: Pinia
- **HTTP 客户端**: Axios
- **包管理器**: pnpm

### 后端
- **框架**: Spring Boot 2.7.18
- **ORM**: MyBatis-Plus 3.5.5
- **安全**: Spring Security + JWT
- **数据库**: MySQL 8
- **构建工具**: Maven
- **Java 版本**: 11 (兼容 17)

## 目录结构

```
/workspace/projects/
├── frontend/               # Vue 3 前端项目
│   ├── src/
│   │   ├── api/           # API 接口
│   │   ├── assets/        # 静态资源
│   │   ├── components/    # 公共组件
│   │   ├── router/        # 路由配置
│   │   ├── stores/        # Pinia 状态
│   │   ├── utils/         # 工具函数
│   │   └── views/         # 页面视图
│   ├── package.json
│   └── vite.config.js
├── backend/               # Spring Boot 后端
│   ├── src/main/java/    # Java 源码
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── sql/           # 数据库脚本
│   └── pom.xml
├── scripts/              # 部署脚本
│   ├── build.sh          # 部署构建脚本
│   ├── run.sh            # 部署运行脚本
│   ├── coze-preview-build.sh
│   └── coze-preview-run.sh
├── .mysql-data/           # MySQL 数据目录
├── DEPLOYMENT.md          # 部署文档
└── start-*.ps1           # 启动脚本
```

## 关键入口

### 前端入口
- `frontend/src/main.js` - 应用入口
- `frontend/src/App.vue` - 根组件
- `frontend/src/router/index.js` - 路由配置

### 后端入口
- `backend/src/main/java/com/campus/equipment/EquipmentApplication.java` - 启动类
- `backend/src/main/resources/application.yml` - 配置文件

### 端口配置
| 服务 | 端口 |
|------|------|
| 前端开发 | 5173 |
| 后端 API | 8080 |
| Swagger UI | 8080/api/swagger-ui.html |
| MySQL | 3307 |
| 前端预览/部署 | 5000 |

## 运行与预览

### 前端开发
```bash
cd frontend
pnpm install
pnpm run dev
```

### 后端开发
```bash
cd backend
mvn spring-boot:run
```

### 启动脚本 (Windows)
```powershell
.\start-all.ps1    # 启动全部服务
.\start-frontend.ps1
.\start-backend.ps1
.\start-mysql.ps1
```

## 默认账号

| 账号 | 密码 | 角色 |
|------|------|------|
| admin | admin123 | 超级管理员 |
| maintainer1 | admin123 | 维护员 |
| user1 | admin123 | 普通用户 |

## 用户偏好与长期约束

1. **包管理器**: 前端项目统一使用 `pnpm`，禁止 `npm` 或 `yarn`
2. **Node 版本**: 前端需要 Node 18+
3. **Java 版本**: 后端推荐 JDK 17，兼容 JDK 11
4. **预览端口**: 平台限制前端预览只能暴露 5000 端口
5. **部署端口**: 部署服务固定使用 5000 端口

## 预览链路配置

### 项目判断
- **项目类型**: web（Vue 3 前端应用）
- **判定依据**: 存在 `package.json`、`vite.config.js`、`index.html` 和 `src/` 目录

### 预览入口
- **build**: `bash scripts/coze-preview-build.sh`
  - 执行 `pnpm install` 安装依赖
- **run**: `bash scripts/coze-preview-run.sh`
  - 使用 `vite preview` 在 5000 端口提供预览
  - 绑定到 `0.0.0.0` 支持外部访问
  - 幂等性：每次执行前清理 5000 端口残留进程

### 预览验证
- `curl http://localhost:5000` 返回 200
- `ss -lptn 'sport = :5000'` 显示监听在 `0.0.0.0:5000`

## 部署配置

### 项目判断
- **项目类型**: web (Vite Frontend-as-Service)
- **部署表面**: service.flavor=web
- **判定依据**: 纯前端项目，无后端服务入口

### 部署入口
- **build**: `bash scripts/build.sh`
  - 进入 `frontend/` 目录
  - 执行 `pnpm install` 和 `pnpm vite build`
  - 产物输出到 `frontend/dist/`
- **run**: `bash scripts/run.sh`
  - 使用 `npx serve frontend/dist` 在 5000 端口提供服务
  - 端口通过 `DEPLOY_RUN_PORT` 环境变量配置，默认为 5000

### 运行时要求
- **requires**: `nodejs-24`

## 常见问题和预防

1. **前端依赖安装失败**: 确保使用 `pnpm install`，不是 `npm install`
2. **后端启动失败**: 检查 MySQL 服务是否运行，默认端口 3307
3. **跨域问题**: Vite 开发服务器已配置 `/api` 代理到后端 8080 端口
4. **JWT 过期**: 默认 token 有效期配置在 `application.yml`
5. **预览端口冲突**: 5000 端口已被占用时，先清理残留进程
