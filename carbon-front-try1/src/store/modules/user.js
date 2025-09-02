import { login, getInfo } from '@/api/user.js'
import { getToken, setToken, removeToken } from '@/utils/auth.js'
import router, { resetRouter } from '../../router'
import { isLoginApi } from '@/api/sms.js'
import Cookies from 'js-cookie'

const state = {
  token: getToken(),
  name: '',
  avatar: '',
  introduction: '',
  roles: [],
  projectPage: 1,
  isRemember: false,
  isLogin: Cookies.get('isLogin')
}

const mutations = {
  SET_TOKEN: (state, token) => {
    state.token = token
  },
  SET_PROJECTPAGE: (state, page) => {
    state.projectPage = page
  },
  SET_ISLOGIN: (state, isLogin) => {
    state.isLogin = isLogin
    Cookies.set(isLogin)
  },
  SET_INTRODUCTION: (state, introduction) => {
    state.introduction = introduction
  },
  SET_NAME: (state, name) => {
    state.name = name
  },
  SET_AVATAR: (state, avatar) => {
    state.avatar = avatar
  },
  SET_ROLES: (state, roles) => {
    state.roles = roles
  },
  SET_REMEMBER: (state, isRemember) => {
    state.isRemember = isRemember
  }
}

const actions = {
  login({ commit }, userInfo) {
    const { account, pwd, key, code, wxCode, rememberMe } = userInfo
    //清空cookie与token
    commit('SET_TOKEN', '')
    commit('SET_ROLES', [])
    removeToken()
    resetRouter()
/*    let apiBaseURL = "";
    if(window.localStorage.getItem("apiBaseURL")){
      apiBaseURL =  window.localStorage.getItem("apiBaseURL")
    }*/
    localStorage.clear();
    Cookies.remove('storeStaffList')
    Cookies.remove('JavaInfo')
    sessionStorage.removeItem('token')
    return new Promise((resolve, reject) => {
      login(userInfo).then(data => {
        const account = data.securityData;
        window.localStorage.setItem("accountName", data.securityData.accountName)
        commit('SET_NAME', data.securityData.accountName)
        commit('SET_TOKEN', data.token)
        if (rememberMe) {
          window.localStorage.setItem("carbonCookie", JSON.stringify(account))
          window.localStorage.setItem("carbonToken", data.token)
        }
        Cookies.set('JavaInfo', JSON.stringify(account))
        setToken(data.token)
        resolve()
      }).catch(error => {
        console.error(error);
        reject(error);
      })
    })
  },

  upAvatar({ commit },avatar){
    return new Promise((resolve, reject) => {
      commit('SET_AVATAR', avatar)
    })
  },

  updateName({ commit },name){
    return new Promise((resolve, reject) => {
      commit('SET_NAME', name)
    })
  },

  isLogin({ commit }, userInfo) {
    // const { username, password } = userInfo
    return new Promise((resolve, reject) => {
      isLoginApi().then(async res => {
        commit('SET_ISLOGIN', res.status)
        resolve(res)
      }).catch(res => {
        commit('SET_ISLOGIN', false)
        reject(res)
      })
    })
  },

  getInfo({ commit, state }) {
    return new Promise((resolve, reject) => {
      getInfo(state.token).then(data => {
        if (!data) {
          reject('Verification failed, please Login again.')
        }
        const { roles, account } = data

        if (!roles || roles.length <= 0) {
          reject('getInfo: roles must be a non-null array!')
        }

        commit('SET_ROLES', roles)
        // commit('SET_ROLES', ['admin'])
        commit('SET_NAME', account)
        // commit('SET_AVATAR', avatar)
        commit('SET_AVATAR', '#')
        commit('SET_INTRODUCTION', '#')
        resolve(data)
      }).catch(error => {
        reject(error)
      })
    })
  },
  setRemember({ commit }, dispatch) {
    commit('SET_REMEMBER', dispatch)
  },
  setProjectPage({ commit }, dispatch) {
    commit('SET_PROJECTPAGE', dispatch)
  },

  logout({ commit, state, dispatch }) {

    return new Promise((resolve, reject) => {
      commit('SET_TOKEN', '')
      commit('SET_ROLES', [])
      removeToken()
      resetRouter()
      let apiBaseURL = "";
      if (window.localStorage.getItem("apiBaseURL")){
        apiBaseURL =  window.localStorage.getItem("apiBaseURL")
      }
      localStorage.clear();
      localStorage.setItem("apiBaseURL",apiBaseURL)
      Cookies.remove('storeStaffList')
      Cookies.remove('JavaInfo')
      sessionStorage.removeItem('token')
      dispatch('tagsView/delAllViews', null, { root: true })

      resolve()
    })
  },

  resetToken({ commit }) {
    return new Promise(resolve => {
      commit('SET_TOKEN', '')
      commit('SET_ROLES', [])
      removeToken()
      resolve()
    })
  },

  setToken({ commit }, state) {
    return new Promise(resolve => {
      commit('SET_TOKEN', state.token)
      Cookies.set('JavaInfo', JSON.stringify(state))
      resolve()
    })
  },

  changeRoles({ commit, dispatch }, role) {
    return new Promise(async resolve => {
      const token = role + '-token'

      commit('SET_TOKEN', token)
      setToken(token)

      const { roles } = await dispatch('getInfo')

      resetRouter()
      const accessRoutes = await dispatch('permission/generateRoutes', roles, { root: true })
      router.addRoutes(accessRoutes)
      dispatch('tagsView/delAllViews', null, { root: true })
      resolve()
    })
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
