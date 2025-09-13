<template>
  <div class="root">
    <div class="fl">
      <div>
        <a class="nav-title">碳交易价格预测 (未来5天)</a>
      </div>
      <div>
        <a class="more-text white-text" href="/trade/quotation" style="margin-right:22px;">查看更多 ></a>
      </div>
    </div>
    <div class="root-tab">
      <div class="card-container bg-with-img">
        <div class="chart-container-full">
          <div ref="chartContainer" style="width:100%;height:330px"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import {predictTree} from "@/api/tradeApi";

export default {
  name: 'PricePredictionChart',
  data() {
    return {
      chart: null,
      predictionData: []
    };
  },
  mounted() {
    this.fetchPredictionData();
  },
  beforeDestroy() {
    if (this.chart) {
      this.chart.dispose();
    }
  },
  methods: {
    fetchPredictionData(){
      predictTree().then(response => {
        if (response.code === 200) {
          // 处理数据，跳过标题行
          const rawData = response.data.slice(1); // 跳过标题行
          this.predictionData = rawData;
          this.initChart();
        }else{
          console.error('API返回错误:', response);
        }
      }).catch(error => {
        console.error('获取碳市场数据失败:', error);
      });
    },

    initChart() {
      if (!this.$refs.chartContainer) return;

      // 销毁之前的图表实例
      if (this.chart) {
        this.chart.dispose();
      }

      // 初始化图表
      this.chart = echarts.init(this.$refs.chartContainer);

      // 处理数据
      const dates = this.predictionData.map(item => item[0]);
      const minPrices = this.predictionData.map(item => parseFloat(item[1]));
      const maxPrices = this.predictionData.map(item => parseFloat(item[2]));
      const medianPrices = this.predictionData.map(item => parseFloat(item[3]));
      const priceRanges = this.predictionData.map(item => parseFloat(item[4]));

      // 图表配置
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross'
          }
        },
        legend: {
          data: ['预测最低价', '预测最高价', '预测中位价', '价格区间宽度']
        },
        grid: {
          left: '10%',
          right: '15%',
          bottom: '15%',
          containLabel: true
        },
        xAxis: [
          {
            type: 'category',
            axisTick: {
              alignWithLabel: true
            },
            data: dates
          }
        ],
        yAxis: [
          {
            type: 'value',
            name: '价格',
            position: 'left',
            axisLabel: {
              formatter: '{value} 元'
            }
          },
          {
            type: 'value',
            name: '区间宽度',
            position: 'right',
            axisLabel: {
              formatter: '{value} 元'
            }
          }
        ],
        series: [
          {
            name: '预测最低价',
            type: 'line',
            yAxisIndex: 0,
            data: minPrices,
            smooth: true,
            symbolSize: 8,
            itemStyle: {
              color: '#5470c6'
            }
          },
          {
            name: '预测最高价',
            type: 'line',
            yAxisIndex: 0,
            data: maxPrices,
            smooth: true,
            symbolSize: 8,
            itemStyle: {
              color: '#91cc75'
            }
          },
          {
            name: '预测中位价',
            type: 'line',
            yAxisIndex: 0,
            data: medianPrices,
            smooth: true,
            symbolSize: 8,
            itemStyle: {
              color: '#fac858'
            }
          },
          {
            name: '价格区间宽度',
            type: 'line',
            yAxisIndex: 1,
            data: priceRanges,
            smooth: true,
            symbolSize: 8,
            itemStyle: {
              color: '#ee6666'
            }
          }
        ]
      };

      // 设置图表配置
      this.chart.setOption(option);

      // 监听窗口大小变化，自适应调整图表
      window.addEventListener('resize', this.resizeChart);
    },

    resizeChart() {
      if (this.chart) {
        this.chart.resize();
      }
    }
  },

  watch: {
    predictionData: {
      handler() {
        this.$nextTick(() => {
          this.initChart();
        });
      },
      deep: true
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


  .bg-with-img {
    background-image: url('../../../assets/videos/feng11.png');
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
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
  }
}

.chart-container-full {
  flex: 1;
  padding: 20px;
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

@media (max-width: 1200px) {
  .card-container {
    height: auto;
    min-height: 368px;
  }
}
</style>
