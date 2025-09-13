<template>
  <div class="root">
    <div>
      <a class="nav-title">碳行业资讯</a>
    </div>
    <div class="card-container">
      <!-- 左侧侧两个两个两个并列部分 -->
      <div class="left-column">
        <!-- 动态部分 -->
        <div class="news-section">
          <div class="section-header">
            <a class="section-title">动态</a>
            <a class="more-text" @click="openMoreNews">查看更多</a>
          </div>
          <div class="section-content">
            <news-list />
          </div>
        </div>

        <!-- 知识库部分 -->
        <div class="knowledge-section">
          <div class="section-header">
            <a class="section-title">知识库</a>
            <a class="more-text" @click="openMoreKnowledge">查看更多</a>
          </div>
          <div class="section-content">
            <knowleage-list />
          </div>
        </div>
      </div>

      <!-- 右侧两个并列部分 -->
      <div class="right-column">
        <!-- 公告部分 -->
        <div class="notice-section">
          <div class="section-header">
            <a class="section-title">公告</a>
            <a class="more-text" @click="openMoreNotic">查看更多</a>
          </div>
          <div class="section-content">
            <notice-list />
          </div>
        </div>

        <!-- 常见问题部分 -->
        <div class="question-section">
          <div class="section-header">
            <a class="section-title">常见问题</a>
            <a class="more-text" @click="openMoreQuestion">查看更多</a>
          </div>
          <div class="section-content">
            <question-list />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import knowleageList from './knowleageList.vue';
import newsList from './newsList.vue';
import questionList from './questionList.vue';
import noticeList from './noticeList.vue';
import { openUrlInNewWindow } from '@/libs/OpenHelper'

export default {
  name: 'news',
  components: {
    'news-list': newsList,
    'knowleage-list': knowleageList,
    'notice-list': noticeList,
    'question-list': questionList
  },
  data() {
    return {
      moreNewsUrl: 'https://carbonmsger.feishu.cn/drive/folder/fldenvWTRci2VfYTyzXG06oIg',
      moreKnowUrl: 'https://carbonmsger.feishu.cn/drive/folder/fldcrpJBeSkS20cvzW7MRGd?from=space_personal_filelist',
      moreQuestUrl: 'https://questionlist.feishu.cn/drive/folder/fldcrpJBeSkS20cvzW7MRGd?from=space_personal_filelist',
      moreNoticUrl: 'https://carbonmsger.feishu.cn/drive/folder/fldenJxL0Wn9m0Dnacz4igvSg'
    }
  },
  methods: {
    openMoreQuestion() {
      openUrlInNewWindow(this.moreQuestUrl);
    },
    openMoreNotic() {
      openUrlInNewWindow(this.moreNoticUrl);
    },
    openMoreNews() {
      openUrlInNewWindow(this.moreNewsUrl);
    },
    openMoreKnowledge() {
      openUrlInNewWindow(this.moreKnowUrl);
    }
  }
}
</script>

<style lang="scss" scoped>
.root {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 489px;
  margin-top: 20px;
  margin-left: 20px;
  margin-bottom: 10px;
}

.card-container {
  margin-top: 0px;
  margin-right: 20px;
  height: 100%;
  display: flex;
  gap: 20px;
}

.left-column,
.right-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

// 为四个部分添加蓝色边框
.news-section,
.knowledge-section,
.notice-section,
.question-section {
  flex: 1;
  background: #FFFFFF;
  box-shadow: 0px 2px 8px 0px #EAF0F3;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  // 添加蓝色边框
  border: 2px solid #0c8351; /* 蓝色边框 */

  // 添加背景图片
  background-image: url("../../../assets/videos/bei3.png"); // 请根据实际图片路径调整
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;

  // 添加半透明遮罩以确保内容可读
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.9);
    border-radius: 8px;
    z-index: 1;
  }
}

.section-header {
  display: flex;
  flex-direction: row;
  padding: 15px 20px 10px 20px;
  position: relative;
  z-index: 2;
}

.section-title {
  font-weight: 500;
  color: #242E35;
  font-size: 16px;
}

.more-text {
  flex-grow: 1;
  text-align: right;
  font-weight: 400;
  color: #5E6C84;
  cursor: pointer;
  font-size: 14px;
}

.section-content {
  flex: 1;
  padding: 0 10px 10px 10px;
  position: relative;
  z-index: 2;
  overflow: hidden;
}

.nav-title {
  margin-top: 20px;
  font-weight: 500;
  color: #080B0D;
  cursor: default;
  font-size: 18px;
}

//// 为不同部分添加不同的背景图片（可选）
//.news-section {
//  background-image: url("@/assets/images/news-dynamic-bg.jpg");
//}
//
//.knowledge-section {
//  background-image: url("@/assets/images/news-knowledge-bg.jpg");
//}
//
//.notice-section {
//  background-image: url("@/assets/images/news-notice-bg.jpg");
//}
//
//.question-section {
//  background-image: url("@/assets/images/news-question-bg.jpg");
//}
</style>
