# 智能校园设备管理系统

## 项目概述

智能校园设备管理系统是一个完整的 B/S 架构应用，用于校园设备的监控、管理、维护和故障处理。系统集成了 AI 智能助手，提供设备诊断、故障预测、维护建议等智能化服务。

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
- **AI 集成**: OkHttp + 豆包大模型

## 目录结构

```
/workspace/projects/
├── frontend/               # Vue 3 前端项目
│   ├── src/
│   │   ├── api/           # API 接口 (含 ai.js)
│   │   ├── assets/        # 静态资源
│   │   ├── components/    # 公共组件
│   │   ├── router/        # 路由配置
│   │   ├── stores/        # Pinia 状态
│   │   ├── utils/         # 工具函数
│   │   └── views/         # 页面视图 (含 ai/ 智能助手)
│   ├── package.json
│   └── vite.config.js
├── backend/               # Spring Boot 后端
│   ├── src/main/java/    # Java 源码
│   │   └── controller/   # 含 AiController
│   │   └── service/     # 含 AiService
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── sql/          # 数据库脚本
│   └── pom.xml
├── scripts/              # 部署脚本
├── .mysql-data/          # MySQL 数据目录
└── DEPLOYMENT.md         # 部署文档
```

## 关键入口

### 前端入口
- `frontend/src/main.js` - 应用入口
- `frontend/src/App.vue` - 根组件
- `frontend/src/router/index.js` - 路由配置
- `frontend/src/views/ai/index.vue` - AI 智能助手页面

### 后端入口
- `backend/src/main/java/com/campus/equipment/EquipmentApplication.java` - 启动类
- `backend/src/main/java/com/campus/equipment/controller/AiController.java` - AI 控制器
- `backend/src/main/resources/application.yml` - 配置文件

### 端口配置
| 服务 | 端口 |
|------|------|
| 前端开发 | 5173 |
| 后端 API | 8080 |
| Swagger UI | 8080/api/swagger-ui.html |
| MySQL | 3307 |
| 前端预览/部署 | 5000 |

## AI 智能助手

### 功能特性
1. **智能对话**: 基于大模型的自然语言交互
2. **设备诊断**: 根据设备代码和症状分析故障原因
3. **维护建议**: 基于设备状态生成维护计划
4. **故障预测**: 基于历史数据分析潜在风险
5. **报告生成**: 自动生成设备运行分析报告

### API 端点
| 端点 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/ai/chat` | POST | 通用对话 | 登录用户 |
| `/ai/diagnose` | POST | 设备诊断 | ADMIN/MAINTAINER |
| `/ai/maintenance-advice` | POST | 维护建议 | ADMIN/MAINTAINER |
| `/ai/predictive-maintenance` | POST | 故障预测 | ADMIN/MAINTAINER |
| `/ai/generate-report` | POST | 报告生成 | ADMIN/MAINTAINER |
| `/ai/status` | GET | 服务状态 | 登录用户 |

## 智能分析模块

### 功能模块
1. **智能仪表盘**: 综合展示系统健康、待办事项、智能洞察
2. **智能预警**: 基于规则的实时设备监控和预警
3. **智能工单**: 基于技能和负载的智能工单分配
4. **智能健康评估**: 设备健康评分、趋势分析、寿命预测
5. **智能通知**: 个性化通知订阅和智能推送

### API 端点
#### 智能仪表盘
| 端点 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/smart/dashboard/data` | GET | 仪表盘数据 | 登录用户 |
| `/api/smart/dashboard/insights` | GET | 智能洞察 | 登录用户 |
| `/api/smart/dashboard/todos` | GET | 待办事项 | 登录用户 |
| `/api/smart/dashboard/health` | GET | 健康概览 | 登录用户 |
| `/api/smart/dashboard/realtime` | GET | 实时监控 | 登录用户 |
| `/api/smart/dashboard/today` | GET | 今日概览 | 登录用户 |
| `/api/smart/dashboard/actions` | GET | 快捷操作 | 登录用户 |

#### 智能预警
| 端点 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/smart/alert/list` | GET | 预警列表 | ADMIN/MAINTAINER |
| `/api/smart/alert/create` | POST | 创建预警 | ADMIN |
| `/api/smart/alert/status/{id}` | PUT | 更新状态 | ADMIN/MAINTAINER |
| `/api/smart/alert/statistics` | GET | 预警统计 | ADMIN/MAINTAINER |

#### 智能工单
| 端点 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/smart/workorder/assign/{faultId}` | POST | 智能分配 | ADMIN/MAINTAINER |
| `/api/smart/workorder/skills` | GET | 技能评分 | ADMIN/MAINTAINER |
| `/api/smart/workorder/analysis` | GET | 工单分析 | ADMIN/MAINTAINER |
| `/api/smart/workorder/predict/{faultId}` | GET | 完成预测 | ADMIN/MAINTAINER |
| `/api/smart/workorder/scheduling` | GET | 排班建议 | ADMIN/MAINTAINER |

#### 智能健康
| 端点 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/smart/health/score/{deviceCode}` | GET | 健康评分 | ADMIN/MAINTAINER |
| `/api/smart/health/ranking` | GET | 健康排名 | ADMIN/MAINTAINER |
| `/api/smart/health/trend/{deviceCode}` | GET | 健康趋势 | ADMIN/MAINTAINER |
| `/api/smart/health/lifespan/{deviceCode}` | GET | 寿命预测 | ADMIN/MAINTAINER |
| `/api/smart/health/optimization` | GET | 维护优化 | ADMIN/MAINTAINER |
| `/api/smart/health/report` | GET | 健康报告 | ADMIN/MAINTAINER |
| `/api/smart/health/high-risk` | GET | 高风险设备 | ADMIN/MAINTAINER |

#### 智能通知
| 端点 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/smart/notification/list` | GET | 通知列表 | 登录用户 |
| `/api/smart/notification/unread` | GET | 未读数量 | 登录用户 |
| `/api/smart/notification/read/{id}` | POST | 标记已读 | 登录用户 |
| `/api/smart/notification/read-all` | POST | 全部已读 | 登录用户 |
| `/api/smart/notification/stats` | GET | 通知统计 | 登录用户 |
| `/api/smart/notification/subscribe` | POST | 订阅通知 | 登录用户 |
| `/api/smart/notification/generate` | POST | 生成通知 | ADMIN |

### 配置参数
```yaml
ai:
  api:
    url: ${AI_API_URL:}
    key: ${AI_API_KEY:}
    model: ${AI_MODEL:doubao-pro}
    max-tokens: ${AI_MAX_TOKENS:2000}
    temperature: ${AI_TEMPERATURE:0.7}
```

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
6. **AI 服务**: 需要配置 AI_API_KEY 环境变量才能使用 AI 功能

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
6. **AI 功能不可用**: 检查 AI_API_KEY 环境变量是否配置正确
