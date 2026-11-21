package org.example.aispingboot.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.aispingboot.DTO.response.EmotionAnalysisResult;
import org.example.aispingboot.entity.ConsultationSession;
import org.example.aispingboot.mapper.ConsultationSessionMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 会话情绪分析服务：在对话过程中分析用户当前情绪并写入会话记录
 */
@Slf4j
@Service
public class EmotionAnalysisService {

    private static final String SYSTEM_PROMPT = """
            你是心理健康领域的情绪分析助手。请阅读用户发来的一段话，判断其当前的主要情绪和风险等级。
            要求：
            1. primaryEmotion 使用简短中文情绪词，例如：焦虑、悲伤、愤怒、平静、开心、压力、孤独。
            2. emotionScore 为 0-100 的情绪强度，数值越高代表情绪越强烈。
            3. riskLevel 取值 0-3：0 表示正常，1 表示需要关注，2 表示预警，3 表示危机（如出现自伤或自杀倾向）。
            4. isNegative 表示是否为负面情绪。
            5. suggestion 给出一句温和、具体的建议。
            6. riskDescription 描述风险原因，没有风险时返回空字符串。
            7. improvementSuggestions 给出 2 到 3 条具体可执行的建议。
            请只输出符合上述字段的 JSON。
            """;

    private final ChatClient chatClient;

    private final ConsultationSessionMapper consultationSessionMapper;

    private final ObjectMapper objectMapper;

    private final ExecutorService executor = Executors.newFixedThreadPool(2, runnable -> {
        Thread thread = new Thread(runnable, "emotion-analysis");
        thread.setDaemon(true);
        return thread;
    });

    public EmotionAnalysisService(OpenAiChatModel openAiChatModel,
                                  ConsultationSessionMapper consultationSessionMapper,
                                  ObjectMapper objectMapper) {
        this.chatClient = ChatClient.builder(openAiChatModel).build();
        this.consultationSessionMapper = consultationSessionMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 异步分析用户消息的情绪，避免阻塞流式回复
     */
    public void analyzeAsync(Long sessionId, String userMessage) {
        if (sessionId == null || StrUtil.isBlank(userMessage)) {
            return;
        }
        executor.submit(() -> {
            try {
                EmotionAnalysisResult result = chatClient.prompt()
                        .system(SYSTEM_PROMPT)
                        .user(userMessage)
                        .call()
                        .entity(EmotionAnalysisResult.class);

                if (result == null) {
                    return;
                }
                consultationSessionMapper.update(null, new LambdaUpdateWrapper<ConsultationSession>()
                        .eq(ConsultationSession::getId, sessionId)
                        .set(ConsultationSession::getLastEmotionAnalysis, objectMapper.writeValueAsString(result))
                        .set(ConsultationSession::getLastEmotionUpdatedAt, LocalDateTime.now()));
            } catch (Exception e) {
                log.warn("会话 {} 情绪分析失败: {}", sessionId, e.getMessage());
            }
        });
    }
}
