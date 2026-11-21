package org.example.aispingboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.aispingboot.entity.ConsultationMessage;
import org.example.aispingboot.entity.ConsultationSession;
import org.example.aispingboot.entity.EmotionDiary;
import org.example.aispingboot.entity.User;
import org.example.aispingboot.mapper.ConsultationMessageMapper;
import org.example.aispingboot.mapper.ConsultationSessionMapper;
import org.example.aispingboot.mapper.EmotionDiaryMapper;
import org.example.aispingboot.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 后台数据统计服务
 */
@Service
public class DataAnalyticsService {

    private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("MM-dd");

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmotionDiaryMapper emotionDiaryMapper;

    @Autowired
    private ConsultationSessionMapper consultationSessionMapper;

    @Autowired
    private ConsultationMessageMapper consultationMessageMapper;

    public Map<String, Object> overview() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();

        long totalUsers = userMapper.selectCount(null);
        long activeUsers = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getStatus, 1));
        long totalDiaries = emotionDiaryMapper.selectCount(null);
        long todayNewDiaries = emotionDiaryMapper.selectCount(new LambdaQueryWrapper<EmotionDiary>()
                .ge(EmotionDiary::getCreatedAt, todayStart)
                .lt(EmotionDiary::getCreatedAt, tomorrowStart));
        long totalSessions = consultationSessionMapper.selectCount(null);
        long todayNewSessions = consultationSessionMapper.selectCount(new LambdaQueryWrapper<ConsultationSession>()
                .ge(ConsultationSession::getStartedAt, todayStart)
                .lt(ConsultationSession::getStartedAt, tomorrowStart));

        List<EmotionDiary> recentDiaries = emotionDiaryMapper.selectList(
                new LambdaQueryWrapper<EmotionDiary>().ge(EmotionDiary::getDiaryDate, startDate));
        List<ConsultationSession> recentSessions = consultationSessionMapper.selectList(
                new LambdaQueryWrapper<ConsultationSession>().ge(ConsultationSession::getStartedAt, startTime));
        List<User> recentUsers = userMapper.selectList(
                new LambdaQueryWrapper<User>().ge(User::getCreatedAt, startTime));

        Map<Long, LocalDateTime> lastMessageTimes = loadLastMessageTimes(recentSessions);

        Map<String, Object> systemOverview = new LinkedHashMap<>();
        systemOverview.put("totalUsers", totalUsers);
        systemOverview.put("activeUsers", activeUsers);
        systemOverview.put("totalDiaries", totalDiaries);
        systemOverview.put("todayNewDiaries", todayNewDiaries);
        systemOverview.put("totalSessions", totalSessions);
        systemOverview.put("todayNewSessions", todayNewSessions);
        systemOverview.put("avgMoodScore", overallAvgMood());

        List<Map<String, Object>> emotionTrend = new ArrayList<>();
        List<Map<String, Object>> sessionTrend = new ArrayList<>();
        List<Map<String, Object>> userActivity = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate day = startDate.plusDays(i);

            List<EmotionDiary> dayDiaries = recentDiaries.stream()
                    .filter(diary -> day.equals(diary.getDiaryDate()))
                    .toList();
            int moodSum = 0;
            int moodCount = 0;
            Set<Long> diaryUserIds = new HashSet<>();
            for (EmotionDiary diary : dayDiaries) {
                if (diary.getMoodScore() != null) {
                    moodSum += diary.getMoodScore();
                    moodCount++;
                }
                if (diary.getUserId() != null) {
                    diaryUserIds.add(diary.getUserId());
                }
            }

            Map<String, Object> moodPoint = new LinkedHashMap<>();
            moodPoint.put("date", day.format(DAY_LABEL));
            moodPoint.put("avgMoodScore", moodCount == 0 ? 0 : round1((double) moodSum / moodCount));
            moodPoint.put("recordCount", dayDiaries.size());
            emotionTrend.add(moodPoint);

            List<ConsultationSession> daySessions = recentSessions.stream()
                    .filter(session -> session.getStartedAt() != null && day.equals(session.getStartedAt().toLocalDate()))
                    .toList();
            Set<Long> sessionUserIds = new HashSet<>();
            for (ConsultationSession session : daySessions) {
                if (session.getUserId() != null) {
                    sessionUserIds.add(session.getUserId());
                }
            }

            Map<String, Object> sessionPoint = new LinkedHashMap<>();
            sessionPoint.put("date", day.format(DAY_LABEL));
            sessionPoint.put("sessionCount", daySessions.size());
            sessionPoint.put("userCount", sessionUserIds.size());
            sessionTrend.add(sessionPoint);

            long newUsers = recentUsers.stream()
                    .filter(user -> user.getCreatedAt() != null && day.equals(user.getCreatedAt().toLocalDate()))
                    .count();
            Set<Long> activeUserIds = new HashSet<>(diaryUserIds);
            activeUserIds.addAll(sessionUserIds);

            Map<String, Object> activityPoint = new LinkedHashMap<>();
            activityPoint.put("date", day.format(DAY_LABEL));
            activityPoint.put("activeUsers", activeUserIds.size());
            activityPoint.put("newUsers", newUsers);
            activityPoint.put("diaryUsers", diaryUserIds.size());
            activityPoint.put("consultationUsers", sessionUserIds.size());
            userActivity.add(activityPoint);
        }

        Map<String, Object> consultationStats = new LinkedHashMap<>();
        consultationStats.put("totalSessions", totalSessions);
        consultationStats.put("avgDurationMinutes", averageDurationMinutes(recentSessions, lastMessageTimes));
        consultationStats.put("dailyTrend", sessionTrend);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("systemOverview", systemOverview);
        data.put("emotionTrend", emotionTrend);
        data.put("consultationStats", consultationStats);
        data.put("userActivity", userActivity);
        return data;
    }

    private Double overallAvgMood() {
        List<Map<String, Object>> rows = emotionDiaryMapper.selectMaps(
                new QueryWrapper<EmotionDiary>().select("IFNULL(AVG(mood_score), 0) AS avg_mood"));
        if (rows == null || rows.isEmpty()) {
            return 0.0;
        }
        Object value = rows.get(0).get("avg_mood");
        if (value instanceof Number number) {
            return round1(number.doubleValue());
        }
        return 0.0;
    }

    private Map<Long, LocalDateTime> loadLastMessageTimes(List<ConsultationSession> sessions) {
        Map<Long, LocalDateTime> lastMessageTimes = new HashMap<>();
        List<Long> sessionIds = sessions.stream().map(ConsultationSession::getId).toList();
        if (sessionIds.isEmpty()) {
            return lastMessageTimes;
        }
        List<ConsultationMessage> messages = consultationMessageMapper.selectList(
                new LambdaQueryWrapper<ConsultationMessage>()
                        .in(ConsultationMessage::getSessionId, sessionIds)
                        .orderByAsc(ConsultationMessage::getCreatedAt));
        for (ConsultationMessage message : messages) {
            if (message.getCreatedAt() != null) {
                lastMessageTimes.put(message.getSessionId(), message.getCreatedAt());
            }
        }
        return lastMessageTimes;
    }

    private double averageDurationMinutes(List<ConsultationSession> sessions,
                                          Map<Long, LocalDateTime> lastMessageTimes) {
        long totalSeconds = 0;
        int counted = 0;
        for (ConsultationSession session : sessions) {
            LocalDateTime lastMessageTime = lastMessageTimes.get(session.getId());
            if (session.getStartedAt() == null || lastMessageTime == null) {
                continue;
            }
            totalSeconds += Math.max(0L, Duration.between(session.getStartedAt(), lastMessageTime).getSeconds());
            counted++;
        }
        return counted == 0 ? 0 : round1(totalSeconds / 60.0);
    }

    private double round1(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
