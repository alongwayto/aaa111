<template>
  <div class="ai-assistant-container">
    <div class="ai-header">
      <div class="header-left">
        <el-icon size="28" color="#409eff"><MagicStick /></el-icon>
        <div class="header-info">
          <h2>智能校园助手</h2>
          <p>基于大模型的智能设备管理助手</p>
        </div>
      </div>
      <div class="header-right">
        <el-tag :type="connected ? 'success' : 'danger'" size="small">
          {{ connected ? '在线' : '离线' }}
        </el-tag>
        <el-button size="small" @click="clearChat">
          <el-icon><Delete /></el-icon>
          清空对话
        </el-button>
      </div>
    </div>

    <div class="ai-content" ref="chatContainer">
      <div class="welcome-section" v-if="messages.length === 0">
        <div class="welcome-icon">
          <el-icon :size="60" color="#409eff"><ChatDotRound /></el-icon>
        </div>
        <h3>你好，我是智能校园助手</h3>
        <p>我可以帮你完成以下任务：</p>
        <div class="quick-actions">
          <el-button 
            v-for="action in quickActions" 
            :key="action.label"
            class="quick-btn"
            @click="sendQuickMessage(action.prompt)"
          >
            <el-icon><component :is="action.icon" /></el-icon>
            {{ action.label }}
          </el-button>
        </div>
      </div>

      <div class="message-list">
        <div 
          v-for="(msg, index) in messages" 
          :key="index" 
          class="message-item"
          :class="msg.role"
        >
          <div class="message-avatar">
            <el-icon v-if="msg.role === 'user'" size="24"><User /></el-icon>
            <el-icon v-else size="24" color="#409eff"><MagicStick /></el-icon>
          </div>
          <div class="message-content">
            <div class="message-bubble" v-html="formatMessage(msg.content)"></div>
            <div class="message-time">{{ msg.time }}</div>
          </div>
        </div>

        <div v-if="loading" class="message-item assistant">
          <div class="message-avatar">
            <el-icon size="24" color="#409eff"><MagicStick /></el-icon>
          </div>
          <div class="message-content">
            <div class="message-bubble loading">
              <span class="loading-dot"></span>
              <span class="loading-dot"></span>
              <span class="loading-dot"></span>
              思考中...
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="ai-input-section">
      <div class="input-wrapper">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="2"
          placeholder="输入你的问题，支持设备诊断、故障分析、维护建议等..."
          @keydown.enter.ctrl="handleSend"
          @keydown.enter.meta="handleSend"
          :disabled="loading"
        />
        <el-button 
          type="primary" 
          :loading="loading"
          @click="handleSend"
          class="send-btn"
        >
          <el-icon><Promotion /></el-icon>
          发送
        </el-button>
      </div>
      <div class="input-tips">
        <span>按 Ctrl+Enter 发送</span>
        <span>|</span>
        <span>支持多轮对话</span>
      </div>
    </div>

    <el-drawer v-model="showSettings" title="助手设置" size="400px">
      <div class="settings-content">
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
        <div class="settings-item">
          <label>包含上下文</label>
          <el-switch v-model="settings.contextEnabled" />
          <span class="settings-hint">开启后可进行多轮对话</span>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  MagicStick, ChatDotRound, User, Promotion, Delete, 
  Monitor, Warning, Tools, TrendCharts, HelpFilled
} from '@element-plus/icons-vue'
import { aiChat, aiDiagnose, aiMaintenanceAdvice } from '@/api/ai'

const chatContainer = ref(null)
const inputMessage = ref('')
const loading = ref(false)
const connected = ref(true)
const showSettings = ref(false)

const settings = reactive({
  model: 'doubao-pro',
  style: 'professional',
  contextEnabled: true
})

const messages = ref([])

const quickActions = [
  { label: '设备诊断', icon: 'Monitor', prompt: '帮我分析一下近期设备故障的原因和规律' },
  { label: '维护建议', icon: 'Tools', prompt: '给出一些设备维护的优化建议' },
  { label: '故障分析', icon: 'Warning', prompt: '分析一下本月故障率上升的原因' },
  { label: '数据报表', icon: 'TrendCharts', prompt: '生成一份设备运行分析报告' },
  { label: '使用帮助', icon: 'HelpFilled', prompt: '介绍一下这个系统的主要功能' }
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
  // 简单的 markdown 格式化
  return content
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/`(.*?)`/g, '<code>$1</code>')
    .replace(/\n/g, '<br>')
}

function scrollToBottom() {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
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
  // 添加欢迎消息
  setTimeout(() => {
    messages.value.push({
      role: 'assistant',
      content: '您好，我是智能校园助手！我可以帮助您进行设备诊断、故障分析、维护建议等多种智能服务。请选择右侧的快捷功能或直接输入您的问题。',
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    })
  }, 500)
})
</script>

<style scoped>
.ai-assistant-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.ai-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border-radius: 12px 12px 0 0;
  color: white;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-info h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.header-info p {
  margin: 4px 0 0;
  font-size: 12px;
  opacity: 0.9;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-right .el-tag {
  background: rgba(255, 255, 255, 0.2);
  border: none;
}

.header-right .el-button {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
}

.header-right .el-button:hover {
  background: rgba(255, 255, 255, 0.3);
}

.ai-content {
  flex: 1;
  overflow-y: auto;
  background: #f5f7fa;
  padding: 20px;
}

.welcome-section {
  text-align: center;
  padding: 40px 20px;
}

.welcome-icon {
  margin-bottom: 20px;
}

.welcome-section h3 {
  color: #303133;
  margin-bottom: 12px;
}

.welcome-section p {
  color: #909399;
  margin-bottom: 24px;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
}

.quick-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border-radius: 20px;
  background: white;
  border: 1px solid #e4e7ed;
  color: #606266;
  transition: all 0.3s;
}

.quick-btn:hover {
  border-color: #409eff;
  color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  gap: 12px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.message-item.user .message-avatar {
  background: #409eff;
  color: white;
}

.message-item.assistant .message-avatar {
  background: white;
  color: #409eff;
}

.message-content {
  max-width: 75%;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 16px;
  line-height: 1.6;
  word-break: break-word;
}

.message-item.user .message-bubble {
  background: #409eff;
  color: white;
  border-bottom-right-radius: 4px;
}

.message-item.assistant .message-bubble {
  background: white;
  color: #303133;
  border-bottom-left-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.message-bubble code {
  background: #f0f0f0;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
}

.message-item.user .message-bubble code {
  background: rgba(255, 255, 255, 0.2);
}

.message-time {
  font-size: 11px;
  color: #909399;
  margin-top: 4px;
  padding: 0 8px;
}

.message-item.user .message-time {
  text-align: right;
}

.message-bubble.loading {
  display: flex;
  align-items: center;
  gap: 4px;
}

.loading-dot {
  width: 6px;
  height: 6px;
  background: #409eff;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.loading-dot:nth-child(1) { animation-delay: -0.32s; }
.loading-dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.ai-input-section {
  background: white;
  padding: 16px 20px;
  border-radius: 0 0 12px 12px;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.05);
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.input-wrapper .el-textarea {
  flex: 1;
}

.send-btn {
  height: 68px;
  padding: 0 24px;
}

.input-tips {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.settings-content {
  padding: 20px;
}

.settings-item {
  margin-bottom: 24px;
}

.settings-item label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #303133;
}

.settings-hint {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
