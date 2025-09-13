import request from '../utils/request'

/**
 * 绑定产品
 * @param pram
 */
export function bindProduct(pram) {
  const data = {
    id: pram.id,
    productId: pram.productId
  }
  return request({
    baseURL: "http://localhost:9002",
    url: '/admin/article/bind/product',
    method: 'POST',
    params: data
  })
}
/**
 * 删除文章
 * @param id
 * @constructor
 */
export function DelArticle(id) {
  return request({
    baseURL: "http://localhost:9002",
    url: '/system/carbonArticle/delete/' + id,
    method: 'DELETE',
  })
}

/**
 * 文章详情
 * @param id
 * @constructor
 */
export function InfoArticle(id) {
  const data = {
    id: id
  }
  return request({
    baseURL: "http://localhost:9002",
    url: '/admin/article/info',
    method: 'get',
    params: data
  })
}

/**
 * 文章列表
 * @param pram
 * @constructor
 */
export function ListArticle(pram) {
  const data = {
    keywords: pram.keywords,
    cid: pram.cid,
    page: pram.page,
    limit: pram.limit
  }
  return request({
    baseURL: "http://localhost:9002",
    url: '/admin/article/list',
    method: 'GET',
    params: data
  })
}

/**
 * 新增文章
 * @param data
 */
export function AddArticle(data) {
  return request({
    baseURL: "http://localhost:9002",
    url: "/system/carbonArticle/push",
    method: 'post',
    timeout: 5000,
    params: { title: data }
    // headers: { // 设置请求头
    //   token: token
    // }
  })
}

/**
 * 更新文章
 * @param pram
 * @constructor
 */
export function UpdateArticle(pram) {
  const data = {
    author: pram.author,
    cid: pram.cid,
    content: pram.content,
    imageInput: pram.imageInput,
    isBanner: pram.isBanner,
    isHot: pram.isHot,
    shareSynopsis: pram.shareSynopsis,
    shareTitle: pram.shareTitle,
    sort: pram.sort,
    synopsis: pram.synopsis,
    title: pram.title,
    url: pram.url
  }
  return request({
    url: '/admin/article/update',
    method: 'post',
    params: { id: pram.id },
    data: data
  })
}

/**
 *
 * @returns 获取碳资讯列表
 */
export function getCarbonArticles(param) {
  return request({
    baseURL: "http://localhost:9002",
    url: '/system/carbonArticle/getPageList',
    method: 'post',
    data: param
  })
}

export function getCarbonArticlesAsUser(param){
  return request({
    baseURL: "http://localhost:9002",
    url: '/system/carbonArticle/getPageListAsUser',
    method: 'post',
    data: param
  })
}

//根据id修改碳文章状态
export function changeCarbonStatusById(param) {
  return request({
    baseURL: "http://localhost:9002",
    url: '/system/carbonArticle/updateStatu/',
    method: 'PUT',
    data: param
  })
}
//同步文章
export function syncArticle(datas) {
  return request({
    baseURL: "http://localhost:9002",
    url: '/system/carbonArticle/getRefreshList',
    method: 'POST',
    data: datas,
    timeout: 60000
  })
}
//添加碳文章到数据库
export function addArticleToSql(datas) {
  return request({
    baseURL: "http://localhost:9002",
    url: '/system/carbonArticle/addmysql',
    method: 'POST',
    data: datas,
    timeout: 60000
  })
}
//添加碳文章到数据库
export function updateArticle(datas) {
  return request({
    baseURL: "http://localhost:9002",
    url: '/system/carbonArticle/update',
    method: 'PUT',
    data: datas,
    timeout: 60000
  })
}
