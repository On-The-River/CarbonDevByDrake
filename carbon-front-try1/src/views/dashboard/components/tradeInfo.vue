<template>
  <div class="root">
    <!-- 顶部标题栏：左半部分文字需变白 -->
    <div class="fl">
      <div>
        <a class="nav-title">碳交易行情</a>
      </div>
      <div style="margin-left: 22px;">
        <!-- 标签页已注释，保留样式类备用 -->
      </div>
      <div>
        <a class="more-text white-text" href="/trade/quotation" style="margin-right:22px;">查看更多 ></a>
      </div>
    </div>

    <div class="root-tab">
      <!-- 核心容器：添加背景图 -->
      <div class="card-container bg-with-img">
        <!-- 左侧第一个环形图（签发量）：色块与扇区颜色对应 -->
        <div class="chart-container">
          <div class="chart-top-text white-text"><span>{{ activeName }}</span> 签发量：{{ quotation.ccerCount }}</div>
          <div id="xunjian_echart" style="margin-left:10px;width:168.18px;height:168.18px" />
          <div class="chart-unit white-text">单位：tCO2e</div>
          <div class="chart-info-bg green-bg">
            <div class="wrap-div">
              <!-- 已核销：对应环形图“已核销”扇区颜色 #009EFF -->
              <div class="info-line">
                <div class="written-off-point" />
                <i class="hint-text white-text">已核销，{{ quotation.writtenOffCount }}，{{ percentWrittenOffCount }}%</i>
              </div>
              <!-- 存量：对应环形图“存量”扇区颜色 #28E891 -->
              <div class="info-line">
                <div class="stock-point" />
                <i class="hint-text white-text">存量，{{ quotation.stockCount }}，{{ percentStockCount }}%</i>
              </div>
            </div>
          </div>
        </div>

        <!-- 左侧第二个环形图（项目量）：色块与扇区颜色对应 -->
        <div class="chart-container">
          <div class="chart-top-text white-text"> {{ activeName }} 项目量：{{ quotation.ccerProjectCount }}</div>
          <div id="lvxin_echart" style="margin-left:10px;width:168.18px;height:168.18px" />
          <div class="chart-unit white-text">单位：个</div>
          <div class="chart-info-bg green-bg">
            <div class="wrap-div">
              <!-- 已审定：对应环形图“已审定”扇区颜色 #fb0d0d -->
              <div class="info-line">
                <div class="approved-point" />
                <i class="hint-text white-text">已审定，{{ quotation.approvedCount }} 个，{{ percentApprovedCount }}%</i>
              </div>
              <!-- 已备案：对应环形图“已备案”扇区颜色 #FFD93D -->
              <div class="info-line">
                <div class="filing-point" />
                <i class="hint-text white-text">已备案，{{ quotation.filingCount }} 个，{{ percentFilingCount }}%</i>
              </div>
              <!-- 已签发：对应环形图“已签发”扇区颜色 #28E891 -->
              <div class="info-line">
                <div class="signed-point" />
                <i class="hint-text white-text">已签发，{{ quotation.singCount }} 个，{{ percentSingCount }}%</i>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧表格：保持原有样式不变 -->
        <el-table
          :header-cell-style="{ textAlign: 'center' }"
          class="table-div"
          :cell-style="{ 'text-align': 'center', border: 'none' }"
          :data="quotation.projects || []"
          :border="false"
          :show-header="true"    style="width: 380px; margin-left: 20px;"
          max-height="330"
        >
          <el-table-column prop="type" label="项目类型" width="180"></el-table-column>
          <el-table-column prop="singCount" label="签发量(tCO2e)" width="180"></el-table-column>
          <el-table-column prop="stockCount" label="存量(tCO2e)"></el-table-column>
          <el-table-column prop="count" label="项目数量(个)"></el-table-column>
        </el-table>

      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import { getHomePanelData } from '@/api/homeApi.js'
import { getToken } from '@/utils/auth'
export default {
  name: 'tradeInfo',
  data() {
    return {
      ccerCount: 0,
      stockCount: 0,
      writtenOffCount: 0,
      percentStockCount: 0,
      percentWrittenOffCount: 0,
      ccerProjectCount: 0,
      singCount: 0,
      filingCount: 0,
      approvedCount: 0,
      percentSingCount: 0,
      percentFilingCount: 0,
      percentApprovedCount: 0,
      isTabShow: 0,
      quotation: {},
      activeName: "CCER"
    }
  },
  mounted() {
    this.caculatePercent("CCER")
  },
  methods: {
    handleClick(tab, event) {
      this.caculatePercent(this.activeName)
    },
    caculatePercent(str) {
      let token = getToken()
      getHomePanelData(token).then(res => {
        let index = 0;
        for (let i = 0; i < res.quotation.length; i++) {
          if (res.quotation[i].type == str) {
            index = i;
            break;
          }
        }
        let quotation = res.quotation[index] || {}
        this.quotation = quotation

        // 处理空数据避免报错
        this.ccerCount = quotation.ccerCount || 0
        this.stockCount = quotation.stockCount || 0
        this.writtenOffCount = quotation.writtenOffCount || 0
        this.ccerProjectCount = quotation.ccerProjectCount || 0
        this.singCount = quotation.singCount || 0
        this.filingCount = quotation.filingCount || 0
        this.approvedCount = quotation.approvedCount || 0

        // 计算占比（避免分母为0）
        this.percentStockCount = this.ccerCount ? parseFloat(this.stockCount / this.ccerCount * 100).toFixed(2) : 0
        this.percentWrittenOffCount = this.ccerCount ? parseFloat(this.writtenOffCount / this.ccerCount * 100).toFixed(2) : 0
        this.percentSingCount = this.ccerProjectCount ? parseFloat(this.singCount / this.ccerProjectCount * 100).toFixed(2) : 0
        this.percentFilingCount = this.ccerProjectCount ? parseFloat(this.filingCount / this.ccerProjectCount * 100).toFixed(2) : 0
        this.percentApprovedCount = this.ccerProjectCount ? parseFloat(this.approvedCount / this.ccerProjectCount * 100).toFixed(2) : 0

        this.drawRing()
      }).catch(() => {})
    },
    drawRing() {
      // 第一个环形图（签发量）：修复中心文字居中问题
      const xj = document.getElementById('xunjian_echart');
      const xunjian = echarts.init(xj);
      const optionXunjian = {
        color: [
          "#28E891", // 存量扇区颜色
          "#009EFF"  // 已核销扇区颜色
        ],
        tooltip: { trigger: 'item' },
        title: {
          show: true,
          text: this.ccerCount, // 中心显示的数值（如256）
          left: '50%', // 水平居中（相对于图表容器）
          top: '50%', // 垂直居中（相对于图表容器）
          textAlign: 'center',
          textStyle: { color: '#fff', fontSize: 14 },
          offset: [0, 0] // 消除默认偏移，确保完全居中
        },
        series: [{
          type: 'pie',
          radius: ['30%', '80%'],
          avoidLabelOverlap: false,
          label: { show: false, position: 'center' },
          emphasis: {
            label: { show: true, fontSize: '12', color: '#fff' }
          },
          labelLine: { show: true },
          data: [
            { value: this.stockCount, name: '存量' },
            { value: this.writtenOffCount, name: '已核销' }
          ]
        }]
      };
      xunjian.setOption(optionXunjian);

      // 第二个环形图（项目量）：修复中心文字居中问题
      const lv = document.getElementById('lvxin_echart');
      const lvxin = echarts.init(lv);
      const optionLvxin = {
        color: [
          "#fb0d0d",   // 已审定扇区颜色
          "#FFD93D",   // 已备案扇区颜色
          "#28E891"    // 已签发扇区颜色
        ],
        tooltip: { trigger: 'item' },
        title: {
          show: true,
          text: this.ccerProjectCount, // 中心显示的数值（如862）
          left: '50%', // 水平居中（相对于图表容器）
          top: '50%', // 垂直居中（相对于图表容器）
          textAlign: 'center',
          textStyle: { color: '#fff', fontSize: 14 },
          offset: [0, 0] // 消除默认偏移，确保完全居中
        },
        series: [{
          type: 'pie',
          radius: ['30%', '80%'],
          avoidLabelOverlap: false,
          label: { show: false, position: 'center' },
          emphasis: {
            label: { show: true, fontSize: '12', color: '#fff' }
          },
          labelLine: { show: true },
          data: [
            { value: this.approvedCount, name: '已审定' },
            { value: this.filingCount, name: '已备案' },
            { value: this.singCount, name: '已签发' }
          ]
        }]
      };
      lvxin.setOption(optionLvxin);

      // 窗口 resize 时重绘图表
      window.addEventListener('resize', () => {
        xunjian.resize();
        lvxin.resize();
      });
    }
  }
}
</script>

<style lang="scss" scoped>
.root {
  display: flex;
  flex-direction: column;
  margin-top: 20px;
  margin-left: 20px;

  // 1. 核心容器背景图
  .bg-with-img {
    background-image: url('../../../assets/videos/bei5.png'); // 替换实际图片路径
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
  }

  // 2. 白色文字通用类
  .white-text {
    color: #fff !important;
    text-shadow: 0 0 2px rgba(0, 0, 0, 0.5);
  }

  // 3. 环形图下方绿色背景
  .green-bg {
    background-image: none !important;
    background: linear-gradient(180deg, #4EDEB7 0%, #26B581 100%) !important;
    border-radius: 8px;
  }

  .card-container {
    margin-top: 0;
    margin-right: 24px;
    display: flex;
    flex-direction: row;
    height: 368px;
    width: 100%;
    box-shadow: 0px 2px 8px 0 #EAF0F3;
    border-radius: 8px;
    padding: 0 10px;
  }
}

// 环形图容器样式
.chart-container {
  display: flex;
  flex-direction: column;
  margin-left: 60px;
  margin-top: 37px;
  margin-bottom: 30px;
  width: 201px;
}

.chart-top-text {
  text-align: center;
  font-weight: 500;
  color: #424C5C;
  margin-bottom: 8px;
}

.chart-unit {
  text-align: center;
  font-weight: 400;
  color: #EB6C84;
  margin: 8px 0;
}

// 环形图下方信息容器
.chart-info-bg {
  display: flex;
  flex-direction: column;
  margin-top: 20px;
  width: 201px;
  height: 111px;
  background-image: url('../../../assets/videos/bei5.png');
  background-repeat: no-repeat;
  align-items: center; // 让文字容器居中
  justify-content: center; // 垂直居中
}

// 信息行样式：确保色块与文字垂直对齐
.info-line {
  display: flex;
  flex-direction: row;
  align-items: center; // 色块与文字垂直居中
  margin: 8px 0;
  width: 800px;
}

// 关键：文字左侧色块，与环形图扇区颜色一一对应
// 1. 签发量模块 - 已核销（对应环形图 #009EFF）
.written-off-point {
  border-radius: 50%; // 圆形色块，与图表视觉统一
  width: 10px;
  height: 10px;
  margin-right: 10px;
  background: #009EFF; // 与“已核销”扇区颜色完全一致
}

// 2. 签发量模块 - 存量（对应环形图 #28E891）
.stock-point {
  border-radius: 50%;
  width: 10px;
  height: 10px;
  margin-right: 10px;
  background: #28E891; // 与“存量”扇区颜色完全一致
}

// 3. 项目量模块 - 已审定（对应环形图 #fb0d0d）
.approved-point {
  border-radius: 50%;
  width: 10px;
  height: 10px;
  margin-right: 10px;
  background: #fb0d0d; // 与“已审定”扇区颜色完全一致
}

// 4. 项目量模块 - 已备案（对应环形图 #FFD93D）
.filing-point {
  border-radius: 50%;
  width: 10px;
  height: 10px;
  margin-right: 10px;
  background: #FFD93D; // 与“已备案”扇区颜色完全一致
}

// 5. 项目量模块 - 已签发（对应环形图 #28E891）
.signed-point {
  border-radius: 50%;
  width: 10px;
  height: 10px;
  margin-right: 10px;
  background: #28E891; // 与“已签发”扇区颜色完全一致
}

// 提示文字样式
.hint-text {
  font-weight: 400;
  color: #424C5C;
  font-size: 13px; // 优化字体大小，避免换行
}

// 右侧表格样式
.table-div {
  margin-left: 20px;
  margin-right: 24px;
  margin-top: 30px;
  margin-bottom: 30px;
  width: 380px;
  height: 370px;
  display: flex;
  flex-direction: column;
  background-color: rgba(0, 0, 0, 0.4) !important;
  border-radius: 8px;
  overflow: hidden;
  backdrop-filter: blur(4px);

  ::v-deep .el-table {
    width: 380px;
    height: 370px;
    min-width: 380px;
    background-color: transparent !important;
    border: none !important;
  }

  /* 表格基础样式 */
  ::v-deep .el-table {
    border: none !important;
    background-color: transparent !important;
  }

  /* 表头容器改为深色透明 */
  ::v-deep .el-table__header {
    border-bottom: 1px solid rgba(255,255,255,0.1) !important;
    background-color: rgba(0, 0, 0, 0.5) !important;
  }

  /* 表头单元格改为深色透明 */
  ::v-deep .el-table__header th {
    font-weight: 500 !important;
    color: #fff !important;
    padding: 12px 0 !important;
    border-right: none !important;
    border-bottom: none !important;
    background-color: rgba(0, 0, 0, 0.5) !important;
    text-shadow: 0 0 2px rgba(0,0,0,0.8);
  }

  /* 表体样式 */
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

  /* 行hover效果 */
  ::v-deep .el-table__row {
    &:hover {
      background-color: rgba(255,255,255,0.15) !important;
    }
  }

  /* 滚动容器样式 */
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


// 顶部标题栏样式
.fl {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-right: 24px;
  margin-bottom: 10px;
}

// 文字容器：确保内部文字居中
.wrap-div {
  width: 100%;
  padding: 0 20px; // 左右内边距，避免文字贴边
}

// 标签页样式（已注释，保留备用）
.white-tab {
  ::v-deep .el-tabs__header {
    margin: 0; // 清除默认头部间距
  }
  ::v-deep .el-tabs__nav {
    .el-tabs__item {
      color: #fff !important; // 标签文字变白（适配背景图）
      font-size: 14px;
      padding: 0 16px; // 调整标签内边距
      &:hover {
        color: #28E891 !important; //  hover 时文字变绿色
      }
      &.is-active {
        color: #28E891 !important; // 激活态标签文字绿色
        font-weight: 500; // 激活态文字加粗
      }
    }
  }
  ::v-deep .el-tabs__indicator {
    background-color: #28E891 !important; // 激活态下划线绿色（与文字颜色呼应）
    height: 2px; // 调整下划线高度
    width: 30% !important; // 下划线宽度适配标签文字
    left: 35% !important; // 下划线居中（配合宽度计算）
  }
}

// 其他基础样式补充
.nav-title {
  margin-top: 20px;
  font-weight: 500;
  color: #080E0D;
  cursor: default;
  font-size: 16px;
}

.nav-subtitle {
  margin-left: 12px;
  font-weight: 400;
  color: #EB6C84;
  cursor: default;
  font-size: 14px;
}

// 修复环形图容器可能的溢出问题
#xunjian_echart,
#lvxin_echart {
  width: 100% !important; // 让图表宽度自适应父容器
  height: 168.18px !important;
  margin-left: 0 !important; // 清除固定左间距，避免图表偏移
}

// 优化响应式：小屏幕下调整布局
@media (max-width: 1200px) {
  .card-container {
    flex-direction: column; // 垂直排列
    height: auto; // 取消固定高度
    padding: 20px;
  }
  .chart-container {
    margin-left: 0;
    margin-top: 20px;
    width: 100%; // 环形图容器占满宽度
    align-items: center; // 内部元素居中
  }
  .table-div {
    margin-left: 0;
    margin-top: 20px;
  }
}
</style>
