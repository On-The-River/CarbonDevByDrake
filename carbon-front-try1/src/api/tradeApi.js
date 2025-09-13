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

// 获取大宗交易数据
export function getBlockTradingData() {
  return request({
    baseURL: "http://localhost:9002",
    url: '/system/python/blockTrading',
    method: 'get'
  })
}

// 获取市场行情数据
export function getMarketConditionsData() {
  return request({
    baseURL: "http://localhost:9002",
    url: 'system/python/marketConditions',
    method: 'get'
  })
}
//价格区间预测
export function predictTree() {
  return request({
    baseURL: "http://localhost:9002",
    url: 'system/python/predictPrice',
    method: 'post'
  })
}
//树类型
export function getBarTreeWithType() {
  return request({
    baseURL: "http://localhost:9002",
    url: 'system/python/barTreeWithType',
    method: 'get'
  })
}
//土壤质量
export function getBarTreeWithSoil() {
  return request({
    baseURL: "http://localhost:9002",
    url: 'system/python/barTreeWithSoil',
    method: 'get'
  })
}
//气候类型
export function getBarTreeWithClimate() {
  return request({
    baseURL: "http://localhost:9002",
    url: 'system/python/barTreeWithClimate',
    method: 'get'
  })
}
