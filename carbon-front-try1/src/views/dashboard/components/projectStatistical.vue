<template>
  <div class="root">
    <div>
      <a class="nav-title" href="/carbon/projectDo">项目统计</a>
    </div>
    <div class="card-container">
      <!-- 树形布局容器 -->
      <div class="tree-container">
        <!-- 树干 -->
        <div class="tree-trunk"></div>

        <!-- 树冠及数据项 -->
        <div class="tree-canopy">
          <!-- 顶部主数据项 - 预计减排量 -->
          <div class="tree-node main-node">
            <div class="reduction-item">
              <span class="reduction-num">{{ projectStat.reductionTotal }}</span>
              <span class="unit">tCO2e</span>
              <span class="reduction-label">预计减排量</span>
            </div>
          </div>

          <!-- 左侧树枝及节点 -->
          <div class="tree-branch left-branch"></div>
          <div class="tree-node left-node">
            <div class="stat-item approved">
              <span class="stat-num">{{ projectStat.approvedCount }}</span>
              <span class="stat-label">累计审定项目</span>
            </div>
          </div>

          <!-- 右侧上树枝及节点 -->
          <div class="tree-branch right-top-branch"></div>
          <div class="tree-node right-top-node">
            <div class="stat-item filing">
              <span class="stat-num">{{ projectStat.filingCount }}</span>
              <span class="stat-label">累计备案项目</span>
            </div>
          </div>

          <!-- 右侧下树枝及节点 -->
          <div class="tree-branch right-bottom-branch"></div>
          <div class="tree-node right-bottom-node">
            <div class="stat-item sing">
              <span class="stat-num">{{ projectStat.singCount }}</span>
              <span class="stat-label">累计签发项目</span>
            </div>
          </div>
        </div>
      </div>

      <el-table
        :header-cell-style="{ textAlign: 'center' }"
        class="table-div"
        :cell-style="{ 'text-align': 'center' }"
        :data="projectList"
        stripe style="width: 100%"
        max-height="368"
      >
        <el-table-column prop="projectName" label="项目名称" width="280"></el-table-column>
        <el-table-column prop="reduction" label="预计减排量(tCO2e)" width="160"></el-table-column>
        <el-table-column prop="carbonValuation" label="碳资产估值(￥)" width="160"></el-table-column>
        <el-table-column prop="developmentState" label="项目状态" width="140"></el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import * as echarts from "echarts";
import { getHomePanelData } from "@/api/homeApi.js";
import { getToken } from "@/utils/auth";

export default {
  name: "projectStatistical",
  data() {
    return {
      projectList : this.projectStat.projectList
    };
  },
  props: {
    projectStat: {}
  },
  mounted() {
    this.projectList = this.projectStat.projectList
  }
};
</script>

<style lang="scss" scoped>
.root {
  display: flex;
  flex-direction: column;
  margin-top: 20px;
  margin-left: 20px;

  .card-container {
    margin-top: 6px;
    margin-right: 24px;
    display: flex;
    flex-direction: row;
    height: 368px;
    background: #ffffff;
    box-shadow: 0px 2px 8px 0px #eaf0f3;
    border-radius: 8px;
  }
}

.nav-title {
  margin-top: 20px;
  font-weight: 500;
  color: #080b0d;
  cursor: default;
}

/* 树形布局样式 */
.tree-container {
  position: relative;
  width: 300px;
  height: 300px;
  margin: 30px 20px;
}

/* 树干样式 */
.tree-trunk {
  position: absolute;
  width: 8px;
  height: 220px;
  background: linear-gradient(0deg, #8B4513 0%, #A0522D 100%);
  left: 120px; /* 调整树干位置更靠左 */
  bottom: 0;
  border-radius: 4px 4px 0 0;
}

/* 树枝样式 */
.tree-branch {
  position: absolute;
  background: linear-gradient(0deg, #8B4513 0%, #A0522D 100%);
  height: 6px;
  border-radius: 3px;
  z-index: 1;
}

/* 左侧树枝 - 向左延伸更长 */
.left-branch {
  width: 100px;
  top: 160px;
  left: 120px;
  transform-origin: left center;
  transform: rotate(-40deg);
}

/* 右侧上树枝 */
.right-top-branch {
  width: 80px;
  top: 100px;
  left: 120px;
  transform-origin: left center;
  transform: rotate(30deg);
}

/* 右侧下树枝 */
.right-bottom-branch {
  width: 80px;
  top: 200px;
  left: 120px;
  transform-origin: left center;
  transform: rotate(20deg);
}

/* 节点位置调整 */
.tree-node {
  position: absolute;
  z-index: 2; /* 确保节点在树枝上方 */
}

/* 顶部节点 */
.main-node {
  top: 0;
  left: 50px; /* 左移使顶部节点居中 */
}

/* 左侧节点 - 左移并调整垂直位置 */
.left-node {
  top: 130px;
  left: 10px;
}

/* 右侧上节点 */
.right-top-node {
  top: 80px;
  left: 160px;
}

/* 右侧下节点 */
.right-bottom-node {
  top: 180px;
  left: 160px;
}

/* 左侧项目文字右对齐 */
.left-node .stat-label {
  text-align: right;
  margin-left: 5px;
}

/* 右侧项目文字左对齐 */
.right-top-node .stat-label,
.right-bottom-node .stat-label {
  text-align: left;
  margin-left: 10px;
}

/* 数据项样式优化 */
.reduction-item {
  background: linear-gradient(135deg, #74d88c 0%, #26b581 100%);
  border-radius: 8px;
  padding: 10px;
  color: #fff;
  text-align: center;
  width: 140px;
  box-shadow: 0 3px 8px rgba(0,0,0,0.15);
  transition: transform 0.2s;
}

.reduction-item:hover {
  transform: translateY(-2px);
}

.reduction-num {
  font-size: 24px;
  font-weight: bold;
  display: block;
}

.unit {
  font-size: 14px;
}

.reduction-label {
  font-size: 14px;
  margin-top: 5px;
  display: block;
}

.stat-item {
  display: flex;
  align-items: center;
}

.stat-num {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #fff;
  box-shadow: 0 3px 8px rgba(0,0,0,0.15);
  transition: transform 0.2s;
}

.stat-num:hover {
  transform: scale(1.05);
}

/* 左侧节点数字在右，文字在左 */
.left-node .stat-item {
  flex-direction: row-reverse;
}

.left-node .stat-num {
  margin-right: 0;
  margin-left: 10px;
}

.approved .stat-num {
  background: linear-gradient(135deg, #74d88c 0%, #26b581 100%);
}

.filing .stat-num,
.sing .stat-num {
  background: linear-gradient(135deg, #00a5ff 0%, #0065ff 100%);
}

.stat-label {
  font-size: 14px;
  color: #424c5c;
  white-space: nowrap;
}

.table-div {
  margin-left: 20px;
  margin-right: 24px;
  margin-top: 30px;
  margin-bottom: 30px;
  flex: 1;
}
</style>
