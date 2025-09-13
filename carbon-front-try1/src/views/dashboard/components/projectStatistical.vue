<template>
  <div class="root">
    <div>
      <a class="nav-title" href="/carbon/projectDo">项目统计</a>
    </div>
    <div class="card-container">
      <!-- 左侧垂直箭头布局容器 -->
      <div class="stat-flow-container">
        <!-- 1. 顶部核心指标：预计减排量 -->
        <div class="stat-card main-card">
          <div class="stat-content">
            <span class="stat-label">预计减排量</span>
            <span class="stat-num">{{ projectStat.reductionTotal }}</span>
            <span class="stat-unit">tCO2e</span>
          </div>
        </div>

        <!-- 向下箭头（连接卡片） -->
        <div class="flow-arrow"></div>

        <!-- 2. 累计审定项目 -->
        <div class="stat-card approved-card">
          <div class="stat-content">
            <span class="stat-label">累计审定项目</span>
            <span class="stat-num">{{ projectStat.approvedCount }}</span>
          </div>
        </div>

        <!-- 向下箭头 -->
        <div class="flow-arrow"></div>

        <!-- 3. 累计备案项目 -->
        <div class="stat-card filing-card">
          <div class="stat-content">
            <span class="stat-label">累计备案项目</span>
            <span class="stat-num">{{ projectStat.filingCount }}</span>
          </div>
        </div>

        <!-- 向下箭头 -->
        <div class="flow-arrow"></div>

        <!-- 4. 累计签发项目 -->
        <div class="stat-card issue-card">
          <div class="stat-content">
            <span class="stat-label">累计签发项目</span>
            <span class="stat-num">{{ projectStat.singCount }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧表格（优化为占满剩余区域） -->
      <div class="table-wrapper">
        <el-table
          :header-cell-style="{ textAlign: 'center' }"
          class="full-width-table"
          :cell-style="{ 'text-align': 'center', border: 'none' }"
          :data="projectList"
          :border="false"
          :show-header="true"
          height="100%"
        >
          <el-table-column prop="projectName" label="项目名称" min-width="220"></el-table-column>
          <el-table-column prop="reduction" label="预计减排量(tCO2e)" min-width="140"></el-table-column>
          <el-table-column prop="carbonValuation" label="碳资产估值(￥)" min-width="140"></el-table-column>
          <el-table-column prop="developmentState" label="项目状态" min-width="120">
            <template #default="scope">
              <el-tag :type="getTagType(scope.row.developmentState)">
                {{ getStateText(scope.row.developmentState) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <!-- 加载状态提示 -->
        <div v-if="isLoading" class="loading-tip">加载中...</div>
      </div>
    </div>
  </div>
</template>

<script>
import { getToken } from "@/utils/auth";

export default {
  name: "projectStatistical",
  data() {
    return {
      projectList: [],
      isLoading: false, // 无限滚动加载状态
      page: 1, // 分页页码
      pageSize: 10, // 每页条数
      // 项目状态编号到文本的映射字典
      stateMap: {
        '0100000001': '待审核',
        '0100000002': '已立项',
        '0100000003': '设计中',
        '0100000004': '审定中',
        '0100000005': '已审定',
        '0100000006': '备案中',
        '0100000007': '已备案',
        '0100000008': '开发中',
        '0100000009': '监测中',
        '0100000010': '核证中',
        '0100000011': '已核证',
        '0100000012': '签发中',
        '0100000013': '已签发',
        '0100000014': '已失效',
        '0100000015':'已撤回',
        '0100000016':'已驳回',
        '0100000017':'已转换',
        '0100000018':'已取消',
        '0100000019':'已开发',

      }
    };
  },
  props: {
    projectStat: {
      type: Object,
      default: () => ({
        reductionTotal: 0,
        approvedCount: 0,
        filingCount: 0,
        singCount: 0,
        projectList: []
      })
    }
  },
  mounted() {
    // 初始化表格数据
    this.projectList = this.projectStat.projectList;
  },
  methods: {
    // 将状态编号转换为显示文本
    getStateText(stateCode) {
      return this.stateMap[stateCode] || '未知状态';
    },
    // 项目状态标签类型映射
    getTagType(stateCode) {
      // 根据状态文本设置标签类型
      const stateText = this.getStateText(stateCode);
      const typeMap = {
        '已审定': 'success',
        '已备案': 'primary',
        '已签发': 'info',
        '已核证': 'success',
        '进行中': 'warning',
        '待审核': 'warning',
        '设计中': 'warning',
        '审定中': 'warning',
        '备案中': 'warning',
        '开发中': 'warning',
        '监测中': 'warning',
        '核证中': 'warning',
        '签发中': 'warning',
        '已立项': 'info',
        '未知状态': 'danger',
        '已失效': 'warning',
        '已撤回': 'warning',
        '已驳回': 'warning',
        '已转换': 'warning',
        '已取消': 'warning',
        '已开发': 'warning',

      };
      return typeMap[stateText] || 'default';
    },
    // 无限滚动加载更多
    loadMore() {
      this.isLoading = true;
      // 模拟接口请求延迟
      setTimeout(() => {
        // 生成包含状态编号的模拟数据
        const stateCodes = Object.keys(this.stateMap);
        const newData = Array.from({ length: this.pageSize }, (_, i) => ({
          projectName: `新项目${this.projectList.length + i + 1}`,
          reduction: (Math.random() * 1000).toFixed(2),
          carbonValuation: (Math.random() * 100000).toFixed(2),
          // 随机选择一个状态编号
          developmentState: stateCodes[Math.floor(Math.random() * stateCodes.length)]
        }));
        this.projectList = [...this.projectList, ...newData];
        this.page++;
        this.isLoading = false;
        // 模拟数据加载完成
        if (this.projectList.length >= 50) {
          this.isLoading = true;
        }
      }, 1000);
    }
  },
  watch: {
    "projectStat.projectList": {
      handler(newList) {
        this.projectList = newList;
      },
      immediate: true
    }
  }
};
</script>

<style lang="scss" scoped>
.root {
  display: flex;
  flex-direction: column;
  margin: 20px 0 20px 20px;

  .card-container {
    margin: 6px 24px 0 0;
    display: flex;
    gap: 20px;
    height: 368px;
    background: #ffffff;
    box-shadow: 0px 2px 8px 0px #eaf0f3;
    border-radius: 8px;
    padding: 16px;
    background-image: url("../../../assets/videos/bei5.png");

    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
    overflow: hidden;
  }
}

.nav-title {
  font-size: 18px;
  font-weight: 500;
  color: #080b0d;
  cursor: pointer;
  text-decoration: none;
  &:hover {
    color: #26b581;
  }
}

.stat-flow-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  width: 180px;
  margin: 10px 0;
  padding: 0 8px;
  box-sizing: border-box;
  flex-shrink: 0;
}

.stat-card {
  width: 100%;
  border-radius: 8px;
  padding: 10px 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  cursor: default;
  box-sizing: border-box;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.09);
  }

  .stat-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .stat-label {
    font-size: 12px;
    font-weight: 400;
    margin-bottom: 4px;
    color: #fff;
    opacity: 0.9;
  }

  .stat-num {
    font-size: 22px;
    font-weight: 700;
    color: #fff;
    line-height: 1.1;
  }

  .stat-unit {
    font-size: 12px;
    color: #fff;
    opacity: 0.9;
    margin-left: 2px;
  }
}

.main-card {
  background: linear-gradient(135deg, #26b581 0%, #1a9c6e 100%);
  border-top: 2px solid #74d88c;
}

.approved-card {
  background: linear-gradient(135deg, #74d88c 0%, #5fc678 100%);
}

.filing-card {
  background: linear-gradient(135deg, #40bfff 0%, #00a5ff 100%);
}

.issue-card {
  background: linear-gradient(135deg, #00a5ff 0%, #0065ff 100%);
}

.flow-arrow {
  width: 2px;
  height: 10px;
  background: linear-gradient(180deg, #cbd5e1 0%, #94a3b8 100%);
  position: relative;

  &::after {
    content: "";
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 0;
    height: 0;
    border-left: 3px solid transparent;
    border-right: 3px solid transparent;
    border-top: 4px solid #94a3b8;
  }
}

/* 表格容器深色半透明（保持原有样式不变） */
.table-wrapper {
  flex: 1;
  height: calc(100% - 20px);
  display: flex;
  flex-direction: column;
  background-color: rgba(0, 0, 0, 0.4) !important;
  border-radius: 8px;
  overflow: hidden;
  backdrop-filter: blur(4px);
}

.full-width-table {
  width: 100%;
  height: 100%;
  min-width: 600px;
  background-color: transparent !important;



/* 表格基础样式（保持原有样式不变） */
::v-deep .el-table {
  border: none !important;
  background-color: transparent !important;
}

/* 关键修改：表头容器改为深色透明（比表格容器深10%透明度，增强层级） */
::v-deep .el-table__header {
  border-bottom: 1px solid rgba(255,255,255,0.1) !important; /* 保留原有边框，区分表头与表体 */
  background-color: rgba(0, 0, 0, 0.5) !important; /* 表头容器深色透明（0.5透明度，比表格容器0.4深） */
}

/* 关键修改：表头单元格改为深色透明（与表头容器一致） */
::v-deep .el-table__header th {
  font-weight: 500 !important;
  color: #fff !important; /* 保留原有白色文字，确保对比清晰 */
  padding: 12px 0 !important;
  border-right: none !important;
  border-bottom: none !important;
  background-color: rgba(0, 0, 0, 0.5) !important; /* 表头单元格与容器背景一致，避免断层 */
  text-shadow: 0 0 2px rgba(0,0,0,0.8); /* 保留文字阴影，增强可读性 */
}

/* 表体样式（保持原有样式不变） */
::v-deep .el-table__body {
  border-top: none !important;
  background-color: transparent !important;
}

::v-deep .el-table__body td {
  border-right: none !important;
  border-bottom: 1px solid rgba(0,0,0,0.6) !important;
  background-color: transparent !important;
  color: #f0f0f0 !important;
  text-shadow: 0 0 1px rgba(0,0,0,0.6);
}

::v-deep .el-table__body tr {
  height: 50px !important;
  border-bottom: none !important;
  background-color: transparent !important;
}

/* 行hover效果（保持原有样式不变） */
::v-deep .el-table__row {
  &:hover {
    background-color: rgba(255,255,255,0.15) !important;
  }
}

/* 滚动容器样式（保持原有样式不变） */
::v-deep .el-table__body-wrapper {
  border: none !important;
  background-color: transparent !important;
  &::-webkit-scrollbar {
    width: 6px;
    height: 6px;
  }

  &::-webkit-scrollbar-track {
    background: rgba(255,255,255,0.1) !important;
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(255,255,255,0.3) !important;
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb:hover {
    background: rgba(255,255,255,0.5) !important;
  }
}
}
/* 加载提示样式（保持原有样式不变） */
.loading-tip {
  text-align: center;
  padding: 10px;
  color: #fff !important;
  font-size: 12px;
  background-color: rgba(0,0,0,0.5) !important;
  text-shadow: 0 0 2px rgba(0,0,0,0.8);
}

/* 状态标签样式（保持原有样式不变） */
::v-deep .el-tag {
  padding: 3px 6px;
  border-radius: 3px;
  font-size: 12px;
  opacity: 1 !important;

  &.el-tag--success {
    background-color: #28E891 !important;
    color: #083321 !important; /* 成功标签文字深色，增强对比 */
  }

  &.el-tag--primary {
    background-color: #009EFF !important;
    color: #052e47 !important; /* 主要标签文字深色 */
  }

  &.el-tag--info {
    background-color: #40BFFF !important;
    color: #053857 !important; /* 信息标签文字深色 */
  }

  &.el-tag--warning {
    background: linear-gradient(135deg, #26b581 0%, #1a9c6e 100%);
    color: #5a4b00 !important; /* 警告标签文字深色 */
  }

  &.el-tag--danger {
    background-color: #fb0d0d !important;
    color: #fff !important; /* 危险标签文字白色 */
  }

  &.el-tag--default {
    background-color: #e5e7eb !important;
    color: #1f2937 !important; /* 默认标签文字深色，适配浅色背景 */
  }
}
</style>
