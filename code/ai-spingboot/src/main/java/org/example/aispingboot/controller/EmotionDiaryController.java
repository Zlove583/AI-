package org.example.aispingboot.controller;

import jakarta.validation.Valid;
import org.example.aispingboot.DTO.command.EmotionDiaryCreateDTO;
import org.example.aispingboot.DTO.response.EmotionDiaryResponseDTO;
import org.example.aispingboot.common.Result;
import org.example.aispingboot.service.EmotionDiaryService;
import org.example.aispingboot.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 情绪日记接口
 */
@RestController
@RequestMapping("/api/emotion-diary")
public class EmotionDiaryController {

    @Autowired
    private EmotionDiaryService emotionDiaryService;

    /**
     * 提交情绪日记，同一天重复提交时更新
     */
    @PostMapping
    public Result<EmotionDiaryResponseDTO> saveDiary(@Valid @RequestBody EmotionDiaryCreateDTO dto) {
        return Result.ok(emotionDiaryService.save(AuthUtil.currentUserId(), dto));
    }

    /**
     * 后台分页查询情绪日记
     */
    @GetMapping("/admin/page")
    public Result<Map<String, Object>> pageDiaries(
            @RequestParam(value = "current", required = false) Long current,
            @RequestParam(value = "size", required = false) Long size,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "moodScreRange", required = false) String moodScreRange) {
        AuthUtil.requireAdmin();
        long currentPage = current == null || current < 1 ? 1L : current;
        long pageSize = size == null || size < 1 ? 10L : size;
        return Result.ok(emotionDiaryService.pageDiaries(currentPage, pageSize, userId, moodScreRange));
    }

    /**
     * 后台删除情绪日记
     */
    @DeleteMapping("/admin/{id}")
    public Result<Void> deleteDiary(@PathVariable Long id) {
        AuthUtil.requireAdmin();
        emotionDiaryService.delete(id);
        return Result.ok();
    }
}
