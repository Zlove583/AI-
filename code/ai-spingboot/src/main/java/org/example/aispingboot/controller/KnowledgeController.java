package org.example.aispingboot.controller;

import jakarta.validation.Valid;
import org.example.aispingboot.DTO.command.ArticleSaveDTO;
import org.example.aispingboot.DTO.response.KnowledgeArticleResponseDTO;
import org.example.aispingboot.common.Result;
import org.example.aispingboot.service.KnowledgeArticleService;
import org.example.aispingboot.service.KnowledgeCategoryService;
import org.example.aispingboot.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 心理健康知识库接口
 */
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    @Autowired
    private KnowledgeCategoryService knowledgeCategoryService;

    @Autowired
    private KnowledgeArticleService knowledgeArticleService;

    /**
     * 查询分类树
     */
    @GetMapping("/category/tree")
    public Result<List<Map<String, Object>>> categoryTree() {
        return Result.ok(knowledgeCategoryService.tree());
    }

    /**
     * 分页查询文章
     */
    @GetMapping("/article/page")
    public Result<Map<String, Object>> articlePage(
            @RequestParam(value = "currentPage", required = false) Long currentPage,
            @RequestParam(value = "size", required = false) Long size,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "categoryId", required = false) String categoryId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "sortField", required = false) String sortField,
            @RequestParam(value = "sortDirection", required = false) String sortDirection) {
        long current = currentPage == null || currentPage < 1 ? 1L : currentPage;
        long pageSize = size == null || size < 1 ? 10L : size;
        return Result.ok(knowledgeArticleService.pageArticles(current, pageSize, title, categoryId,
                status, sortField, sortDirection, AuthUtil.isAdmin()));
    }

    /**
     * 查询文章详情
     */
    @GetMapping("/article/{id}")
    public Result<KnowledgeArticleResponseDTO> articleDetail(@PathVariable String id) {
        return Result.ok(knowledgeArticleService.detail(id, AuthUtil.isAdmin()));
    }

    /**
     * 新增文章
     */
    @PostMapping("/article")
    public Result<String> createArticle(@Valid @RequestBody ArticleSaveDTO dto) {
        AuthUtil.requireAdmin();
        return Result.ok(knowledgeArticleService.create(dto, AuthUtil.currentUserId()));
    }

    /**
     * 编辑文章
     */
    @PutMapping("/article/{id}")
    public Result<Void> updateArticle(@PathVariable String id, @Valid @RequestBody ArticleSaveDTO dto) {
        AuthUtil.requireAdmin();
        knowledgeArticleService.update(id, dto);
        return Result.ok();
    }

    /**
     * 修改文章状态
     */
    @PutMapping("/article/{id}/status")
    public Result<Void> changeArticleStatus(@PathVariable String id, @RequestBody Map<String, Object> body) {
        AuthUtil.requireAdmin();
        Object statusValue = body.get("status");
        Integer status = null;
        if (statusValue instanceof Number number) {
            status = number.intValue();
        } else if (statusValue != null) {
            status = Integer.valueOf(statusValue.toString());
        }
        knowledgeArticleService.changeStatus(id, status);
        return Result.ok();
    }

    /**
     * 删除文章
     */
    @DeleteMapping("/article/{id}")
    public Result<Void> deleteArticle(@PathVariable String id) {
        AuthUtil.requireAdmin();
        knowledgeArticleService.delete(id);
        return Result.ok();
    }
}
