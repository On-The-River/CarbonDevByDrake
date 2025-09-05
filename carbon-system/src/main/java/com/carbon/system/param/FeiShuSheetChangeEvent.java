package com.carbon.system.param;

import lombok.Data;

import java.util.List;

@Data
public class FeiShuSheetChangeEvent {
    private String schema;       // 事件格式版本
    private String challenge;    // 验证challenge
    private String token;        // 验证token
    private String type;         // 事件类型
    private Event event;         // 事件内容
    private Header header;       // 事件头

    @Data
    public static class Header {
        private String event_id;          // 事件 ID
        private String event_type;        // 事件类型（必须为 "sheet.content.changed_v2"）
        private String create_time;       // 创建时间戳
        private String tenant_key;        // 租户 key
        private String app_id;            // 应用 ID
        private String token;             // 事件 token
    }

    @Data
    public static class Event {
        private String file_token;       // 文件token
        private String file_type;        // 文件类型
        private String sheet_id;          // 工作表ID
        private String spreadsheet_token; // 表格 token

        private List<ChangedRange> changed_ranges; // 变更的单元格范围（核心数据）
        private Operator operator;        // 操作者信息

        @Data
        public static class ChangedRange {
            private String range;         // 变更范围（如 "A1:B2"）
            private Integer start_row;    // 起始行（从 1 开始）
            private Integer end_row;      // 结束行
            private Integer start_col;    // 起始列（从 1 开始）
            private Integer end_col;      // 结束列
            private List<List<String>> after_value; // 变更后的值（二维数组）
        }
        @Data
        public static class Operator {
            private String open_id;       // 操作者开放 ID
            private String user_id;       // 操作者用户 ID（需权限）
            private String union_id;      // 操作者唯一标识
        }
    }
}
