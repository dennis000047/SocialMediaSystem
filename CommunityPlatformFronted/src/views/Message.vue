<template>
  <!-- 頂部欄位：使用者資訊及登出 -->
  <div class="header">
    <div class="user-info">
      <span>當前用戶：{{ displayCurrentUser }}</span>
      <button @click="handleLogout" class="logout-btn">登出</button>
    </div>
  </div>

  <!-- 主要內容區 -->
  <div class="container">
    <h1>社群平台 - 發文區</h1>

    <!-- 新增訊息按鈕 -->
    <div class="action-bar">
      <button @click="openAddPostDialog" class="add-btn">+ 新增發文</button>
    </div>

    <!-- 訊息表格 -->
    <table class="message-table">
      <thead>
        <tr>
          <th style="width: 10%">發文者</th>
          <th style="width: 40%">內容</th>
          <th style="width: 20%">發文時間</th>
          <th style="width: 30%">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="message in messages" :key="message.id" class="message-row">
          <td><strong>{{ message.author }}</strong></td>
          <td>
            <div class="content-display">
              {{ message.content }}
            </div>
          </td>
          <td class="time-cell">{{ formatTime(message.timestamp) }}</td>
          <td class="action-cell">
            <!-- 本人可以編輯/刪除 -->
            <div v-if="message.author === currentUserName" class="action-buttons">
              <button @click="startEdit(message.id, message.content)" class="edit-btn">編輯</button>
              <button @click="deleteMessage(message.id)" class="delete-btn">刪除</button>
              <button @click="openCommentDialog(message.id)" class="comment-btn">留言 ({{ message.comments.length }})</button>
            </div>
            <!-- 其他人可以留言 -->
            <div v-else class="action-buttons">
              <button @click="openCommentDialog(message.id)" class="comment-btn">留言 ({{ message.comments.length }})</button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>

  <!-- 新增發文 Dialog -->
  <div v-if="addPostDialogVisible" class="dialog-overlay" @click.self="closeAddPostDialog">
    <div class="dialog-content">
      <div class="dialog-header">
        <h2>新增發文</h2>
        <button @click="closeAddPostDialog" class="close-btn">&times;</button>
      </div>

      <div class="dialog-body">
        <div class="form-group">
          <label>發文者：</label>
          <p class="form-value">{{ currentUserName || displayCurrentUser }}</p>
        </div>

        <div class="form-group">
          <label>發文內容：</label>
          <textarea v-model="newPostContent" class="comment-textarea" placeholder="請輸入發文內容" maxlength="500"></textarea>
          <span class="char-count">{{ newPostContent.length }}/500</span>
        </div>
      </div>

      <div class="dialog-footer">
        <button @click="submitNewPost" class="publish-btn">發布</button>
        <button @click="closeAddPostDialog" class="cancel-btn">取消</button>
      </div>
    </div>
  </div>

  <!-- 編輯發文 Dialog -->
  <div v-if="editDialogVisible" class="dialog-overlay" @click.self="closeEditDialog">
    <div class="dialog-content">
      <div class="dialog-header">
        <h2>編輯發文</h2>
        <button @click="closeEditDialog" class="close-btn">&times;</button>
      </div>

      <div class="dialog-body">
        <div class="form-group">
          <label>發文者：</label>
          <p class="form-value">{{ currentUserName || displayCurrentUser }}</p>
        </div>
        <div class="form-group">
          <label>編輯內容：</label>
          <textarea v-model="editContent" class="comment-textarea" placeholder="請修改發文內容" maxlength="500"></textarea>
          <span class="char-count">{{ editContent.length }}/500</span>
        </div>
      </div>

      <div class="dialog-footer">
        <button @click="saveEdit()" class="publish-btn">保存</button>
        <button @click="closeEditDialog" class="cancel-btn">取消</button>
      </div>
    </div>
  </div>

  <!-- 留言 Dialog -->
  <div v-if="commentDialogVisible" class="dialog-overlay" @click.self="closeCommentDialog">
    <div class="comment-dialog-content">
      <div class="dialog-header">
        <h2>留言區</h2>
        <button @click="closeCommentDialog" class="close-btn">&times;</button>
      </div>

      <div class="dialog-body comment-dialog-body">
        <!-- 原始發文信息 -->
        <div class="original-post">
          <div class="post-info">
            <strong>{{ getCommentingMessage?.author }}</strong>
            <span class="post-time">{{ formatTime(getCommentingMessage?.timestamp) }}</span>
          </div>
          <div class="post-content">{{ getCommentingMessage?.content }}</div>
        </div>

        <!-- 新增留言區 -->
        <div class="new-comment-section">
          <label>你的留言：</label>
          <textarea v-model="commentContent" class="comment-textarea" placeholder="請輸入留言內容" maxlength="300"></textarea>
          <span class="char-count">{{ commentContent.length }}/300</span>
        </div>

        <!-- 留言列表 (DataTable) -->
        <div class="comments-list-section">
          <h4>所有留言 ({{ getCommentingMessage?.comments.length || 0 }})</h4>
          <table v-if="getCommentingMessage?.comments.length" class="comments-table">
            <thead>
              <tr>
                <th style="width: 15%">留言者</th>
                <th style="width: 35%">內容</th>
                <th style="width: 20%">時間</th>
                <th style="width: 30%">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(comment, index) in getCommentingMessage?.comments" :key="index" class="comment-row">
                <td><strong>{{ comment.author }}</strong></td>
                <td>
                  <!-- 編輯模式 -->
                  <div v-if="editingCommentId === comment.id" class="edit-mode">
                    <textarea v-model="editCommentContent" class="edit-textarea-small"></textarea>
                    <div class="edit-buttons-small">
                      <button @click="saveCommentEdit(comment.id)" class="save-btn">保存</button>
                      <button @click="cancelCommentEdit" class="cancel-btn">取消</button>
                    </div>
                  </div>
                  <div v-else>{{ comment.content }}</div>
                </td>
                <td class="time-cell">{{ formatTime(comment.timestamp) }}</td>
                <td class="action-cell">
                  <div v-if="comment.authorId  === currentUserId" class="action-buttons-small">
                    <button @click="startCommentEdit(comment.id, comment.content)" class="edit-btn-small">編輯</button>
                    <button @click="deleteComment(comment.id)" class="delete-btn-small">刪除</button>
                  </div>
                  <div v-else class="action-buttons-small">
                    <span class="no-action">無操作</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
          <div v-else class="no-comments">暫無留言</div>
        </div>
      </div>

      <div class="dialog-footer">
        <button @click="submitComment" class="publish-btn">發布留言</button>
        <button @click="closeCommentDialog" class="cancel-btn">關閉</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Swal from 'sweetalert2'

const router = useRouter()

// 抓使用者
const currentUserName = ref(localStorage.getItem('userName') || '')
const currentUserId = ref(localStorage.getItem('userId') || '')
const displayCurrentUser = computed(() => currentUserName.value || currentUserId.value || '未知用戶')

// 訊息資料結構
interface Comment {
  id: number
  author: string
  authorId: string
  content: string
  timestamp: Date
}

interface Message {
  id: number
  author: string
  content: string
  timestamp: Date
  comments: Comment[]
}

const apiBase = 'http://localhost:8080/api'
const getPostInfoUrl = `${apiBase}/getpostinfo`
const addPostUrl = `${apiBase}/addPost`
const editPostUrl = `${apiBase}/editPost`
const deleteMsgUrl = `${apiBase}/deletemsg`
const commentMsgUrl = `${apiBase}/commentmsg`
const editCommentUrl = `${apiBase}/editComment`
const deleteCommentUrl = `${apiBase}/deleteComment`

// 從後端取得發文列表
const messages = ref<Message[]>([])

const authToken = localStorage.getItem('authToken')
if (authToken) {
  axios.defaults.headers.common.Authorization = `Bearer ${authToken}`
  console.log('auth token loaded from localStorage')
}

const showSwal = async (success: boolean, text: string) => {
  await Swal.fire({
    title: success ? '成功' : '失敗',
    text,
    icon: success ? 'success' : 'error',
    draggable: true
  })
}

const fetchMessages = async () => {
  console.log('fetchMessages() start', { authToken })
  if (!authToken) {
    console.warn('沒有可用的 auth token，導向登入頁')
    router.push('/login')
    return
  }
  try {
    const response = await axios.get(getPostInfoUrl)
    console.log('GET', getPostInfoUrl, 'response:', response.data)
    const posts = Array.isArray(response.data)
      ? response.data.map((post: any) => ({
          id: post.id ?? post.postId,
          author: post.authorName || post.author || '匿名',
          content: post.content,
          timestamp: new Date(post.createdAt || post.timestamp || Date.now()),
          comments: Array.isArray(post.comments)
            ? post.comments.map((comment: any) => ({
                id: comment.commentId,           
                author: comment.userName,         
                authorId: String(comment.userId), // 後端是 userId
                content: comment.content,
                timestamp: new Date(comment.createdAt)
              }))
            : []
        }))
      : []
    messages.value = posts
  } catch (error) {
    console.error('fetchMessages() error:', error)
  }
}
onMounted(() => {
  fetchMessages()
})

// 計算當前評論的訊息
const getCommentingMessage = computed(() => {
  return messages.value.find(m => m.id === commentingMessageId.value)
})

// 編輯訊息相關
const editingId = ref<number | null>(null)
const editContent = ref('')
const editDialogVisible = ref(false)

// 編輯留言相關
const editingCommentId = ref<number | null>(null)
const editCommentContent = ref('')

// 新增發文對話框
const addPostDialogVisible = ref(false)
const newPostContent = ref('')

// 留言 dialog 相關
const commentDialogVisible = ref(false)
const commentingMessageId = ref<number | null>(null)
const commentContent = ref('')

// 格式化時間
const formatTime = (date?: Date): string => {
  if (!date) return ''
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMins < 1) return '剛剛'
  if (diffMins < 60) return `${diffMins} 分鐘前`
  if (diffHours < 24) return `${diffHours} 小時前`
  if (diffDays < 7) return `${diffDays} 天前`
  
  return date.toLocaleDateString('zh-TW')
}

// 開啟新增發文對話框
const openAddPostDialog = () => {
  newPostContent.value = ''
  addPostDialogVisible.value = true
}

// 關閉新增發文對話框
const closeAddPostDialog = () => {
  addPostDialogVisible.value = false
  newPostContent.value = ''
}

// 提交新發文
const submitNewPost = async () => {
  if (!newPostContent.value.trim()) {
    await showSwal(false, '請輸入發文內容')
    return
  }

  const payload = {
    authorId: currentUserId.value,
    content: newPostContent.value,
    image: null
  }
  console.log('submitNewPost() start, payload:', payload)

  try {
    const response = await axios.post(addPostUrl, payload)
    console.log('POST', addPostUrl, 'response:', response.data)
    const newId = messages.value.length ? Math.max(...messages.value.map(m => m.id)) + 1 : 1
    const newMessage = {
      id: response.data?.id ?? response.data?.postId ?? newId,
      author: response.data?.author || response.data?.authorName || currentUserName.value || currentUserId.value,
      content: newPostContent.value,
      timestamp: response.data?.timestamp ? new Date(response.data.timestamp) : new Date(),
      comments: Array.isArray(response.data?.comments) ? response.data.comments.map((comment: any) => ({
        id: comment.id,
        author: comment.author,
        content: comment.content,
        timestamp: new Date(comment.timestamp)
      })) : []
    }

    messages.value.unshift(newMessage)
    await showSwal(true, '')
    closeAddPostDialog()
  } catch (error) {
    console.error('submitNewPost() error:', error)
    await showSwal(false, '發文失敗，請稍後再試!')
  }
}

// 留言dialog
const openCommentDialog = (messageId: number) => {
  commentingMessageId.value = messageId
  commentContent.value = ''
  commentDialogVisible.value = true
}

// 發布留言
const submitComment = async () => {
  if (!commentingMessageId.value || !commentContent.value.trim()) {
    await showSwal(false, '請輸入留言內容')
    return
  }

  const payload = {
    postId: commentingMessageId.value,
    authorId: currentUserId.value,
    authorName: currentUserName.value,
    content: commentContent.value
  }
  console.log('submitComment() start, payload:', payload)

  try {
    const response = await axios.post(commentMsgUrl, payload)
    console.log('POST', commentMsgUrl, 'response:', response.data)

    const message = messages.value.find(m => m.id === commentingMessageId.value)
    if (message) {
      const savedComment = response.data?.comment || {
        id: response.data?.id ?? Math.max(0, ...message.comments.map(c => c.id)) + 1,
        author: currentUserName.value || currentUserId.value || '匿名',
        content: commentContent.value,
        timestamp: response.data?.timestamp ? new Date(response.data.timestamp) : new Date()
      }
      message.comments.push({
        id: savedComment.id,
        author: savedComment.author,
        authorId: String(savedComment.userId ?? currentUserId.value),
        content: savedComment.content,
        timestamp: savedComment.timestamp instanceof Date ? savedComment.timestamp : new Date(savedComment.timestamp)
      })
    }

    commentContent.value = ''
    await showSwal(true, '')
  } catch (error) {
    console.error('submitComment() error:', error)
    await showSwal(false, '留言失敗，請稍後再試')
  }
}

// 關閉留言 dialog
const closeCommentDialog = () => {
  commentDialogVisible.value = false
  commentingMessageId.value = null
  commentContent.value = ''
  editingCommentId.value = null
  editCommentContent.value = ''
}

// 編輯訊息
const startEdit = (id: number, content: string) => {
  editingId.value = id
  editContent.value = content
  editDialogVisible.value = true
}

// 保存編輯發文
const saveEdit = async () => {
  const id = editingId.value
  if (id === null) {
    await showSwal(false, '找不到要編輯的發文')
    return
  }

  const message = messages.value.find(m => m.id === id)
  if (!message || !editContent.value.trim()) {
    await showSwal(false, '請輸入訊息內容')
    return
  }

  const payload = {
    id,
    content: editContent.value,
    userId: currentUserId.value,
    image: null
  }
  console.log('saveEdit() start, payload:', payload)

  try {
    const response = await axios.post(editPostUrl, payload)
    console.log('POST', editPostUrl, 'response:', response.data)
    message.content = editContent.value
    editingId.value = null
    editContent.value = ''
    editDialogVisible.value = false
    await showSwal(true, '')
  } catch (error) {
    console.error('saveEdit() error:', error)
    await showSwal(false, '保存失敗，請稍後再試')
  }
}

// 取消編輯
const closeEditDialog = () => {
  editDialogVisible.value = false
  editingId.value = null
  editContent.value = ''
}

// 刪除訊息
const deleteMessage = async (id: number) => {
  if (!confirm('確定要刪除這則發文嗎？')) {
    return
  }

  const payload = {
    id: id,
    userId: currentUserId.value
  }
  console.log('deleteMessage() start, payload:', payload)

  try {
    const response = await axios.post(deleteMsgUrl, payload)
    console.log('POST', deleteMsgUrl, 'response:', response.data)
    const index = messages.value.findIndex(m => m.id === id)
    if (index > -1) {
      messages.value.splice(index, 1)
    }
    await showSwal(true, '')
  } catch (error) {
    console.error('deleteMessage() error:', error)
    await showSwal(false, '刪除失敗，請稍後再試')
  }
}

// 編輯留言
const startCommentEdit = (commentId: number, content: string) => {
  editingCommentId.value = commentId
  editCommentContent.value = content
}

// 保存編輯留言

const saveCommentEdit = async (commentId: number) => {
  const message = getCommentingMessage.value
  if (!message) return

  const comment = message.comments.find(c => c.id === commentId)
  if (!comment || !editCommentContent.value.trim()) {
    await showSwal(false, '請輸入留言內容')
    return
  }

  const payload = {
    commentId,
    userId: currentUserId.value,
    content: editCommentContent.value
  }

  try {
    const response = await axios.post(editCommentUrl, payload)
    if (response.data?.success === false) {
      await showSwal(false, response.data.message || '編輯失敗')
      return
    }
    comment.content = editCommentContent.value
    editingCommentId.value = null
    editCommentContent.value = ''
    await showSwal(true, '留言編輯成功')
  } catch (error) {
    console.error('saveCommentEdit() error:', error)
    await showSwal(false, '留言編輯失敗，請稍後再試')
  }
}

// 取消編輯留言
const cancelCommentEdit = () => {
  editingCommentId.value = null
  editCommentContent.value = ''
}

// 刪除留言

const deleteComment = async (commentId: number) => {
  if (!confirm('確定要刪除這則留言嗎？')) return

  const payload = {
    commentId,
    userId: currentUserId.value
  }

  try {
    const response = await axios.post(deleteCommentUrl, payload)
    if (response.data?.success === false) {
      await showSwal(false, response.data.message || '刪除失敗')
      return
    }
    const message = getCommentingMessage.value
    if (message) {
      const index = message.comments.findIndex(c => c.id === commentId)
      if (index > -1) {
        message.comments.splice(index, 1)
      }
    }
    await showSwal(true, '留言刪除成功')
  } catch (error) {
    console.error('deleteComment() error:', error)
    await showSwal(false, '留言刪除失敗，請稍後再試')
  }
}

// 登出
const handleLogout = () => {
  if (confirm('確定要登出嗎？')) {
    localStorage.removeItem('authToken')
    localStorage.removeItem('userId')
    localStorage.removeItem('userName')
    router.push('/login')
  }
}
</script>



<style scoped>
* {
  box-sizing: border-box;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 15px 20px;
  display: flex;
  justify-content: flex-end;
  border-bottom: 1px solid #ddd;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-info span {
  font-weight: bold;
  font-size: 16px;
  color: white;
}

.logout-btn {
  padding: 8px 16px;
  background-color: #f56565;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.logout-btn:hover {
  background-color: #e53e3e;
}

.container {
  padding: 30px 20px;
  max-width: 1400px;
  margin: 0 auto;
  background-color: #f8f9fa;
  min-height: 100vh;
}

h1 {
  color: #333;
  margin-bottom: 25px;
  font-size: 28px;
}

.action-bar {
  margin-bottom: 25px;
}

.add-btn {
  padding: 12px 24px;
  background-color: #48bb78;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(72, 187, 120, 0.3);
}

.add-btn:hover {
  background-color: #38a169;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(72, 187, 120, 0.4);
}

.message-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  background-color: white;
  border-radius: 8px;
  overflow: hidden;
}

.message-table thead {
  background-color: #f0f2f5;
  border-bottom: 2px solid #ddd;
}

.message-table th {
  padding: 15px 12px;
  text-align: left;
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.message-row {
  border-bottom: 1px solid #eee;
  transition: background-color 0.2s;
}

.message-row:hover {
  background-color: #f9f9f9;
}

.message-table td {
  padding: 15px 12px;
  font-size: 14px;
}

.content-display {
  color: #555;
  line-height: 1.5;
  word-break: break-word;
}

.time-cell {
  color: #999;
  font-size: 13px;
  white-space: nowrap;
}

.action-cell {
    text-align: left;
  }

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: flex-start;
  align-items: center;
}

.action-buttons-small {
  display: flex;
  gap: 6px;
  justify-content: center;
  flex-wrap: wrap;
}

.no-action {
  color: #999;
  font-size: 12px;
}

.edit-btn,
.delete-btn,
.comment-btn {
  padding: 7px 13px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  font-weight: bold;
  transition: all 0.3s;
}

.edit-btn {
  background-color: #4299e1;
  color: white;
}

.edit-btn:hover {
  background-color: #3182ce;
  transform: translateY(-1px);
}

.delete-btn {
  background-color: #f56565;
  color: white;
}

.delete-btn:hover {
  background-color: #e53e3e;
  transform: translateY(-1px);
}

.comment-btn {
  background-color: #9f7aea;
  color: white;
}

.comment-btn:hover {
  background-color: #805ad5;
  transform: translateY(-1px);
}

.edit-btn-small,
.delete-btn-small {
  padding: 4px 8px;
  border: none;
  border-radius: 3px;
  cursor: pointer;
  font-size: 11px;
  font-weight: bold;
  transition: all 0.3s;
}

.edit-btn-small {
  background-color: #4299e1;
  color: white;
}

.edit-btn-small:hover {
  background-color: #3182ce;
}

.delete-btn-small {
  background-color: #f56565;
  color: white;
}

.delete-btn-small:hover {
  background-color: #e53e3e;
}

.edit-mode {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.edit-textarea,
.edit-textarea-small {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  font-family: Arial, sans-serif;
  resize: vertical;
}

.edit-textarea {
  min-height: 80px;
}

.edit-textarea-small {
  min-height: 60px;
  font-size: 12px;
}

.edit-textarea:focus,
.edit-textarea-small:focus {
  outline: none;
  border-color: #4299e1;
  box-shadow: 0 0 0 2px rgba(66, 153, 225, 0.1);
}

.edit-buttons,
.edit-buttons-small {
  display: flex;
  gap: 6px;
}

.edit-buttons {
  margin-top: 6px;
}

.edit-buttons-small {
  margin-top: 4px;
  gap: 4px;
}

.save-btn {
  padding: 6px 12px;
  background-color: #48bb78;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  font-weight: bold;
  transition: background-color 0.3s;
}

.save-btn:hover {
  background-color: #38a169;
}

.cancel-btn {
  padding: 6px 12px;
  background-color: #a0aec0;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  font-weight: bold;
  transition: background-color 0.3s;
}

.cancel-btn:hover {
  background-color: #718096;
}

/* Dialog 樣式 */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  overflow-y: auto;
  padding: 20px;
}

.dialog-content {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  width: 90%;
  max-width: 500px;
  display: flex;
  flex-direction: column;
  max-height: 90vh;
  overflow: hidden;
}

.comment-dialog-content {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  width: 95%;
  max-width: 800px;
  display: flex;
  flex-direction: column;
  max-height: 90vh;
  overflow: hidden;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
  flex-shrink: 0;
}

.dialog-header h2 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: color 0.3s;
}

.close-btn:hover {
  color: #333;
}

.dialog-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

.comment-dialog-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.original-post {
  background-color: #f5f5f5;
  padding: 12px;
  border-radius: 6px;
  border-left: 4px solid #9f7aea;
}

.post-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
}

.post-time {
  color: #999;
}

.post-content {
  color: #555;
  line-height: 1.5;
  word-break: break-word;
}

.new-comment-section {
  border: 1px solid #eee;
  padding: 12px;
  border-radius: 6px;
  background-color: #fafafa;
}

.new-comment-section label {
  display: block;
  font-weight: bold;
  margin-bottom: 8px;
  color: #333;
  font-size: 14px;
}

.comments-list-section {
  flex: 1;
  overflow-y: auto;
  border: 1px solid #eee;
  border-radius: 6px;
  padding: 12px;
  background-color: white;
}

.comments-list-section h4 {
  margin: 0 0 12px 0;
  color: #333;
  font-size: 14px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.no-comments {
  text-align: center;
  color: #999;
  padding: 20px;
  font-size: 14px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-weight: bold;
  margin-bottom: 8px;
  color: #333;
}

.form-value {
  margin: 0;
  padding: 8px;
  background-color: #f5f5f5;
  border-radius: 4px;
  color: #666;
}

.char-count {
  display: block;
  text-align: right;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.comment-textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  font-family: Arial, sans-serif;
  resize: vertical;
  min-height: 100px;
  transition: border-color 0.3s;
}

.comment-textarea:focus {
  outline: none;
  border-color: #4299e1;
  box-shadow: 0 0 0 2px rgba(66, 153, 225, 0.1);
}

.comments-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.comments-table thead {
  background-color: #f5f5f5;
  border-bottom: 1px solid #ddd;
}

.comments-table th {
  padding: 8px;
  text-align: left;
  font-weight: 600;
  color: #333;
  font-size: 12px;
}

.comment-row {
  border-bottom: 1px solid #eee;
  transition: background-color 0.2s;
}

.comment-row:hover {
  background-color: #fafafa;
}

.comments-table td {
  padding: 8px;
  word-break: break-word;
}

.dialog-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  padding: 16px;
  border-top: 1px solid #eee;
  flex-shrink: 0;
  background-color: #f9f9f9;
}

.publish-btn {
  padding: 8px 16px;
  background-color: #48bb78;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: background-color 0.3s;
}

.publish-btn:hover {
  background-color: #38a169;
}
</style>