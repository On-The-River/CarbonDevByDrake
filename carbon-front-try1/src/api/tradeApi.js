import request from '../utils/newRequest'
    /**
     * 交易列表
     * @param {*} data
     * @returns
     */
     export function laodTradeList(data) {
        return request({
          baseURL: "http://localhost:9003",
        url:'/assets/carbonAssetsBusiness/getPageList',
        method: 'post',
        data
        })
    }
    /**
     * 根据ID获取中和资产交易信息
     * @returns
     * @param id
     */
     export function getTradeInfo(id) {
            return request({
              baseURL: "http://localhost:9003",
            url:'/assets/carbonAssetsBusiness/info/' + id,
            method: 'get',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
        })
    }

    /**
     * 获取交易账号列表
     * @param {*} params
     * @returns
     */
    export function getTradeAccountList(params) {
            return request({
              baseURL: "http://localhost:9003",
            url:'/assets/exchangeAccount/getPageList',
            method: 'post',
            data: params,
            headers: { 'Content-Type': 'application/json' }
        })
    }

    /**
     * 获取交易账号信息
     * @param {*} id
     * @returns
     */
    export function getTradeAccountInfo(id) {
        return request({
          baseURL: "http://localhost:9003",
        url:'/assets/exchangeAccount/info/' + id,
        method: 'get',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
      })
   }


   export function searchTradeAccountInfo(data) {
     return request({
       baseURL: "http://localhost:9003",
        url:'/assets/exchangeAccount/getPageList',
        method: 'post',
        data:data,
        headers: { 'Content-Type': 'application/json' }
     })
   }

    /**
     * 绑定交易账号
     * @returns
     * @param data
     */
    export function bindTradeAccountInfo(data) {
        return request({
          baseURL: "http://localhost:9003",
        url:'/assets/exchangeAccount/binding',
        method: 'put',
        data:data,
        headers: { 'Content-Type': 'application/json' }
    })
}
