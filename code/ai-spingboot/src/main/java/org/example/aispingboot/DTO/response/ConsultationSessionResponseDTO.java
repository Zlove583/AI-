package org.example.aispingboot.DTO.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 咨询会话列表响应
 */
@Data
@Builder
public class ConsultationSessionResponseDTO {
    private Long id;

    private String sessionId;

    private Long userId;

    private String userNickname;

    private String sessionTitle;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startedAt;

    private String lastMessageContent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime lastMessageTime;

    private Integer messageCount;

    private Long durationMinutes;
}
