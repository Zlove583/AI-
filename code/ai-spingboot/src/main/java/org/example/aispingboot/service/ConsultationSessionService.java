package org.example.aispingboot.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.aispingboot.DTO.command.ConsultationSessionCreateDTO;
import org.example.aispingboot.DTO.response.ConsultationMessageResponseDTO;
import org.example.aispingboot.DTO.response.ConsultationSessionResponseDTO;
import org.example.aispingboot.entity.ConsultationSession;
import org.example.aispingboot.entity.User;
import org.example.aispingboot.exception.BusinessException;
import org.example.aispingboot.mapper.ConsultationSessionMapper;
import org.example.aispingboot.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ConsultationSessionService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ConsultationSessionMapper consultationSessionMapper;

    @Autowired
    private ConsultationMessageService consultationMessageService;

    public ConsultationSession createSession(Long userId, ConsultationSessionCreateDTO createDTO) {
        // 验证用户是否存在
        User user = userMapper.selectById(userId);
        if (user != null) {
            // 创建会话记录
            ConsultationSession session = ConsultationSession.builder()
                    .userId(userId)
                    .sessionTitle(createDTO.getSessionTitle())
                    .startedAt(LocalDateTime.now())
                    .build();
            // 如果未提供标题
            if (StrUtil.isBlank(createDTO.getSessionTitle())) {
                session.setSessionTitle(String.format("宁渡AI助手 - " + DateUtil.format(LocalDateTime.now(), "MM-dd HH:mm")));
            }

            // 插入记录
            consultationSessionMapper.insert(session);
            return session;
        }

        return null;
    }

    /**
     * 分页查询会话列表，管理员可查看全部，普通用户仅可查看自己的会话
     */
    public Map<String, Object> pageSessions(Long userId, boolean admin, long pageNum, long pageSize) {
        Page<ConsultationSession> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ConsultationSession> queryWrapper = new LambdaQueryWrapper<>();
        if (!admin) {
            queryWrapper.eq(ConsultationSession::getUserId, userId);
        }
        queryWrapper.orderByDesc(ConsultationSession::getStartedAt);
        Page<ConsultationSession> result = consultationSessionMapper.selectPage(page, queryWrapper);

        Map<Long, String> userNames = new HashMap<>();
        List<ConsultationSessionResponseDTO> records = new ArrayList<>();
        for (ConsultationSession session : result.getRecords()) {
            records.add(toResponseDTO(session, userNames));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", records);
        data.put("total", result.getTotal());
        data.put("current", result.getCurrent());
        data.put("size", result.getSize());
        return data;
    }

    /**
     * 查询某个会话的完整消息列表
     */
    public List<ConsultationMessageResponseDTO> listMessages(Long sessionId, Long userId, boolean admin) {
        getAccessibleSession(sessionId, userId, admin);
        return consultationMessageService.listBySessionId(sessionId);
    }

    /**
     * 查询会话最近一次情绪分析结果
     */
    public Object getSessionEmotion(Long sessionId, Long userId, boolean admin) {
        ConsultationSession session = getAccessibleSession(sessionId, userId, admin);
        String json = session.getLastEmotionAnalysis();
        if (StrUtil.isBlank(json)) {
            return new JSONObject();
        }
        try {
            return JSONUtil.parseObj(json);
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    /**
     * 删除会话，同时级联删除会话消息
     */
    public void deleteSession(Long sessionId, Long userId, boolean admin) {
        ConsultationSession session = consultationSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        if (!admin && !Objects.equals(session.getUserId(), userId)) {
            throw new BusinessException("无权操作该会话");
        }
        consultationMessageService.deleteBySessionId(sessionId);
        consultationSessionMapper.deleteById(sessionId);
    }

    private ConsultationSession getAccessibleSession(Long sessionId, Long userId, boolean admin) {
        ConsultationSession session = consultationSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        if (!admin && !Objects.equals(session.getUserId(), userId)) {
            throw new BusinessException("无权操作该会话");
        }
        return session;
    }

    private ConsultationSessionResponseDTO toResponseDTO(ConsultationSession session, Map<Long, String> userNames) {
        int messageCount = consultationMessageService.getMessageCountBySessionId(session.getId());
        ConsultationMessageResponseDTO lastMessage = consultationMessageService.getLastMessageBySessionId(session.getId());

        long durationMinutes = 0L;
        if (session.getStartedAt() != null && lastMessage != null && lastMessage.getCreatedAt() != null) {
            durationMinutes = Math.max(0L, Duration.between(session.getStartedAt(), lastMessage.getCreatedAt()).toMinutes());
        }

        String nickname = userNames.computeIfAbsent(session.getUserId(), id -> {
            User user = id == null ? null : userMapper.selectById(id);
            return user == null ? "未知用户" : user.getDisplayName();
        });

        String preview = lastMessage == null ? "" : lastMessage.getContent();
        if (preview != null && preview.length() > 100) {
            preview = preview.substring(0, 100) + "...";
        }

        return ConsultationSessionResponseDTO.builder()
                .id(session.getId())
                .sessionId("session_" + session.getId())
                .userId(session.getUserId())
                .userNickname(nickname)
                .sessionTitle(session.getSessionTitle())
                .startedAt(session.getStartedAt())
                .lastMessageContent(preview)
                .lastMessageTime(lastMessage == null ? null : lastMessage.getCreatedAt())
                .messageCount(messageCount)
                .durationMinutes(durationMinutes)
                .build();
    }
}
