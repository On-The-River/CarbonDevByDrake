<template>
  <div class="root">
    <a class="nav-title">项目开发进展</a>
    <div class="container-div">
      <!-- 第一行 -->
      <div class="card-div">
        <left-card-arrow-bg :tetx="progress.first" />
        <div class="content-div">
          <div class="deliver">
            <img
              class="deliver-item"
              src="@/assets/imgs/bg_apply_project_deliver.png"
            />
          </div>
          <div class="card-left-div">
            <a class="card-left-4-text">选择方法学</a>
            <a class="card-left-4-text">创建项目</a>
            <a class="card-left-4-text">上传业主资料</a>
            <a style="margin-bottom: 0px" class="card-left-4-text"
            >查看项目情况</a
            >
          </div>
          <div class="card-empty"></div>
          <div class="card-right-div">
            <a class="card-right-4-text" ><router-link :to="{ path: '/carbon/methodology' }">去选择 </router-link> </a>
            <a class="card-right-4-text" href=""><router-link :to="{ path:'/carbon/projectCreate/projectAdd' }">去创建</router-link></a>
            <a class="card-right-4-text" href=""> <router-link :to="{ path: '/carbon/ownerAdd' }">去上传</router-link></a>
            <a class="card-right-4-text" href=""> <router-link :to="{ path: '/carbon/projectCreate' }">去查看</router-link></a>
          </div>
        </div>
      </div>
      <div class="card-div">
        <right-card-arrow-bg :tetx="progress.second" />
        <div class="content-div">
          <div class="deliver">
            <img
              class="deliver-item"
              src="@/assets/imgs/bg_apply_project_deliver.png"
            />
          </div>
          <div class="card-left-div">
            <a class="card-left-3-text">选择项目</a>
            <a class="card-left-3-text">添加监测数据</a>
            <a style="margin-bottom: 0px" class="card-left-3-text"
            >查看监测情况</a
            >
          </div>
          <div class="card-empty"></div>
          <div class="card-right-div">
            <a class="card-right-3-text"
            ><router-link :to="{ path: '/carbon/projectCreate' }"
            >去选择
            </router-link>
            </a>
            <a class="card-right-3-text"
            ><router-link :to="{ path: '/carbon/projectDo' }"
            >去添加
            </router-link>
            </a>
            <a style="margin-bottom: 0px" class="card-right-3-text"
            ><router-link :to="{ path: '/carbon/projectDo' }"
            >去查看
            </router-link></a
            >

          </div>
        </div>
      </div>

      <!-- 第二行 -->
      <div class="card-div">
        <left-card-arrow-bg :tetx="progress.third" />
        <div class="content-div">
          <div class="deliver">
            <img
              class="deliver-item"
              src="@/assets/imgs/bg_apply_project_deliver.png"
            />
          </div>
          <div class="card-left-div">
            <a class="card-left-3-text">选择项目</a>
            <a class="card-left-3-text">编写项目设计文档</a>
            <a style="margin-bottom: 0px" class="card-left-3-text"
            >查看开发进展</a
            >
          </div>
          <div class="card-empty"></div>
          <div class="card-right-div">
            <a class="card-right-3-text"><router-link :to="{ path: '/carbon/projectCreate' }">去选择 </router-link> </a>
            <a class="card-right-3-text"><router-link :to="{ path: '/carbon/projectDo' }">去编写 </router-link> </a>
            <a style="margin-bottom: 0px" class="card-right-3-text"><router-link :to="{ path: '/carbon/projectDo' }">去查看 </router-link></a>
          </div>
        </div>
      </div>
      <div class="card-div">
        <right-card-arrow-bg :tetx="progress.fourth" />
        <div class="content-div">
          <div class="deliver">
            <img
              class="deliver-item"
              src="@/assets/imgs/bg_apply_project_deliver.png"
            />
          </div>
          <div class="card-left-div">
            <a class="card-left-4-text">项目审定报告</a>
            <a class="card-left-4-text">项目备案书</a>
            <a class="card-left-4-text">项目核证报告</a>
            <a style="margin-bottom: 0px" class="card-left-4-text"
            >碳减排量核证签发</a
            >
          </div>
          <div class="card-empty"></div>
          <div class="card-right-div">
            <a class="card-right-4-text" href=""><router-link :to="{ path: '/carbon/projectFile' }">去上传</router-link></a>
            <a class="card-right-4-text" href=""><router-link :to="{ path: '/carbon/projectFile' }">去上传</router-link></a>
            <a class="card-right-4-text" href=""><router-link :to="{ path: '/carbon/projectFile' }">去上传</router-link></a>
            <a
              style="margin-bottom: 0px"
              class="card-right-4-text"
            ><router-link :to="{ path: '/carbon/projectFile' }">去上传</router-link></a
            >
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import leftCardArrowBg from "./leftCardArrowBg.vue";
import rightCardArrowBg from "./rightCardArrowBg.vue";
export default {
  name: "companyPackage",
  components: {
    leftCardArrowBg: leftCardArrowBg,
    rightCardArrowBg: rightCardArrowBg,
  },
  data() {
    return {
      progress: {
        first: "01 项目立项",
        second: "02 项目监测",
        third: "03 项目开发",
        fourth: "04 项目材料",
      },
      oneBg: {
        leftImg: "@/asstes/imgs/bg_apply_project_one_left.png",
        middleImg: "@/asstes/imgs/bg_apply_project_one_middle.png",
        rightImg: "@/asstes/imgs/bg_apply_project_one_right.png",
      },
      twoBg: {
        leftImg: "@/asstes/imgs/bg_apply_project_one_left.png",
        middleImg: "@/asstes/imgs/bg_apply_project_one_middle.png",
        rightImg: "@/asstes/imgs/bg_apply_project_one_right.png",
      },
    };
  },
  mounted() {},
  methods: {},
  created() {},
};
</script>

<style lang="scss" scoped>
.root {
  display: flex;
  flex-direction: column;
  padding: 0px 24px 0px 20px;
  margin: 0;
}

.nav-title {
  font-size: 18px;
  font-family: PingFangSC-Medium, PingFang SC;
  font-weight: 500;
  color: #080b0d;
  cursor: default;
}

.container-div {
  display: grid;
  grid-template-columns: 1fr 1fr; /* 两列布局 */
  grid-gap: 24px;
}

.card-div {
  display: flex;
  flex-direction: column;

  /* 添加蓝色边框 */
  .content-div {
    border: 2px solid #0c8351;
    transition: all 0.3s ease;
  }

  /* 鼠标悬停效果 */
  &:hover .content-div {
    border: 2px solid #f7f8fa;
    box-shadow: 0 0 10px rgb(247, 248, 250);
    transform: translateY(-2px);
  }
}

// 为标题组件添加蓝色边框样式
::v-deep .arrowbg {
  border: 2px solid #f7f8fa !important;
  border-radius: 8px;
}

.title-bar {
  display: table;
}

.project-text-one {
  display: table-cell;
  height: 48px;
  text-align: center;
  vertical-align: middle;
  margin: auto;
  font-size: 18px;
  font-family: Barlow-Medium, Barlow;
  font-weight: 500;
  color: #242b35;
}

.project-text-two {
  display: table-cell;
  height: 48px;
  text-align: center;
  vertical-align: middle;
  margin: auto;
  font-size: 18px;
  font-family: Barlow-Medium, Barlow;
  font-weight: 500;
  color: #242b35;
}

.content-div {
  margin-top: 2px;
  display: flex;
  flex-direction: row;
  height: 130px;
  //background: #ffffff;
  //box-shadow: 0px 2px 8px 0px #eaf0f3;
  border-radius: 8px;
  background-image: url("../../../assets/videos/feng10.png");
  background-position: right bottom;
  background-repeat: no-repeat;
}

.deliver {
  display: flex;
  height: 100%;
  margin-left: 41px;
}

.deliver-item {
  align-self: center;
  height: 94px;
}

.card-left-div {
  align-self: center;
  height: 94px;
  display: flex;
  flex-direction: column;
  margin-left: 11px;
}

.card-right-div {
  align-self: center;
  display: flex;
  height: 94px;
  flex-direction: column;
  margin-left: 11px;
  margin-right: 38px;
}

.card-empty {
  flex-grow: 1;
}

.card-left-4-text {
  font-size: 13px;
  font-family: PingFangSC-Regular, PingFang SC;
  font-weight: 400;
  color: #424c5c;
  margin-bottom: 12px;
  cursor: default;
}

.card-right-4-text {
  text-align: right;
  font-size: 13px;
  font-family: PingFangSC-Regular, PingFang SC;
  font-weight: 400;
  color: #0c8351;
  margin-bottom: 12px;
}

.card-left-3-text {
  font-size: 13px;
  font-family: PingFangSC-Regular, PingFang SC;
  font-weight: 400;
  color: #424c5c;
  margin-bottom: 25px;
  cursor: default;
}

.card-right-3-text {
  text-align: right;
  font-size: 13px;
  font-family: PingFangSC-Regular, PingFang SC;
  font-weight: 400;
  color: #0c8351;
  margin-bottom: 25px;
}

.arrowbg {
  position: relative;
  border: 2px solid #0c8351;
  border-radius: 8px;
}
</style>
