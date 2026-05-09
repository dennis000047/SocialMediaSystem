<template>
  <div class="login-container">
    <h1>會員登入</h1>
    <div class="form-group">
      <label>手機號碼：</label>
      <input :value="'09' + loginForm.phoneInput" type="tel" placeholder="請輸入手機號碼後8碼" @input="handlePhoneInput" />
      <div v-if="!isValidPhone" class="error-message">手機格式錯誤</div>
    </div>
    <div class="form-group">
      <label>密碼：</label>
      <input v-model="loginForm.password" type="password" placeholder="請輸入密碼" />
    </div>
    <button @click="handleLogin">登入</button>
    <p>還沒有帳號？ <router-link to="/register">註冊新帳號</router-link></p>
  </div>
</template>

<script setup lang="ts">
import { reactive, computed } from 'vue'
import axios from 'axios'
import Swal from 'sweetalert2'
import { useRouter } from 'vue-router' // 1. 必須 import 這個 Hook

const router = useRouter() // 2. 必須在 setup 頂層執行它並賦值給 router

const apiBase = 'http://localhost:8080/api'
const loginUrl = `${apiBase}/login`

const loginForm = reactive({
  phone: '09',
  phoneInput: '',
  password: ''
})

const isValidPhone = computed(() => loginForm.phone.match(/^09\d{8}$/))

const handlePhoneInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  let value = target.value

  // 確保至少以09開頭，且長度至少2
  if (value.length < 2 || !value.startsWith('09')) {
    value = '09'
  }

  // 提取後8碼數字
  const inputPart = value.substring(2).replace(/\D/g, '').substring(0, 8)
  loginForm.phoneInput = inputPart
  loginForm.phone = '09' + inputPart

  // 更新input值
  target.value = '09' + inputPart
}

const handleLogin = async () => {
  if (!isValidPhone.value) {
    await Swal.fire({ title: '失敗', text: '手機格式錯誤', icon: 'error', draggable: true })
    return
  }
  try {
    console.log('handleLogin start', loginForm)

    const payload = {
      phone: loginForm.phone,
      password: loginForm.password,
    }
    const response = await axios.post(loginUrl, payload)

    console.log('POST', loginUrl, 'response:', response.data)

    if (response.data?.success) {
      const token = response.data.token
      const userId = response.data.userId
      const userName = response.data.userName

      console.log('login success token:', token, 'userId:', userId, 'userName:', userName)

      localStorage.setItem('authToken', token)
      localStorage.setItem('userId', String(userId))
      localStorage.setItem('userName', String(userName))

      axios.defaults.headers.common.Authorization = `Bearer ${token}`

      await Swal.fire({ title: '成功', icon: 'success', draggable: true })
      router.push('/message')
    } else {
      await Swal.fire({ title: '失敗', text: response.data?.message || '登入失敗', icon: 'error', draggable: true })
    }
  } catch (error) {
    console.error('登入失敗', error)
    await Swal.fire({ title: '失敗', text: '登入發生錯誤', icon: 'error', draggable: true })
  }
}
</script>



<style scoped>
.login-container {
  max-width: 400px;
  margin: 50px auto;
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 8px;
}
.form-group {
  margin-bottom: 15px;
}
input {
  width: 100%;
  padding: 8px;
  margin-top: 5px;
}
button {
  width: 100%;
  padding: 10px;
  background-color: #42b983;
  color: white;
  border: none;
  cursor: pointer;
}
.error-message {
  color: red;
  font-size: 14px;
  margin-top: 5px;
}
</style>
