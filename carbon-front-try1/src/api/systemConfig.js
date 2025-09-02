import request from '../utils/request'

export function configCheckUnique(pram) {
  const data = {
    name: pram.name
  }
  return request({
    url: '/admin/system/config/check',
    method: 'GET',
    params: data
  })
}

export function configDelete(pram) {
  const data = {
    id: pram.id
  }
  return request({
    url: '/admin/system/config/delete',
    method: 'GET',
    params: data
  })
}

export function configInfo(pram) {
  const data = {
    formId: pram.id
  }
  return request({
    url: '/admin/system/config/info',
    method: 'GET',
    params: data
  })
}

export function configList(pram) {
  const data = {
    page: pram.page,
    limit: pram.limit
  }
  return request({
    url: '/admin/system/config/list',
    method: 'GET',
    params: data
  })
}

export function configSave(pram) {
  const data = {
    systemConfigRequest: {
      desc: pram.desc,
      groupId: pram.groupId,
      info: pram.info,
      name: pram.name,
      pid: pram.pid,
      status: pram.status,
      type: pram.type,
      value: pram.value
    }
  }
  return request({
    url: '/admin/system/config/save',
    method: 'POST',
    params: data
  })
}

/**
 * @returns {Promise}
 */
export function configSaveForm(pram) {
  return request({
    url: '/admin/system/config/save/form',
    method: 'POST',
    data: pram
  })
}

export function configUpdate(pram) {
  const data = {
    id: pram.id,
    systemConfigRequest: pram.systemConfigRequest
  }
  return request({
    url: '/admin/system/config/update',
    method: 'POST',
    params: data
  })
}

export function configSaveUniq(pram) {
  const data = {
    key: pram.key,
    value: pram.value
  }
  return request({
    url: '/admin/system/config/saveuniq',
    method: 'POST',
    params: data
  })
}

/**
 * @returns {Promise}
 */
export function configGetUniq(pram) {
  const data = {
    key: pram.key
  }
  return request({
    url: '/admin/system/config/getuniq',
    method: 'GET',
    params: data
  })
}

/**
 * 获取全部字典
 * @returns {Promise}
 */
export function getAllDict() {
  return request({
    baseURL: "http://localhost:9002",
    url: '/system/sysDict/getAllDict',
    method: 'GET',
  })
}
