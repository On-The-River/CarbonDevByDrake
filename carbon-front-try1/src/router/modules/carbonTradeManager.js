import Layout from "@/layout";

let carbonNewsManager = {
  path: "/trade/",
  component: Layout,
  name: "tradeManager",
  //   meta: {
  //     title: '碳交易管理',
  //     icon: ''
  //   },
  children: [
    // {
    //     path: '/',
    //     component: () => import('@/views/trade/index'),
    //     name: 'trade',
    //     meta: { title: '碳交易管理', icon: 'clipboard' }
    // },
    // {
    //   path: '/trade/account/exchange/',
    //   component: () => import('@/views/carbonTrade/account/exchangeManager'),
    //   name: 'exchangeManager',
    //   meta: { title: '交易所管理', icon: 'clipboard' }
    // },
    {
        path: '/trade/performance/',
        component: () => import('@/views/carbonTrade/performance/index.vue'),
        name: 'trade',
        meta: { title: '碳履约', icon: 'clipboard' }
    },
    {
      path:'/trade/performance/detail',
      component:()=>import ('@/views/carbonTrade/performance/detail.vue'),
      name: 'performanceDetail',
      meta: { title: '碳履约详情', icon: 'clipboard' },
    },
    // {
    //   path: '/trade/orderResult',
    //   component: () => import('@/views/carbonTrade/quotation/orderResult'),
    //   name: 'trade',
    //   meta: { title: '碳履约', icon: 'clipboard' }
    // },
    // {
    //   path: '/trade/quoteResult',
    //   component: () => import('@/views/carbonTrade/quotation/quoteResult.vue'),
    //   name: 'trade',
    //   meta: { title: '碳履约', icon: 'clipboard' }
    // }
  ]

};

export default carbonNewsManager;
