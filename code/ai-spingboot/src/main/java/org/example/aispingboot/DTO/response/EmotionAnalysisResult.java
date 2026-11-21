package org.example.aispingboot.DTO.response;

import java.util.List;

/**
 * AI 情绪分析结果
 */
public record EmotionAnalysisResult(
        String primaryEmotion,
        Integer emotionScore,
        Integer riskLevel,
        Boolean isNegative,
        String suggestion,
        String riskDescription,
        List<String> improvementSuggestions
) {
}
