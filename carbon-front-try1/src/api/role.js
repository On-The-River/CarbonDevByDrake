import request from '../utils/request'

export function addRole(data) {
  return request({
    baseURL: "http://localhost:9002",
    url: '/system/sysRole/add',
    method: 'post',
    data: data
  })
}

/**
 * @param id
 * @returns {Promise}
 */
export function delRole(id) {
  return request({
    baseURL: "http://localhost:9002",
    url: '/system/sysRole/delete/' + id,
    method: 'DELETE',
  })
}

export function getRoleMenu(id) {
  return request({
    baseURL: "http://localhost:9002",
    url: '/system/sysRole/menu/' + id,
    method: 'GET'
  })
}
export function getInfo(pram) {
  const data = {
    ids: pram.id
  }
  return request({
    url: '/admin/system/role/info',
    method: 'get',
    params: data
  })
}

/**
 * @returns
 * @param data
 */
export function getRoleList(data) {
  return request({
    baseURL: "http://localhost:9002",
    url: '/system/sysRole/getPageList',
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    data: data,
  })
}


export function updateRole(pram) {
  let data = {
    id: pram.id,
    roleCode: pram.roleCode,
    roleName: pram.roleName,
    status: pram.status
  }
  return request({
    baseURL: "http://localhost:9002",
    url: '/system/sysRole/update',
    method: 'put',
    // params: {id: pram.id},
    data: data
  })
}
