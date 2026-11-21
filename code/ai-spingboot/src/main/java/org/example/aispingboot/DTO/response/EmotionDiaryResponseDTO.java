package org.example.aispingboot.DTO.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 情绪日记响应
 */
@Data
@Builder
public class EmotionDiaryResponseDTO {
    private Long id;

    private Long userId;

    private String username;

    private String nickname;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate diaryDate;

    private Integer moodScore;

    private String dominantEmotion;

    private String emotionTriggers;

    private String diaryContent;

    private Integer sleepQuality;

    private Integer stressLevel;

    private String aiEmotionAnalysis;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime aiAnalysisUpdatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
