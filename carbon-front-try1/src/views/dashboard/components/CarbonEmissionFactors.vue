<template>
  <div class="root">
    <div class="fl">
      <div>
        <a class="nav-title">碳排放影响因素分析</a>
      </div>
      <div>
        <a class="more-text white-text" href="/trade/quotation" style="margin-right:22px;">查看更多 ></a>
      </div>
    </div>
    <div class="root-tab">
      <div class="card-container bg-with-img">
        <div class="chart-container-full">
          <div class="charts-container">
            <div class="chart-item">
              <div ref="treeTypeChart" class="chart"></div>
            </div>
            <div class="chart-item">
              <div ref="soilTypeChart" class="chart"></div>
            </div>
            <div class="chart-item">
              <div ref="climateTypeChart" class="chart"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import { getBarTreeWithType, getBarTreeWithSoil, getBarTreeWithClimate } from '@/api/tradeApi';

export default {
  name: 'CarbonEmissionFactors',
  data() {
    return {
      treeTypeChart: null,
      soilTypeChart: null,
      climateTypeChart: null
    };
  },
  mounted() {
    this.initCharts();
    this.loadChartData();
  },
  beforeDestroy() {
    if (this.treeTypeChart) {
      this.treeTypeChart.dispose();
    }
    if (this.soilTypeChart) {
      this.soilTypeChart.dispose();
    }
    if (this.climateTypeChart) {
      this.climateTypeChart.dispose();
    }
  },
  methods: {
    initCharts() {
      this.treeTypeChart = echarts.init(this.$refs.treeTypeChart);
      this.soilTypeChart = echarts.init(this.$refs.soilTypeChart);
      this.climateTypeChart = echarts.init(this.$refs.climateTypeChart);
    },

    loadChartData() {
      this.loadTreeTypeData();
      this.loadSoilTypeData();
      this.loadClimateTypeData();
    },

    loadTreeTypeData(){
      getBarTreeWithType().then(response=>{
        if(response.data){
          this.renderPieChart(this.treeTypeChart, response.data, '树木类型分布')
        }
      }).catch(error=>{
        console.error('获取树木类型数据失败:', error)
      });
    },

    loadSoilTypeData() {
      getBarTreeWithSoil().then(response => {
        if (response.data) {
          this.renderPieChart(this.soilTypeChart, response.data, '土壤质量分布');
        }
      }).catch(error => {
        console.error('获取土壤质量数据失败:', error);
      });
    },

    loadClimateTypeData() {
      getBarTreeWithClimate().then(response => {
        if (response.data) {
          this.renderPieChart(this.climateTypeChart, response.data, '气候类型分布');
        }
      }).catch(error => {
        console.error('获取气候类型数据失败:', error);
      });
    },

    renderPieChart(chart, data, title) {
      // 将数据转换为 echarts 需要的格式
      const chartData = Object.entries(data).map(([name, value]) => ({
        name,
        value: parseFloat(value)
      }));

      // 绿色系配色方案
      const colorPalette = [
        '#32a852', '#4caf50', '#8bc34a', '#cddc39', '#2e7d32',
        '#689f38', '#9ccc65', '#aed581', '#558b2f', '#827717'
      ];

      const option = {
        title: {
          text: title,
          left: 'center',
          textStyle: {
            fontSize: 14,
            fontWeight: 'normal'
          }
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c}TCO2e (占比 {d}%)'
        },
        legend: {
          orient: 'horizontal', // 水平布局（默认就是 horizontal，确保明确设置）
          bottom: -55,          // 底部紧贴容器，避免与饼图重叠
          left: '10%',          // 左侧留间距，避免贴边
          right: '10%',         // 右侧留间距，避免贴边
          textStyle: {
            fontSize: 10
          },
          padding: [0, 0, 55, 0], // 底部内边距，给图例留呼吸空间
          itemWidth: 12,         // 图例项宽度（可选，让图例更紧凑）
          itemHeight: 8         // 图例项高度（可选，让图例更紧凑）

        },
        series: [
          {
            name: title,
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 6,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: '12',
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: chartData,
            color: colorPalette
          }
        ]
      };

      chart.setOption(option);
    }
  }
};
</script>

<style scoped>
.root {
  display: flex;
  flex-direction: column;
  margin-top: 20px;
  margin-left: 20px;
}

.bg-with-img {
  background-image: url('../../../assets/videos/feng11.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  position: relative;  /* 添加这行 */
  z-index: 0;          /* 设置为最低层级 */
}

.white-text {
  color: #000000 !important;
  text-shadow: 0 0 2px rgba(0, 0, 0, 0.5);
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
  position: relative;
}

.chart-container-full {
  flex: 1;
  padding: 20px;
  position: relative;
  z-index: 1;
}

.fl {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-right: 24px;
  margin-bottom: 10px;
}

.nav-title {
  margin-top: 20px;
  font-weight: 500;
  color: #080E0D;
  cursor: default;
  font-size: 16px;
}

.charts-container {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  height: 100%;
  z-index: 1;
}

.chart-item {
  width: 32%;
  min-width: 320px;
  margin-bottom: 20px;
  z-index: 5;
}

.chart {
  width: 100%;
  min-height: 300px;
  position: relative;
  z-index: 10;
  align: 'center'
}

@media (max-width: 1200px) {
  .card-container {
    height: auto;
    min-height: 368px;
  }

  .chart-item {
    width: 100%;
  }
}
</style>
