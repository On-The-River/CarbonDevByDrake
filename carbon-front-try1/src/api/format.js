/**
 * 获取文件URL和项目参数
 * @returns {Object} 包含url和token的对象
 */
export function getFileUrlAndProjectParameters() {
  const token = localStorage.getItem("token") || "";
  // 假设你的文件上传接口是 /api/file/upload
  const url = process.env.VUE_APP_BASE_API + "/api/file/upload";

  return {
    url,
    token
  };
}
