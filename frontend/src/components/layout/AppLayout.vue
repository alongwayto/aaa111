<template>
  <el-container class="app-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapsed ? '64px' : '240px'" class="sidebar">
      <!-- Logo 区域 -->
      <div class="logo" :class="{ collapsed: isCollapsed }">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M12 2L2 7l10 5 10-5-10-5z"/>
            <path d="M2 17l10 5 10-5"/>
            <path d="M2 12l10 5 10-5"/>
          </svg>
        </div>
        <transition name="fade">
          <span v-if="!isCollapsed" class="logo-text">智能校园设备</span>
        </transition>
      </div>
      
      <!-- 菜单 -->
      <el-scrollbar class="menu-scrollbar">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapsed"
          :unique-opened="true"
          background-color="transparent"
          text-color="rgba(255,255,255,0.7)"
          active-text-color="#ffffff"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/dashboard" class="menu-item">
            <el-icon><Odometer /></el-icon>
            <template #title>首页仪表盘</template>
          </el-menu-item>

          <el-sub-menu index="device" class="menu-item">
            <template #title>
              <el-icon><Monitor /></el-icon>
              <span>设备管理</span>
            </template>
            <el-menu-item index="/device/list">设备列表</el-menu-item>
            <el-menu-item index="/device/category">设备分类</el-menu-item>
            <el-menu-item index="/device/location">位置管理</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="monitor" class="menu-item">
            <template #title>
              <el-icon><DataAnalysis /></el-icon>
              <span>状态监控</span>
            </template>
            <el-menu-item index="/monitor/status">
              实时监控
              <el-badge v-if="alertCount > 0" :value="alertCount" class="menu-badge" type="danger" />
            </el-menu-item>
            <el-menu-item index="/monitor/alert">预警管理</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="fault" class="menu-item">
            <template #title>
              <el-icon><Warning /></el-icon>
              <span>故障管理</span>
            </template>
            <el-menu-item index="/fault/report">故障上报</el-menu-item>
            <el-menu-item index="/fault/workorder">工单管理</el-menu-item>
            <el-menu-item index="/fault/archive">故障归档</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="statistics" class="menu-item">
            <template #title>
              <el-icon><TrendCharts /></el-icon>
              <span>数据统计</span>
            </template>
            <el-menu-item index="/statistics/usage">使用率统计</el-menu-item>
            <el-menu-item index="/statistics/cost">维护成本</el-menu-item>
            <el-menu-item index="/statistics/fault">故障报表</el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/smart" class="menu-item smart-menu">
            <el-icon><Cpu /></el-icon>
            <template #title>智能分析</template>
          </el-menu-item>

          <el-menu-item index="/ai" class="menu-item ai-menu">
            <el-icon><MagicStick /></el-icon>
            <template #title>AI 助手</template>
          </el-menu-item>

          <el-sub-menu index="system" v-if="userStore.roles.includes('ROLE_ADMIN')" class="menu-item">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/system/user">用户管理</el-menu-item>
            <el-menu-item index="/system/role">角色管理</el-menu-item>
            <el-menu-item index="/system/log">操作日志</el-menu-item>
            <el-menu-item index="/system/backup">数据备份</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-scrollbar>
      
      <!-- 折叠按钮 -->
      <div class="collapse-trigger" @click="isCollapsed = !isCollapsed">
        <el-icon size="18">
          <component :is="isCollapsed ? 'DArrowRight' : 'DArrowLeft'" />
        </el-icon>
      </div>
    </el-aside>

    <el-container class="main-container">
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <!-- 面包屑 -->
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/' }">
              <el-icon><House /></el-icon>
            </el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <!-- 全屏 -->
          <el-tooltip content="全屏" placement="bottom">
            <div class="header-icon-btn" @click="toggleFullScreen">
              <el-icon size="20"><FullScreen /></el-icon>
            </div>
          </el-tooltip>
          
          <!-- 预警通知 -->
          <el-tooltip content="预警通知">
            <el-badge :value="alertCount" :hidden="alertCount === 0" class="header-badge">
              <div class="header-icon-btn" @click="$router.push('/monitor/alert')">
                <el-icon size="20"><Bell /></el-icon>
              </div>
            </el-badge>
          </el-tooltip>
          
          <!-- 用户信息 -->
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-info">
              <el-avatar :size="36" class="user-avatar">
                <span>{{ userStore.username?.charAt(0)?.toUpperCase() }}</span>
              </el-avatar>
              <div class="user-detail">
                <span class="username">{{ userStore.userInfo?.realName || userStore.username }}</span>
                <span class="role">{{ userStore.roles[0] === 'ROLE_ADMIN' ? '管理员' : userStore.roles[0] === 'ROLE_MAINTAINER' ? '维护员' : '用户' }}</span>
              </div>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon> 个人信息
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon> 设置
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getUnhandledCount } from '@/api/monitor'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapsed = ref(false)
const alertCount = ref(0)

const activeMenu = computed(() => route.path)

const toggleFullScreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

onMounted(async () => {
  try {
    const res = await getUnhandledCount()
    alertCount.value = res.data || 0
  } catch {}
})

async function handleCommand(cmd) {
  if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'settings') {
    router.push('/settings')
  } else if (cmd === 'logout') {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}
</script>

<style scoped>
.app-layout {
  height: 100vh;
  background: #f5f7fa;
}

/* 侧边栏 */
.sidebar {
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 200px;
  background: linear-gradient(180deg, rgba(64, 158, 255, 0.1) 0%, transparent 100%);
  pointer-events: none;
}

/* Logo */
.logo {
  height: 70px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  white-space: nowrap;
  overflow: hidden;
}

.logo.collapsed {
  padding: 0;
  justify-content: center;
}

.logo-icon {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  padding: 6px;
  background: linear-gradient(135deg, #409eff 0%, #1890ff 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-icon svg {
  width: 100%;
  height: 100%;
  color: white;
}

.logo-text {
  font-size: 16px;
  font-weight: 700;
  color: #ffffff;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 菜单滚动条 */
.menu-scrollbar {
  flex: 1;
  padding: 12px 0;
}

.sidebar-menu {
  border-right: none !important;
  background: transparent !important;
}

/* 菜单项 */
:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  height: 50px !important;
  line-height: 50px !important;
  margin: 4px 12px !important;
  padding: 0 16px !important;
  border-radius: 12px !important;
  transition: all 0.3s ease !important;
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background: rgba(64, 158, 255, 0.15) !important;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.3) 0%, rgba(64, 158, 255, 0.15) 100%) !important;
}

:deep(.el-menu-item.is-active)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 24px;
  background: #409eff;
  border-radius: 0 4px 4px 0;
}

:deep(.el-sub-menu .el-menu-item) {
  height: 44px !important;
  line-height: 44px !important;
  margin: 2px 12px !important;
  padding: 0 16px !important;
}

/* 智能分析菜单高亮 */
.smart-menu,
.ai-menu {
  background: linear-gradient(90deg, rgba(103, 58, 183, 0.2) 0%, rgba(64, 158, 255, 0.1) 100%) !important;
}

.smart-menu.is-active,
.ai-menu.is-active {
  background: linear-gradient(90deg, rgba(103, 58, 183, 0.4) 0%, rgba(64, 158, 255, 0.25) 100%) !important;
}

.menu-badge {
  margin-left: 8px;
}

/* 折叠按钮 */
.collapse-trigger {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: all 0.3s;
}

.collapse-trigger:hover {
  color: #409eff;
  background: rgba(64, 158, 255, 0.1);
}

/* 主容器 */
.main-container {
  display: flex;
  flex-direction: column;
}

/* 顶部导航 */
.header {
  background: #ffffff;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.header-left {
  display: flex;
  align-items: center;
}

.breadcrumb {
  display: flex;
  align-items: center;
}

.breadcrumb :deep(.el-breadcrumb__inner) {
  display: flex;
  align-items: center;
  gap: 4px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  cursor: pointer;
  color: #606266;
  transition: all 0.3s;
}

.header-icon-btn:hover {
  background: #f5f7fa;
  color: #409eff;
}

.header-badge {
  line-height: 1;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.user-info:hover {
  background: #f5f7fa;
}

.user-avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #409eff 0%, #1890ff 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
  font-weight: 600;
}

.user-detail {
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.role {
  font-size: 12px;
  color: #909399;
}

.arrow {
  color: #c0c4cc;
  transition: transform 0.3s;
}

.user-info:hover .arrow {
  transform: rotate(180deg);
}

/* 主内容区 */
.main-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: #f5f7fa;
}

/* 路由动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 响应式 */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    z-index: 1000;
    transform: translateX(-100%);
  }
  
  .sidebar:not(.is-collapse) {
    transform: translateX(0);
  }
  
  .header {
    padding: 0 16px;
  }
  
  .user-detail {
    display: none;
  }
}
</style>
