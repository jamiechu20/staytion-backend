package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "房東回覆評論的請求資料")
public class ReviewReplyDto {
    @Schema(description = "回覆內容", example = "謝謝您的建議，我們會改善")
    private String reply;
    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
}
