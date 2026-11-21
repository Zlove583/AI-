package org.example.aispingboot.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.aispingboot.DTO.command.ArticleSaveDTO;
import org.example.aispingboot.DTO.response.KnowledgeArticleResponseDTO;
import org.example.aispingboot.entity.KnowledgeArticle;
import org.example.aispingboot.entity.KnowledgeCategory;
import org.example.aispingboot.entity.User;
import org.example.aispingboot.exception.BusinessException;
import org.example.aispingboot.mapper.KnowledgeArticleMapper;
import org.example.aispingboot.mapper.KnowledgeCategoryMapper;
import org.example.aispingboot.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 知识文章服务
 */
@Service
public class KnowledgeArticleService {

    @Autowired
    private KnowledgeArticleMapper knowledgeArticleMapper;

    @Autowired
    private KnowledgeCategoryMapper knowledgeCategoryMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 分页查询文章，普通用户只能看到已发布文章
     */
    public Map<String, Object> pageArticles(long current, long size, String title, String categoryId,
                                            String status, String sortField, String sortDirection, boolean admin) {
        LambdaQueryWrapper<KnowledgeArticle> queryWrapper = new LambdaQueryWrapper<>();
        if (!admin) {
            queryWrapper.eq(KnowledgeArticle::getStatus, 1);
        } else if (StrUtil.isNotBlank(status)) {
            try {
                queryWrapper.eq(KnowledgeArticle::getStatus, Integer.valueOf(status.trim()));
            } catch (NumberFormatException ignored) {
                // 非法状态值忽略该筛选条件
            }
        }
        if (StrUtil.isNotBlank(title)) {
            queryWrapper.like(KnowledgeArticle::getTitle, title.trim());
        }
        if (StrUtil.isNotBlank(categoryId)) {
            try {
                queryWrapper.eq(KnowledgeArticle::getCategoryId, Long.valueOf(categoryId.trim()));
            } catch (NumberFormatException ignored) {
                // 非法分类值忽略该筛选条件
            }
        }

        boolean asc = "asc".equalsIgnoreCase(sortDirection);
        if ("publishedAt".equals(sortField)) {
            queryWrapper.orderBy(true, asc, KnowledgeArticle::getPublishedAt);
        } else if ("readCount".equals(sortField)) {
            queryWrapper.orderBy(true, asc, KnowledgeArticle::getReadCount);
        } else if ("createdAt".equals(sortField)) {
            queryWrapper.orderBy(true, asc, KnowledgeArticle::getCreatedAt);
        } else {
            queryWrapper.orderByDesc(KnowledgeArticle::getUpdatedAt);
        }

        Page<KnowledgeArticle> page = new Page<>(current, size);
        Page<KnowledgeArticle> result = knowledgeArticleMapper.selectPage(page, queryWrapper);

        Map<Long, String> categoryNames = loadCategoryNames();
        Map<Long, String> authorNames = loadAuthorNames(result.getRecords());

        List<KnowledgeArticleResponseDTO> records = new ArrayList<>();
        for (KnowledgeArticle article : result.getRecords()) {
            records.add(toResponseDTO(article, categoryNames, authorNames));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", records);
        data.put("total", result.getTotal());
        data.put("current", result.getCurrent());
        data.put("size", result.getSize());
        return data;
    }

    /**
     * 查询文章详情，普通用户查看时累加阅读量
     */
    public KnowledgeArticleResponseDTO detail(String id, boolean admin) {
        KnowledgeArticle article = knowledgeArticleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        if (!admin) {
            if (!Integer.valueOf(1).equals(article.getStatus())) {
                throw new BusinessException("文章尚未发布");
            }
            knowledgeArticleMapper.update(null, new LambdaUpdateWrapper<KnowledgeArticle>()
                    .eq(KnowledgeArticle::getId, id)
                    .setSql("read_count = IFNULL(read_count, 0) + 1"));
            article.setReadCount((article.getReadCount() == null ? 0 : article.getReadCount()) + 1);
        }
        return toResponseDTO(article, loadCategoryNames(), loadAuthorNames(List.of(article)));
    }

    /**
     * 新增文章
     */
    @Transactional
    public String create(ArticleSaveDTO dto, Long authorId) {
        requireCategory(dto.getCategoryId());
        LocalDateTime now = LocalDateTime.now();
        int status = dto.getStatus() == null ? 0 : dto.getStatus();

        KnowledgeArticle article = KnowledgeArticle.builder()
                .id(StrUtil.isNotBlank(dto.getId()) ? dto.getId() : UUID.randomUUID().toString())
                .categoryId(dto.getCategoryId())
                .title(dto.getTitle().trim())
                .summary(dto.getSummary())
                .content(dto.getContent())
                .coverImage(dto.getCoverImage())
                .tags(dto.getTags())
                .authorId(authorId)
                .readCount(0)
                .status(status)
                .publishedAt(status == 1 ? now : null)
                .createdAt(now)
                .updatedAt(now)
                .build();

        knowledgeArticleMapper.insert(article);
        return article.getId();
    }

    /**
     * 编辑文章
     */
    @Transactional
    public void update(String id, ArticleSaveDTO dto) {
        KnowledgeArticle article = knowledgeArticleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        requireCategory(dto.getCategoryId());

        LocalDateTime now = LocalDateTime.now();
        int status = dto.getStatus() == null ? article.getStatus() : dto.getStatus();

        article.setCategoryId(dto.getCategoryId());
        article.setTitle(dto.getTitle().trim());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setCoverImage(dto.getCoverImage());
        article.setTags(dto.getTags());
        article.setStatus(status);
        if (status == 1 && article.getPublishedAt() == null) {
            article.setPublishedAt(now);
        }
        article.setUpdatedAt(now);
        knowledgeArticleMapper.updateById(article);
    }

    /**
     * 修改文章状态 0:草稿 1:已发布 2:已下线
     */
    @Transactional
    public void changeStatus(String id, Integer status) {
        if (status == null || status < 0 || status > 2) {
            throw new BusinessException("文章状态不合法");
        }
        KnowledgeArticle article = knowledgeArticleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        if (status == 1 && article.getPublishedAt() == null) {
            article.setPublishedAt(now);
        }
        article.setStatus(status);
        article.setUpdatedAt(now);
        knowledgeArticleMapper.updateById(article);
    }

    /**
     * 删除文章
     */
    @Transactional
    public void delete(String id) {
        KnowledgeArticle article = knowledgeArticleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        knowledgeArticleMapper.deleteById(id);
    }

    private void requireCategory(Long categoryId) {
        KnowledgeCategory category = knowledgeCategoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException("文章分类不存在");
        }
    }

    private Map<Long, String> loadCategoryNames() {
        Map<Long, String> categoryNames = new HashMap<>();
        for (KnowledgeCategory category : knowledgeCategoryMapper.selectList(null)) {
            categoryNames.put(category.getId(), category.getCategoryName());
        }
        return categoryNames;
    }

    private Map<Long, String> loadAuthorNames(List<KnowledgeArticle> articles) {
        Set<Long> authorIds = articles.stream()
                .map(KnowledgeArticle::getAuthorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> authorNames = new HashMap<>();
        if (!authorIds.isEmpty()) {
            for (User user : userMapper.selectBatchIds(authorIds)) {
                authorNames.put(user.getId(), user.getDisplayName());
            }
        }
        return authorNames;
    }

    private KnowledgeArticleResponseDTO toResponseDTO(KnowledgeArticle article,
                                                      Map<Long, String> categoryNames,
                                                      Map<Long, String> authorNames) {
        List<String> tagArray = new ArrayList<>();
        if (StrUtil.isNotBlank(article.getTags())) {
            tagArray = Arrays.stream(article.getTags().split(","))
                    .map(String::trim)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toList());
        }

        return KnowledgeArticleResponseDTO.builder()
                .id(article.getId())
                .categoryId(article.getCategoryId())
                .categoryName(categoryNames.get(article.getCategoryId()))
                .title(article.getTitle())
                .summary(article.getSummary())
                .content(article.getContent())
                .coverImage(article.getCoverImage())
                .tags(article.getTags())
                .tagArray(tagArray)
                .authorId(article.getAuthorId())
                .authorName(authorNames.get(article.getAuthorId()))
                .readCount(article.getReadCount())
                .status(article.getStatus())
                .publishedAt(article.getPublishedAt())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }
}
