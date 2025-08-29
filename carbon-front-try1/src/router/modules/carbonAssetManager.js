import Layout from "@/layout";
import { AppMain } from "@/layout/components";
let carbonAssetManager = {
  path: "/assets",
  component: Layout,
  // redirect: '/assets/carbonQuota',
  // name: 'assets',
  // meta: {
  //   title: '碳资产管理',
  //   icon: ''
  // },
  redirect: "noRedirect",
  children: [
    {
      path: "/dataRecord/add",
      component: () => import("@/views/carbonAssets/dataSource/addSource"),
      name: "addSourceAsset",
      meta: { title: "添加碳数据", icon: "" }
    },
    {
      path: "/dataRecord/read",
      component: () => import("@/views/carbonAssets/dataSource/readSource"),
      name: "readSourceAsset",
      meta: { title: "查看碳数据", icon: "" }
    },
    // {
    //   path: '/dataRecord/edit',
    //   component: () => import('@/views/carbonAssets/dataSource/editSource'),
    //   name: 'editSource',
    //   meta: { title: '编辑碳数据', icon: '' }
    // },

    {
      path: "/carbonAccounting",
      component: () => import("@/views/carbonAssets/apply/projectListView"),
      name: "carbonAccountingAsset",
      meta: { title: "碳减排核", icon: "" }
    },

    /***CCER项目*/
    {
      path: "/applyProject/",
      component: () => import("@/views/carbonAssets/apply/projectListView"),
      name: "projectListAsset",
      meta: { title: "申请项目主页", icon: "" }
    },
    {
      path: "/applyProject/add",
      component: () => import("@/views/carbonAssets/apply/addNewProject"),
      name: "addNewProjectAsset",
      meta: { title: "申请项目", icon: "" }
    },
    {
      path: "/applyProject/read",
      component: () => import("@/views/carbonAssets/apply/readProject"),
      name: "readProjectAsset",
      meta: { title: "查看项目", icon: "" }
    },
    {
      path: "/applyProject/edit",
      component: () => import("@/views/carbonAssets/apply/editProject"),
      name: "editProjectAsset",
      meta: { title: "编辑项目", icon: "" }
    },
    {
      path: "quota",
      component: () => import("@/views/carbonAssets/quota"),
      name: "carbonQuota",
      meta: { title: "碳配额", icon: "" }
    },
    // 已移除重复 quotaDetail name
    // 已移除重复 quotaDetail name
    {
      path: "credit",
      component: () => import("@/views/carbonAssets/credit"),
      name: "carbonCredit",
      meta: { title: "碳信用", icon: "" }
    },
    // 已移除重复 assetDetail name
    // 已移除重复 assetDetail name
    {
      path: "credit/CarbonAssessment",
      component: () => import("@/views/carbonAssets/CarbonAssessment"),
      name: "CarbonAssessment",
      meta: { title: "碳评估", icon: "" }
    },
    {
      path: "/carbonMethodology/add/",
      component: () => import("@/views/carbonAssets/method/methodDetail"),
      name: "methodDetailAsset",
      meta: { title: "添加方法学详情页", icon: "" }
    },
    // {
    //   path: '/carbonMethodology/add',
    //   component: () => import('@/views/carbonAssets/method/addMethod'),
    //   name: 'addNewProject',
    //   meta: { title: '申请方法学', icon: '' }
    // },
    {
      path: "/carbonMethodology/readMethod",
      component: () => import("@/views/carbonAssets/method/readMethod"),
      name: "readMethod",
      meta: { title: "查看方法学", icon: "" }
    },
    {
      path: "/carbonMethodology/editMethod",
      name: "editSourceAsset",
      name: "editMethod",
      meta: { title: "编辑方法学", icon: "" }
    }
  ]
};

export default carbonAssetManager;
