<template>
  <div class="login-container">
    <!-- 背景动画 -->
    <div class="bg-animation">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
      <div class="circle circle-4"></div>
    </div>
    
    <!-- 登录卡片 -->
    <div class="login-card">
      <!-- 左侧品牌区域 -->
      <div class="login-brand">
        <div class="brand-content">
          <div class="brand-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M12 2L2 7l10 5 10-5-10-5z"/>
              <path d="M2 17l10 5 10-5"/>
              <path d="M2 12l10 5 10-5"/>
            </svg>
          </div>
          <h1 class="brand-title">智能校园设备管理系统</h1>
          <p class="brand-subtitle">Smart Campus Equipment Management System</p>
          <div class="brand-features">
            <div class="feature-item">
              <i class="el-icon-monitor"></i>
              <span>设备监控</span>
            </div>
            <div class="feature-item">
              <i class="el-icon-warning"></i>
              <span>智能预警</span>
            </div>
            <div class="feature-item">
              <i class="el-icon-chat-dot-round"></i>
              <span>AI 诊断</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧登录表单 -->
      <div class="login-form-wrapper">
        <div class="login-form">
          <div class="form-header">
            <h2>欢迎回来</h2>
            <p>请登录您的账户继续</p>
          </div>
          
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="rules"
            class="login-form-content"
          >
            <el-form-item prop="username">
              <div class="input-wrapper">
                <i class="el-icon-user"></i>
                <el-input
                  v-model="loginForm.username"
                  placeholder="请输入用户名"
                  size="large"
                  prefix-icon="none"
                />
              </div>
            </el-form-item>
            
            <el-form-item prop="password">
              <div class="input-wrapper">
                <i class="el-icon-lock"></i>
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="请输入密码"
                  size="large"
                  prefix-icon="none"
                  show-password
                  @keyup.enter="handleLogin"
                />
              </div>
            </el-form-item>
            
            <div class="form-options">
              <el-checkbox v-model="rememberMe">记住我</el-checkbox>
              <a href="#" class="forgot-link">忘记密码？</a>
            </div>
            
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                class="login-button"
                @click="handleLogin"
              >
                <span v-if="!loading">登 录</span>
                <span v-else>登录中...</span>
              </el-button>
            </el-form-item>
          </el-form>
          
          <div class="demo-accounts">
            <p class="demo-title">演示账号</p>
            <div class="demo-list">
              <div class="demo-item" @click="fillDemo('admin', 'Admin@2024')">
                <span class="demo-role">管理员</span>
                <span class="demo-info">admin / Admin@2024</span>
              </div>
              <div class="demo-item" @click="fillDemo('maintainer1', 'Mtn@2024')">
                <span class="demo-role">维护员</span>
                <span class="demo-info">maintainer1 / Mtn@2024</span>
              </div>
              <div class="demo-item" @click="fillDemo('user1', 'User@2024')">
                <span class="demo-role">普通用户</span>
                <span class="demo-info">user1 / User@2024</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getInfo as getUserProfile, login as loginApi } from '@/api/auth'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const fillDemo = (username, password) => {
  loginForm.username = username
  loginForm.password = password
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      const res = await loginApi(loginForm)
      if (res.code === 200) {
        localStorage.setItem('token', res.data.token)
        
        // 获取用户信息
        const profileRes = await getUserProfile()
        if (profileRes.code === 200) {
          userStore.setUserInfo(profileRes.data)
        }
        
        ElMessage.success('登录成功')
        router.push('/')
      } else {
        ElMessage.error(res.message || '登录失败')
      }
    } catch (error) {
      ElMessage.error(error.message || '登录失败')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

/* 背景动画 */
.bg-animation {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  overflow: hidden;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.circle-2 {
  width: 200px;
  height: 200px;
  top: 50%;
  right: -50px;
  animation-delay: 2s;
}

.circle-3 {
  width: 150px;
  height: 150px;
  bottom: -50px;
  left: 30%;
  animation-delay: 4s;
}

.circle-4 {
  width: 100px;
  height: 100px;
  top: 20%;
  left: 20%;
  animation-delay: 1s;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

/* 登录卡片 */
.login-card {
  display: flex;
  width: 900px;
  max-width: 95%;
  background: white;
  border-radius: 24px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  overflow: hidden;
  position: relative;
  z-index: 10;
  animation: slideUp 0.6s ease;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 品牌区域 */
.login-brand {
  flex: 1;
  background: linear-gradient(135deg, #409eff 0%, #1890ff 100%);
  padding: 60px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-brand::before {
  content: '';
  position: absolute;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  top: -50%;
  left: -50%;
  animation: rotate 30s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: white;
}

.brand-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 30px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  backdrop-filter: blur(10px);
}

.brand-icon svg {
  width: 100%;
  height: 100%;
}

.brand-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 10px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.brand-subtitle {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 40px;
}

.brand-features {
  display: flex;
  justify-content: center;
  gap: 30px;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.feature-item i {
  font-size: 24px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
}

.feature-item span {
  font-size: 13px;
}

/* 表单区域 */
.login-form-wrapper {
  flex: 1;
  padding: 60px 50px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-form {
  width: 100%;
  max-width: 320px;
}

.form-header {
  text-align: center;
  margin-bottom: 40px;
}

.form-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 10px;
}

.form-header p {
  font-size: 14px;
  color: #909399;
}

.input-wrapper {
  position: relative;
  width: 100%;
}

.input-wrapper i {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 18px;
  color: #909399;
  z-index: 1;
}

.input-wrapper :deep(.el-input__wrapper) {
  padding-left: 45px !important;
  border-radius: 12px !important;
  box-shadow: 0 0 0 1px #dcdfe6 inset !important;
  height: 48px;
}

.input-wrapper :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #409eff inset !important;
}

.input-wrapper :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2) inset !important;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.forgot-link {
  color: #409eff;
  font-size: 14px;
  text-decoration: none;
}

.forgot-link:hover {
  color: #66b1ff;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px !important;
  background: linear-gradient(135deg, #409eff 0%, #1890ff 100%) !important;
  border: none !important;
  box-shadow: 0 4px 14px rgba(64, 158, 255, 0.4) !important;
  transition: all 0.3s ease !important;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.5) !important;
}

.login-button:active {
  transform: translateY(0);
}

/* 演示账号 */
.demo-accounts {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.demo-title {
  font-size: 12px;
  color: #909399;
  text-align: center;
  margin-bottom: 12px;
}

.demo-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.demo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.demo-item:hover {
  background: #ecf5ff;
  transform: translateX(4px);
}

.demo-role {
  font-size: 13px;
  font-weight: 500;
  color: #409eff;
}

.demo-info {
  font-size: 12px;
  color: #909399;
}

/* 响应式 */
@media (max-width: 768px) {
  .login-card {
    flex-direction: column;
    width: 100%;
    max-width: 100%;
    border-radius: 0;
    min-height: 100vh;
  }
  
  .login-brand {
    padding: 40px 30px;
    min-height: 200px;
  }
  
  .brand-title {
    font-size: 22px;
  }
  
  .brand-subtitle {
    font-size: 12px;
  }
  
  .brand-features {
    gap: 20px;
  }
  
  .feature-item i {
    font-size: 20px;
    padding: 10px;
  }
  
  .feature-item span {
    font-size: 12px;
  }
  
  .login-form-wrapper {
    padding: 40px 30px;
  }
  
  .form-header h2 {
    font-size: 24px;
  }
}
</style>
