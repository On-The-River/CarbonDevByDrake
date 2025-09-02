import request from '../utils/request'

export function getHomePanelData(token) {
    return request({
        baseURL: "http://localhost:9002",
        url: '/system/dataPanel/home',
        method: 'get',
        params: [],
        headers: {'token': token}
    })
}
