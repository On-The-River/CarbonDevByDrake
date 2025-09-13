<template>
  <div class="root">
    <div class="fl">
      <div>
        <a class="nav-title">碳交易商品行情</a>
      </div>
      <div style="margin-left: 22px;">
        <!-- 标签页已注释，保留样式类备用 -->
      </div>
      <div>
        <a class="more-text white-text" href="/trade/quotation" style="margin-right:22px;">查看更多 ></a>
      </div>
    </div>

    <div class="root-tab">
      <div class="card-container bg-with-img">
        <!-- 折线图容器 -->
        <div class="chart-container-full">
          <div id="trading_chart" style="width:100%;height:330px" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import { getBlockTradingData } from '@/api/tradeApi'

export default {
  name: 'carbonTradingChart',
  data() {
    return {
      tradingData: []
    }
  },
  mounted() {
    this.loadTradingData();
  },
  methods: {
    loadTradingData() {
      getBlockTradingData().then(response => {
        if (response.code === 200) {
          // 处理数据，跳过标题行
          const rawData = response.data.slice(1); // 跳过标题行
          this.tradingData = rawData.map(item => ({
            date: item[0],           // 日期
            volume: parseFloat(item[1]),         // 成交量
            amount: parseFloat(item[2]),         // 成交额
            price: parseFloat(item[3]), // 均价
            premium: parseFloat(item[4])         // 折溢价
          }));
          this.drawChart();
        }
      }).catch(error => {
        console.error('获取碳交易数据失败:', error);
      });
    },

    drawChart() {
      const chartDom = document.getElementById('trading_chart');
      const myChart = echarts.init(chartDom);

      // 准备数据（按日期倒序排列，最新的在右边）
      const sortedData = [...this.tradingData].reverse();
      const dates = sortedData.map(item => item.date);
      const volumes = sortedData.map(item => (item.volume / 10000).toFixed(2)); // 转换为万吨
      const amounts = sortedData.map(item => (item.amount / 100000000).toFixed(2)); // 转换为亿元
      const prices = sortedData.map(item => item.price);
      const premiums = sortedData.map(item => item.premium);

      const option = {
        color: ['#28E891', '#009EFF', '#26B581', '#4EDEB7', '#1A7F57'],
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(0, 0, 0, 0.7)',
          borderColor: '#28E891',
          borderWidth: 1,
          textStyle: {
            color: '#ffffff'
          },
          formatter: function(params) {
            let result = params[0].name + '<br/>';
            params.forEach(param => {
              let value = '';
              if (param.seriesName === '成交量') {
                value = param.value + ' 万吨';
              } else if (param.seriesName === '成交额') {
                value = param.value + ' 亿元';
              } else if (param.seriesName === '均价') {
                value = param.value + ' 元';
              } else if (param.seriesName === '折溢价') {
                value = param.value + ' %';
              }
              result += `<span style="display:inline-block;margin-right:5px;border-radius:10px;width:10px;height:10px;background-color:${param.color};"></span>${param.seriesName}: ${value}<br/>`;
            });
            return result;
          }
        },
        legend: {
          data: ['成交量', '成交额', '均价', '折溢价'],
          textStyle: {
            color: '#000000'
          },
          top: 10
        },
        grid: {
          left: '8%',
          right: '8%',
          bottom: '15%',
          top: '20%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: dates,
          axisLabel: {
            color: '#000000',
            rotate: 45,
            fontSize: 12
          },
          axisLine: {
            lineStyle: {
              color: '#000000'
            }
          },
          axisTick: {
            lineStyle: {
              color: '#000000'
            }
          }
        },
        yAxis: [
          {
            type: 'value',
            name: '成交量(万吨)/成交额(亿元)',
            nameTextStyle: {
              color: '#000000',
              fontSize: 12
            },
            axisLabel: {
              color: '#000000',
              fontSize: 12
            },
            axisLine: {
              lineStyle: {
                color: '#000000'
              }
            },
            splitLine: {
              show: true,
              lineStyle: {
                color: 'rgba(255, 255, 255, 0.1)'
              }
            }
          },
          {
            type: 'value',
            name: '均价(元)',
            nameTextStyle: {
              color: '#000000',
              fontSize: 12
            },
            axisLabel: {
              color: '#000000',
              fontSize: 12
            },
            axisLine: {
              lineStyle: {
                color: '#000000'
              }
            },
            splitLine: {
              show: false
            }
          },
          {
            type: 'value',
            name: '折溢价(%)',
            nameTextStyle: {
              color: '#000000',
              fontSize: 12
            },
            axisLabel: {
              color: '#000000',
              fontSize: 12
            },
            axisLine: {
              lineStyle: {
                color: '#000000'
              }
            },
            splitLine: {
              show: false
            },
            show: false,
          }
        ],
        series: [
          {
            name: '成交量',
            type: 'line',
            data: volumes,
            smooth: true,
            symbolSize: 6,
            lineStyle: {
              width: 2
            },
            itemStyle: {
              color: '#28E891' // 浅绿色
            },
            showSymbol: false,
            emphasis: {
              focus: 'series'
            }
          },
          {
            name: '成交额',
            type: 'line',
            data: amounts,
            smooth: true,
            symbolSize: 6,
            lineStyle: {
              width: 2
            },
            itemStyle: {
              color: '#26B581' // 中绿色
            },
            showSymbol: false,
            emphasis: {
              focus: 'series'
            }
          },
          {
            name: '均价',
            type: 'line',
            yAxisIndex: 1,
            data: prices,
            smooth: true,
            symbolSize: 6,
            lineStyle: {
              width: 2
            },
            itemStyle: {
              color: '#4EDEB7' // 浅蓝绿色
            },
            showSymbol: false,
            emphasis: {
              focus: 'series'
            }
          },
          {
            name: '折溢价',
            type: 'line',
            yAxisIndex: 2,
            data: premiums,
            smooth: true,
            symbolSize: 6,
            lineStyle: {
              width: 2
            },
            itemStyle: {
              color: '#1A7F57' // 深绿色
            },
            showSymbol: false,
            emphasis: {
              focus: 'series'
            }
          }
        ],
        dataZoom: [
          {
            type: 'inside',
            start: 0,
            end: 100
          }
        ]
      };

      myChart.setOption(option);

      // 窗口 resize 时重绘图表
      window.addEventListener('resize', () => {
        myChart.resize();
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

  // 核心容器背景图
  .bg-with-img {
    background-image: url('../../../assets/videos/feng11.png');
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
  }

  // 文字通用类
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

// 优化响应式：小屏幕下调整布局
@media (max-width: 1200px) {
  .card-container {
    height: auto;
    min-height: 368px;
  }
}
</style>
