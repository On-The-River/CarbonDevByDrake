import request from '../utils/request'
import md5 from 'js-md5';
import { getToken } from '@/utils/auth'

const authURL = 'http://localhost:9001';

/**
 * 注册
 * @param {*} data
 * @returns {Promise}
 */
export function register(data) {
  data.password = md5(data.password)
  data.confirmPassword = md5(data.confirmPassword)
  return request({
    baseURL: authURL,
    url: '/authCenter/auth/register',
    method: 'post',
    data
  })
}
/**
 * 忘记密码
 * @param {*} data
 * @returns {Promise}
 */
export function putForgotPassword(data) {
  // data.confirmPassword = md5(data.confirmPassword)
  // data.password = md5(data.password)暂时注释
  return request({
    baseURL: authURL,
    url: '/authCenter/auth/forgotPassword',
    method: 'post',
    headers: {
      'Content-Type': 'application/json'
    },
    data
  })
}

/**
 * 登录
 * @returns {Promise}
 * @param info
 */
export function login(info) {
  const data = {
    "accountName": info.account,
    "password": md5(info.pwd),
    "captcha": "123456"
  };
  return request({
    baseURL: authURL,
    url: '/authCenter/auth/login',
    method: 'post',
    headers: {
      'Content-Type': 'application/json'
    },
    data
  })
}

/**
 * 验证账户是否存在
 * @param {*} data
 * @returns {Promise}
 * @param {string} param
 */
export function getAuthCenterAuthVerify(param) {
  return request({
    baseURL: authURL,
    url: '/authCenter/auth/verify/' + param,
    method: 'GET',
  })
}

export function getInfo(token) {
  return request({
    url: '/admin/getAdminInfoByToken',
    method: 'get',
    token: token
  })
}

/**
 * 退出登录
 * @param {*} data
 * @returns
 * {accountName}
 */
export function logout() {
  let token = getToken()
  return request({
    baseURL: authURL,
    url: '/authCenter/auth/logout',
    method: 'GET',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'token': token
    }
  })
}

/**
 * 会员管理 列表
 * @param pram
 */
export function userListApi(params) {
  return request({
    url: `/admin/user/list`,
    method: 'get',
    params
  })
}

/**
 * 会员管理 修改
 * @returns {Promise}
 * @param params
 * @param data
 */
export function userUpdateApi(params, data) {
  return request({
    url: `/admin/user/update`,
    method: 'post',
    params,
    data
  })
}

/**
 * 会员管理 详情
 * @returns {Promise}
 * @param params
 */
export function userInfoApi(params) {
  return request({
    url: `/admin/user/info`,
    method: 'get',
    params
  })
}

/**
 * 会员管理 账户详情
 * @returns {Promise}
 * @param params
 */
export function infobyconditionApi(params) {
  return request({
    url: `/admin/user/infobycondition`,
    method: 'get',
    params
  })
}

/**
 * 会员管理 账户详情top数据
 * @returns {Promise}
 * @param params
 */
export function topdetailApi(params) {
  return request({
    url: `/admin/user/topdetail`,
    method: 'get',
    params
  })
}

/**
 * 会员管理 批量设置分组
 * @returns {Promise}
 * @param params
 */
export function groupPiApi(params) {
  return request({
    url: `/admin/user/group`,
    method: 'post',
    params
  })
}

/**
 * 会员管理 批量设置标签
 * @param params
 */
export function tagPiApi(params) {
  return request({
    url: `/admin/user/tag`,
    method: 'post',
    params
  })
}

/**
 * 会员管理 积分余额
 * @param params
 */
export function foundsApi(params) {
  return request({
    url: `/admin/user/operate/founds`,
    method: 'get',
    params
  })
}

/**
 * 会员管理 删除
 * @param params
 */
export function userDeleteApi(params) {
  return request({
    url: `/admin/user/delete`,
    method: 'get',
    params
  })
}

/**
 * 会员等级 列表
 * @param params
 */
export function levelListApi(params) {
  return request({
    url: `/admin/system/user/level/list`,
    method: 'get',
    params
  })
}

/**
 * 会员等级 新增
 * @param data
 */
export function levelSaveApi(data) {
  return request({
    url: `/admin/system/user/level/save`,
    method: 'post',
    data
  })
}

/**
 * 会员等级 编辑
 * @param params
 * @param data
 */
export function levelUpdateApi(params, data) {
  return request({
    url: `/admin/system/user/level/update`,
    method: 'post',
    params,
    data
  })
}

/**
 * 会员等级 详情
 * @param params
 */
export function levelInfoApi(params) {
  return request({
    url: `/admin/system/user/level/info`,
    method: 'get',
    params
  })
}

/**
 * 会员等级 删除
 * @param params
 */
export function levelDeleteApi(params) {
  return request({
    url: `/admin/system/user/level/delete`,
    method: 'get',
    params
  })
}

/**
 * 会员等级 是否显示
 * @param params
 */
export function levelUseApi(params) {
  return request({
    url: `/admin/system/user/level/use`,
    method: 'get',
    params
  })
}

/**
 * 会员标签 列表
 * @param params
 */
export function tagListApi(params) {
  return request({
    url: `/admin/user/tag/list`,
    method: 'get',
    params
  })
}

/**
 * 会员标签 新增
 * @param pram
 */
export function tagSaveApi(data) {
  return request({
    url: `/admin/user/tag/save`,
    method: 'post',
    data
  })
}

/**
 * 会员标签 编辑
 * @param pram
 */
export function tagUpdateApi(params, data) {
  return request({
    url: `/admin/user/tag/update`,
    method: 'post',
    params,
    data
  })
}

/**
 * 会员标签 详情
 * @param pram
 */
export function tagInfoApi(params) {
  return request({
    url: `/admin/user/tag/info`,
    method: 'get',
    params
  })
}

/**
 * 会员标签 删除
 * @param pram
 */
export function tagDeleteApi(params) {
  return request({
    url: `/admin/user/tag/delete`,
    method: 'get',
    params
  })
}

/**
 * 会员分组 列表
 * @param pram
 */
export function groupListApi(params) {
  return request({
    url: `/admin/user/group/list`,
    method: 'get',
    params
  })
}

/**
 * 会员分组 新增
 * @param pram
 */
export function groupSaveApi(data) {
  return request({
    url: `/admin/user/group/save`,
    method: 'post',
    data
  })
}

/**
 * 会员分组 编辑
 * @param pram
 */
export function groupUpdateApi(params, data) {
  return request({
    url: `/admin/user/group/update`,
    method: 'post',
    params,
    data
  })
}

/**
 * 会员分组 详情
 * @param pram
 */
export function groupInfoApi(params) {
  return request({
    url: `/admin/user/group/info`,
    method: 'get',
    params
  })
}

/**
 * 会员分组 删除
 * @param pram
 */
export function groupDeleteApi(params) {
  return request({
    url: `/admin/user/group/delete`,
    method: 'get',
    params
  })
}

/**
 *获取登录页图片
 */
export function getLoginPicApi() {
  return request({
    url: `/admin/getLoginPic`,
    method: 'get'
  })
}

/**
 * @description 验证码
 */
export function captchaApi() {
  return request({
    url: `/admin/validate/code/get`,
    method: 'get'
  })
}

/**
 * @description 修改上级推广人
 */
export function updateSpreadApi(data) {
  return request({
    url: `/admin/user/update/spread`,
    method: 'post',
    data
  })
}

/**
 * 获取邮箱验证码
 * @param {*} data
 * @returns
 */
export function regEmailCode(email) {
  return request({
    baseURL:authURL,
    url: `/authCenter/auth/register/emailCode/` + email,
    method: 'GET',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
  })
}
/**
 * 忘记密码 邮箱验证码
 * @param {*} data
 */
export function regForgotPasswordCode(email) {
  return request({
    baseURL: authURL,
    url: `/authCenter/auth/forgotPassword/code/` + email,
    method: 'GET',
    headers: {'Content-Type': 'application/x-www-form-urlencoded'}
  })
}
  /**
   * 验证邮箱是否存在
   * @param {*} email
   */
export function verifyEmail(email) {
  return request({
    baseURL: "http://localhost:9001",

    url: '/authCenter/auth/verifyEmail/' + email,
    method: 'GET',
    // headers: {'Content-Type': 'application/json'}
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' }

  })
}

