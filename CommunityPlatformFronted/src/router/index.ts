import { createRouter, createWebHistory } from 'vue-router'
import RegisterView from '../views/RegisterView.vue'
import LoginView from '../views/LoginView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login',
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
    },
    // {
    //   path: '/message',
    //   name: 'message',
    //   component: () => import('../views/AboutView.vue'),
    // },
    {
      path: '/message',
      name: 'message',
      component: () => import('../views/Message.vue'),
    },
  ],
})

export default router
