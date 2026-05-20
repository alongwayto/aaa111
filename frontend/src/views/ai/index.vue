<template>
  <div class="ai-assistant-container">
    <!-- 头部 -->
    <div class="ai-header">
      <div class="header-content">
        <div class="header-left">
          <div class="ai-avatar">
            <el-icon :size="32"><MagicStick /></el-icon>
          </div>
          <div class="header-info">
            <h2>智能校园助手</h2>
            <p>基于大模型的智能设备管理助手</p>
          </div>
        </div>
        <div class="header-right">
          <div class="status-badge" :class="{ online: connected, offline: !connected }">
            <span class="status-dot"></span>
            <span>{{ connected ? 'AI 在线' : 'AI 离线' }}</span>
          </div>
          <el-button text @click="showSettings = true">
            <el-icon><Setting /></el-icon>
          </el-button>
          <el-button text @click="clearChat">
            <el-icon><Delete /></el-icon>
            清空
          </el-button>
        </div>
      </div>
    </div>

    <!-- 聊天内容区 -->
    <div class="ai-content" ref="chatContainer">
      <!-- 欢迎界面 -->
      <div class="welcome-section" v-if="messages.length === 0">
        <div class="welcome-animation">
          <div class="ai-greeting">
            <div class="greeting-icon">
              <el-icon :size="64" color="#667eea"><ChatLineSquare /></el-icon>
            </div>
            <h3>你好，我是智能校园助手</h3>
            <p>我可以帮你完成以下任务：</p>
          </div>
        </div>
        
        <div class="quick-actions">
          <div
            v-for="action in quickActions"
            :key="action.label"
            class="quick-action-card"
            @click="sendQuickMessage(action.prompt)"
          >
            <div class="action-icon">
              <el-icon :size="24"><component :is="action.icon" /></el-icon>
            </div>
            <span class="action-label">{{ action.label }}</span>
            <span class="action-desc">{{ action.desc }}</span>
          </div>
        </div>
      </div>

      <!-- 消息列表 -->
      <div class="message-list" v-else>
        <div
          v-for="(msg, index) in messages"
          :key="index"
          class="message-item"
          :class="msg.role"
        >
          <div class="message-avatar" :class="msg.role">
            <el-icon v-if="msg.role === 'user'" :size="22"><User /></el-icon>
            <el-icon v-else :size="22"><MagicStick /></el-icon>
          </div>
          <div class="message-content">
            <div class="message-bubble" :class="msg.role" v-html="formatMessage(msg.content)"></div>
            <div class="message-time">{{ msg.time }}</div>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="message-item assistant">
          <div class="message-avatar assistant">
            <el-icon :size="22"><MagicStick /></el-icon>
          </div>
          <div class="message-content">
            <div class="message-bubble assistant loading">
              <div class="typing-indicator">
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
              </div>
              <span class="loading-text">AI 正在思考中...</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="ai-input-section">
      <div class="input-container">
        <div class="input-wrapper">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="2"
            placeholder="输入你的问题，支持设备诊断、故障分析、维护建议等..."
            @keydown.enter.ctrl="handleSend"
            @keydown.enter.meta="handleSend"
            :disabled="loading"
            resize="none"
          />
        </div>
        <el-button
          type="primary"
          :loading="loading"
          @click="handleSend"
          class="send-btn"
          :disabled="!inputMessage.trim()"
        >
          <el-icon><Promotion /></el-icon>
          <span>发送</span>
        </el-button>
      </div>
      <div class="input-footer">
        <span class="tip">
          <el-icon><InfoFilled /></el-icon>
          按 Ctrl+Enter 快速发送
        </span>
        <span class="capability">
          <el-icon><Cpu /></el-icon>
          支持多轮对话上下文
        </span>
      </div>
    </div>

    <!-- 设置抽屉 -->
    <el-drawer v-model="showSettings" title="助手设置" size="380px" direction="rtl">
      <div class="settings-content">
        <div class="settings-section">
          <h4>模型配置</h4>
          <div class="settings-item">
            <label>AI 模型</label>
            <el-select v-model="settings.model" placeholder="选择模型">
              <el-option label="豆包 Pro (高性能)" value="doubao-pro" />
              <el-option label="豆包 Lite (快速)" value="doubao-lite" />
            </el-select>
          </div>
          <div class="settings-item">
            <label>响应风格</label>
            <el-radio-group v-model="settings.style">
              <el-radio label="professional">专业详细</el-radio>
              <el-radio label="concise">简洁明了</el-radio>
            </el-radio-group>
          </div>
        </div>
        
        <div class="settings-section">
          <h4>功能设置</h4>
          <div class="settings-item">
            <label>多轮对话</label>
            <el-switch v-model="settings.contextEnabled" />
            <span class="settings-hint">开启后可进行多轮对话</span>
          </div>
          <div class="settings-item">
            <label>显示思考过程</label>
            <el-switch v-model="settings.showThinking" />
            <span class="settings-hint">展示 AI 分析思路</span>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  MagicStick, ChatDotRound, ChatLineSquare, User, Promotion, Delete,
  Monitor, Warning, Tools, TrendCharts, HelpFilled, Setting, InfoFilled, Cpu
} from '@element-plus/icons-vue'
import { aiChat } from '@/api/ai'

const chatContainer = ref(null)
const inputMessage = ref('')
const loading = ref(false)
const connected = ref(true)
const showSettings = ref(false)

const settings = reactive({
  model: 'doubao-pro',
  style: 'professional',
  contextEnabled: true,
  showThinking: false
})

const messages = ref([])

const quickActions = [
  { label: '设备诊断', icon: 'Monitor', desc: '分析设备异常原因', prompt: '帮我分析一下近期设备故障的原因和规律' },
  { label: '维护建议', icon: 'Tools', desc: '推荐维护周期', prompt: '给出一些设备维护的优化建议' },
  { label: '故障分析', icon: 'Warning', desc: '分析故障原因', prompt: '分析一下本月故障率上升的原因' },
  { label: '数据报表', icon: 'TrendCharts', desc: '生成分析报告', prompt: '生成一份设备运行分析报告' },
  { label: '使用帮助', icon: 'HelpFilled', desc: '系统功能介绍', prompt: '介绍一下这个系统的主要功能' }
]

const systemPrompt = `你是智能校园设备管理系统的AI助手，专门帮助用户管理校园设备。

你的能力包括：
1. 设备故障诊断：分析设备异常原因，给出处理建议
2. 维护计划建议：根据设备使用情况，推荐维护周期
3. 数据分析：解读设备运行数据，发现潜在问题
4. 系统操作指导：帮助用户使用系统各项功能
5. 故障预测：基于历史数据预测可能发生的故障

请用专业、友好的语气回答。如果涉及具体数据，可以要求用户提供设备ID或时间范围。
始终以"您好，我是智能校园助手"开头。`

function formatMessage(content) {
  if (!content) return ''
  return content
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/`(.*?)`/g, '<code>$1</code>')
    .replace(/\n/g, '<br>')
}

function scrollToBottom() {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTo({
        top: chatContainer.value.scrollHeight,
        behavior: 'smooth'
      })
    }
  })
}

async function handleSend() {
  const text = inputMessage.value.trim()
  if (!text || loading.value) return

  messages.value.push({
    role: 'user',
    content: text,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  })

  inputMessage.value = ''
  loading.value = true
  scrollToBottom()

  try {
    const historyMessages = settings.contextEnabled
      ? messages.value.slice(0, -1).map(m => ({ role: m.role, content: m.content }))
      : []

    const response = await aiChat(historyMessages, systemPrompt)

    messages.value.push({
      role: 'assistant',
      content: response.data || response,
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    })
  } catch (error) {
    ElMessage.error('AI 回复失败，请重试')
    messages.value.push({
      role: 'assistant',
      content: '抱歉，AI 服务暂时不可用，请稍后再试。',
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

async function sendQuickMessage(prompt) {
  inputMessage.value = prompt
  await handleSend()
}

function clearChat() {
  messages.value = []
}

onMounted(() => {
  setTimeout(() => {
    messages.value.push({
      role: 'assistant',
      content: '您好，我是智能校园助手！我可以帮助您进行设备诊断、故障分析、维护建议等多种智能服务。请选择上方的快捷功能或直接输入您的问题。',
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    })
  }, 500)
})
</script>

<style scoped>
.ai-assistant-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 112px);
  background: linear-gradient(180deg, #f0f2f5 0%, #e8ecf0 100%);
}

/* 头部 */
.ai-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 24px;
  margin: -24px -24px 0 -24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.ai-avatar {
  width: 56px;
  height: 56px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  backdrop-filter: blur(10px);
}

.header-info h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: white;
}

.header-info p {
  margin: 4px 0 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 20px;
  font-size: 13px;
  color: white;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #67c23a;
  animation: pulse 2s infinite;
}

.status-badge.offline .status-dot {
  background: #f56c6c;
  animation: none;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.header-right :deep(.el-button) {
  color: white;
  background: rgba(255, 255, 255, 0.15);
  border: none;
}

.header-right :deep(.el-button:hover) {
  background: rgba(255, 255, 255, 0.25);
}

/* 聊天内容区 */
.ai-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

/* 欢迎界面 */
.welcome-section {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.welcome-animation {
  text-align: center;
  margin-bottom: 32px;
}

.greeting-icon {
  width: 100px;
  height: 100px;
  margin: 0 auto 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.ai-greeting h3 {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px;
}

.ai-greeting p {
  font-size: 15px;
  color: #909399;
  margin: 0;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
}

.quick-action-card {
  padding: 24px 20px;
  background: white;
  border-radius: 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.quick-action-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.2);
}

.action-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.action-label {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.action-desc {
  display: block;
  font-size: 12px;
  color: #909399;
}

/* 消息列表 */
.message-list {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.message-item {
  display: flex;
  gap: 14px;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message-avatar.user {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.message-avatar.assistant {
  background: white;
  color: #667eea;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.message-content {
  max-width: 70%;
}

.message-bubble {
  padding: 14px 18px;
  border-radius: 18px;
  line-height: 1.7;
  font-size: 14px;
  word-break: break-word;
}

.message-bubble.user {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 6px;
}

.message-bubble.assistant {
  background: white;
  color: #303133;
  border-bottom-left-radius: 6px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.message-bubble code {
  background: rgba(0, 0, 0, 0.06);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
}

.message-bubble.user code {
  background: rgba(255, 255, 255, 0.2);
}

.message-time {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 6px;
  padding: 0 6px;
}

.message-item.user .message-time {
  text-align: right;
}

/* 加载动画 */
.message-bubble.loading {
  display: flex;
  align-items: center;
  gap: 12px;
}

.typing-indicator {
  display: flex;
  gap: 4px;
}

.typing-indicator .dot {
  width: 8px;
  height: 8px;
  background: #667eea;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.typing-indicator .dot:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator .dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.loading-text {
  font-size: 13px;
  color: #909399;
}

/* 输入区域 */
.ai-input-section {
  background: white;
  padding: 16px 24px 20px;
  border-top: 1px solid #f0f2f5;
  margin: 0 -24px -24px;
}

.input-container {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.input-wrapper {
  flex: 1;
}

.input-wrapper :deep(.el-textarea__inner) {
  border-radius: 14px;
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.6;
}

.send-btn {
  height: 68px;
  padding: 0 28px;
  border-radius: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.3s;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
}

.send-btn:disabled {
  background: #dcdfe6;
  border-color: #dcdfe6;
}

.input-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  padding: 0 4px;
}

.tip, .capability {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #c0c4cc;
}

/* 设置抽屉 */
.settings-content {
  padding: 0 8px;
}

.settings-section {
  margin-bottom: 28px;
}

.settings-section h4 {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f2f5;
}

.settings-item {
  margin-bottom: 20px;
}

.settings-item label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 8px;
}

.settings-hint {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
}

/* 响应式 */
@media (max-width: 768px) {
  .ai-header {
    padding: 16px;
    margin: -24px -16px 0 -16px;
  }
  
  .ai-content {
    padding: 16px;
  }
  
  .ai-input-section {
    padding: 12px 16px 16px;
    margin: 0 -16px -16px;
  }
  
  .header-info h2 {
    font-size: 18px;
  }
  
  .message-content {
    max-width: 85%;
  }
  
  .quick-actions {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
