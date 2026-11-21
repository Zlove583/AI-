package org.example.aispingboot.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.aispingboot.DTO.command.EmotionDiaryCreateDTO;
import org.example.aispingboot.DTO.response.EmotionDiaryResponseDTO;
import org.example.aispingboot.entity.EmotionDiary;
import org.example.aispingboot.entity.User;
import org.example.aispingboot.exception.BusinessException;
import org.example.aispingboot.mapper.EmotionDiaryMapper;
import org.example.aispingboot.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 情绪日记服务
 */
@Service
public class EmotionDiaryService {

    @Autowired
    private EmotionDiaryMapper emotionDiaryMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 保存情绪日记，同一天重复提交时更新已有记录
     */
    @Transactional
    public EmotionDiaryResponseDTO save(Long userId, EmotionDiaryCreateDTO dto) {
        LocalDate diaryDate = dto.getDiaryDate() == null ? LocalDate.now() : dto.getDiaryDate();
        LocalDateTime now = LocalDateTime.now();

        LambdaQueryWrapper<EmotionDiary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EmotionDiary::getUserId, userId)
                .eq(EmotionDiary::getDiaryDate, diaryDate);
        EmotionDiary diary = emotionDiaryMapper.selectOne(queryWrapper);

        if (diary == null) {
            diary = EmotionDiary.builder()
                    .userId(userId)
                    .diaryDate(diaryDate)
                    .moodScore(dto.getMoodScore())
                    .dominantEmotion(dto.getDominantEmotion())
                    .emotionTriggers(dto.getEmotionTriggers())
                    .diaryContent(dto.getDiaryContent())
                    .sleepQuality(dto.getSleepQuality())
                    .stressLevel(dto.getStressLevel())
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            emotionDiaryMapper.insert(diary);
        } else {
            diary.setMoodScore(dto.getMoodScore());
            diary.setDominantEmotion(dto.getDominantEmotion());
            diary.setEmotionTriggers(dto.getEmotionTriggers());
            diary.setDiaryContent(dto.getDiaryContent());
            diary.setSleepQuality(dto.getSleepQuality());
            diary.setStressLevel(dto.getStressLevel());
            diary.setUpdatedAt(now);
            emotionDiaryMapper.updateById(diary);
        }

        return toResponseDTO(diary, userId == null ? null : userMapper.selectById(userId));
    }

    /**
     * 后台分页查询情绪日记
     */
    public Map<String, Object> pageDiaries(long current, long size, String userId, String moodScoreRange) {
        LambdaQueryWrapper<EmotionDiary> queryWrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(userId)) {
            try {
                queryWrapper.eq(EmotionDiary::getUserId, Long.valueOf(userId.trim()));
            } catch (NumberFormatException ignored) {
                // 非法用户ID忽略该筛选条件
            }
        }
        if (StrUtil.isNotBlank(moodScoreRange)) {
            String[] range = moodScoreRange.split("-");
            if (range.length == 2) {
                try {
                    queryWrapper.between(EmotionDiary::getMoodScore,
                            Integer.parseInt(range[0].trim()), Integer.parseInt(range[1].trim()));
                } catch (NumberFormatException ignored) {
                    // 非法评分区间忽略该筛选条件
                }
            }
        }
        queryWrapper.orderByDesc(EmotionDiary::getDiaryDate).orderByDesc(EmotionDiary::getId);

        Page<EmotionDiary> page = new Page<>(current, size);
        Page<EmotionDiary> result = emotionDiaryMapper.selectPage(page, queryWrapper);

        Set<Long> userIds = result.getRecords().stream()
                .map(EmotionDiary::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, User> users = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (User user : userMapper.selectBatchIds(userIds)) {
                users.put(user.getId(), user);
            }
        }

        List<EmotionDiaryResponseDTO> records = new ArrayList<>();
        for (EmotionDiary diary : result.getRecords()) {
            records.add(toResponseDTO(diary, users.get(diary.getUserId())));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", records);
        data.put("total", result.getTotal());
        data.put("current", result.getCurrent());
        data.put("size", result.getSize());
        return data;
    }

    /**
     * 删除情绪日记
     */
    @Transactional
    public void delete(Long id) {
        EmotionDiary diary = emotionDiaryMapper.selectById(id);
        if (diary == null) {
            throw new BusinessException("情绪日记不存在");
        }
        emotionDiaryMapper.deleteById(id);
    }

    private EmotionDiaryResponseDTO toResponseDTO(EmotionDiary diary, User user) {
        return EmotionDiaryResponseDTO.builder()
                .id(diary.getId())
                .userId(diary.getUserId())
                .username(user == null ? null : user.getUsername())
                .nickname(user == null ? null : user.getDisplayName())
                .diaryDate(diary.getDiaryDate())
                .moodScore(diary.getMoodScore())
                .dominantEmotion(diary.getDominantEmotion())
                .emotionTriggers(diary.getEmotionTriggers())
                .diaryContent(diary.getDiaryContent())
                .sleepQuality(diary.getSleepQuality())
                .stressLevel(diary.getStressLevel())
                .aiEmotionAnalysis(diary.getAiEmotionAnalysis())
                .aiAnalysisUpdatedAt(diary.getAiAnalysisUpdatedAt())
                .createdAt(diary.getCreatedAt())
                .updatedAt(diary.getUpdatedAt())
                .build();
    }
}
