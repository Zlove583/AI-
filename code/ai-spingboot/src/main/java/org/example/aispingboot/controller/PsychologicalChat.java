package org.example.aispingboot.controller;

import cn.hutool.json.JSONUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.validation.Valid;
import org.example.aispingboot.AiService.PsychologicalSupportService;
import org.example.aispingboot.AiService.StructOutPut;
import org.example.aispingboot.DTO.command.ConsultationSessionCreateDTO;
import org.example.aispingboot.DTO.command.ConsultationStreamDTO;
import org.example.aispingboot.DTO.response.ConsultationMessageResponseDTO;
import org.example.aispingboot.common.Result;
import org.example.aispingboot.common.ResultCode;
import org.example.aispingboot.service.ConsultationSessionService;
import org.example.aispingboot.util.AuthUtil;
import org.example.aispingboot.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.result.view.Fragment;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/psychological-chat")
public class PsychologicalChat {
    @Autowired
    private PsychologicalSupportService psychologicalSupportService;

    @Autowired
    private ConsultationSessionService consultationSessionService;

    @PostMapping("/session/start")
    public Result<StructOutPut.StreamChatSession> startSession(@Valid @RequestBody ConsultationSessionCreateDTO createDTO) {
        // 获取当前用户
        String token = JwtTokenUtil.getCurrentToken();
        DecodedJWT jwt = JwtTokenUtil.verifyToken(token);
        Long userId = jwt.getClaim("userId").asLong();
        StructOutPut.StreamChatSession session = psychologicalSupportService.startSession(userId, createDTO);
        return Result.ok(session);
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@Valid @RequestBody ConsultationStreamDTO streamDTO) {
        // 获取当前用户
        String token = JwtTokenUtil.getCurrentToken();
        DecodedJWT jwt = JwtTokenUtil.verifyToken(token);
        Long userId = jwt.getClaim("userId").asLong();

        if (userId == null) {
            return Flux.just(ServerSentEvent.<String>builder()
                    .event("error")
                    .data(JSONUtil.toJsonStr(Result.error(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMsg(), "用户未登录")))
                    .build());
        }

        // 开始流式对话
        return psychologicalSupportService.streamPsychologicalChat(streamDTO.getSessionId(), streamDTO.getUserMessage())
                .map(Fragment -> {
                    return ServerSentEvent.<String>builder()
                            .event("message")
                            .data(JSONUtil.toJsonStr(Result.ok(Map.of("content", Fragment, "type", "normal"))))
                            .build();
                })
                .concatWith(Flux.just(ServerSentEvent.<String>builder()
                        .event("done")
                        .data("{}")
                        .build()
                ))
                .delayElements(Duration.ofMillis(50)); // 添加延迟确保流式数据的体验
    }

    /**
     * 分页查询会话列表，管理员可查看全部会话
     */
    @GetMapping("/sessions")
    public Result<Map<String, Object>> listSessions(
            @RequestParam(value = "pageNum", required = false) Long pageNum,
            @RequestParam(value = "pageSize", required = false) Long pageSize,
            @RequestParam(value = "currentPage", required = false) Long currentPage,
            @RequestParam(value = "size", required = false) Long size) {
        long current = pageNum != null ? pageNum : (currentPage != null ? currentPage : 1L);
        long pageSizeValue = pageSize != null ? pageSize : (size != null ? size : 10L);
        return Result.ok(consultationSessionService.pageSessions(
                AuthUtil.currentUserId(), AuthUtil.isAdmin(), Math.max(current, 1L), Math.max(pageSizeValue, 1L)));
    }

    /**
     * 查询会话的完整消息记录
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<ConsultationMessageResponseDTO>> listMessages(@PathVariable String sessionId) {
        return Result.ok(consultationSessionService.listMessages(
                parseSessionId(sessionId), AuthUtil.currentUserId(), AuthUtil.isAdmin()));
    }

    /**
     * 查询会话最近一次情绪分析结果
     */
    @GetMapping("/session/{sessionId}/emotion")
    public Result<Object> getSessionEmotion(@PathVariable String sessionId) {
        return Result.ok(consultationSessionService.getSessionEmotion(
                parseSessionId(sessionId), AuthUtil.currentUserId(), AuthUtil.isAdmin()));
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/sessions/{sessionId}")
    public Result<Void> deleteSession(@PathVariable String sessionId) {
        consultationSessionService.deleteSession(
                parseSessionId(sessionId), AuthUtil.currentUserId(), AuthUtil.isAdmin());
        return Result.ok();
    }

    private Long parseSessionId(String sessionId) {
        String value = sessionId == null ? "" : sessionId.trim();
        if (value.startsWith("session_")) {
            value = value.substring("session_".length());
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            throw new org.example.aispingboot.exception.BusinessException("会话ID格式不正确");
        }
    }
}
