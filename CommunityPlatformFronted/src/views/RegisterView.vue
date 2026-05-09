<template>
  <div class="register-container">
    <h1>註冊帳號</h1>
    <div class="form-group">
      <label>手機號碼：</label>
      <input :value="'09' + registerForm.phoneInput" type="tel" placeholder="請輸入手機號碼後8碼" @input="PhoneInput" />
      <div v-if="!isValidPhone" class="error-message">手機格式錯誤</div>
    </div>
    <div class="form-group">
      <label>使用者名稱：</label>
      <input v-model="registerForm.userName" type="text" placeholder="請輸入暱稱" />
    </div>
    <div class="form-group">
      <label>密碼：</label>
      <input v-model="registerForm.password" type="password" placeholder="請輸入密碼" maxlength="10" />
      <div v-if="!isValidPassword" class="error-message">密碼格式錯誤</div>
    </div>
    <button @click="RegisterBTN">註冊</button>
    <p>已有帳號？ <router-link to="/login">點我登入</router-link></p>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive } from 'vue'

import axios from 'axios'
import Swal from 'sweetalert2'
import { useRouter } from 'vue-router'

const router = useRouter()

const apiBase = 'http://localhost:8080/api'
const registerUrl = `${apiBase}/register`

const registerForm = reactive({
  phone: '09',
  phoneInput: '',
  password: '',
  userName: ''
})

const isValidPhone = computed(() => registerForm.phone.match(/^09\d{8}$/))
const isValidPassword = computed(() => registerForm.password.length > 0 && registerForm.password.length <= 10)

const PhoneInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  let value = target.value

  // 確保至少以09開頭，且長度至少2
  if (value.length < 2 || !value.startsWith('09')) {
    value = '09'
  }

  // 提取後8碼數字
  const inputPart = value.substring(2).replace(/\D/g, '').substring(0, 8)
  registerForm.phoneInput = inputPart
  registerForm.phone = '09' + inputPart

  // 更新input值
  target.value = '09' + inputPart
}

const RegisterBTN = async () => {
    if (!isValidPhone.value) {
      await Swal.fire(
        {
          title: '失敗',
          text: '手機格式錯誤',
          icon: 'error',
          draggable: true })
      return
    }
    if (!isValidPassword.value) {
      await Swal.fire(
        {
          title: '失敗',
          text: '密碼格式錯誤',
          icon: 'error', draggable: true
        }
      )
      return
    }
    try {
      const payload = {
        phone: registerForm.phone,
        password: registerForm.password,
        userName: registerForm.userName
      }
      const response = await axios.post(registerUrl, payload)
      console.log('POST', registerUrl, 'response:', response.data)
      if(response.data.success){
        await Swal.fire(
          { title: '成功',
           icon: 'success',
            draggable: true 
          }
        )
        router.push('/login')

      }else{
        await Swal.fire(
          { title: '失敗',
            text: '註冊失敗：' + response.data.message,
            icon: 'error',
            draggable: true })
        console.log(response)
      }
    } catch (error) {
      console.error('註冊失敗', error)
      await Swal.fire(
        { title: '失敗',
          text: '註冊發生錯誤',
          icon: 'error',
          draggable: true }
        )
    }
}
</script>



<style scoped>
.register-container {
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