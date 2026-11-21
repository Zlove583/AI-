package org.example.aispingboot.DTO.command;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 情绪日记提交参数
 */
@Data
public class EmotionDiaryCreateDTO {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate diaryDate;

    @NotNull(message = "情绪评分不能为空")
    @Min(value = 1, message = "情绪评分不能小于1")
    @Max(value = 10, message = "情绪评分不能大于10")
    private Integer moodScore;

    @Size(max = 50, message = "主要情绪长度不能超过50个字符")
    private String dominantEmotion;

    private String emotionTriggers;

    private String diaryContent;

    @Min(value = 1, message = "睡眠质量不能小于1")
    @Max(value = 5, message = "睡眠质量不能大于5")
    private Integer sleepQuality;

    @Min(value = 1, message = "压力水平不能小于1")
    @Max(value = 5, message = "压力水平不能大于5")
    private Integer stressLevel;
}
