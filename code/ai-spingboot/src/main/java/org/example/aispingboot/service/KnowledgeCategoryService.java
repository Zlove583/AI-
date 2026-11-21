package org.example.aispingboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.aispingboot.entity.KnowledgeCategory;
import org.example.aispingboot.mapper.KnowledgeCategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识分类服务
 */
@Service
public class KnowledgeCategoryService {

    @Autowired
    private KnowledgeCategoryMapper knowledgeCategoryMapper;

    /**
     * 查询启用状态的分类树
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> tree() {
        List<KnowledgeCategory> categories = knowledgeCategoryMapper.selectList(
                new LambdaQueryWrapper<KnowledgeCategory>()
                        .eq(KnowledgeCategory::getStatus, 1)
                        .orderByAsc(KnowledgeCategory::getSortOrder)
                        .orderByAsc(KnowledgeCategory::getId));

        Map<Long, Map<String, Object>> nodes = new LinkedHashMap<>();
        for (KnowledgeCategory category : categories) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", category.getId());
            node.put("parentId", category.getParentId());
            node.put("categoryName", category.getCategoryName());
            node.put("description", category.getDescription());
            node.put("sortOrder", category.getSortOrder());
            node.put("children", new ArrayList<Map<String, Object>>());
            nodes.put(category.getId(), node);
        }

        List<Map<String, Object>> roots = new ArrayList<>();
        for (KnowledgeCategory category : categories) {
            Map<String, Object> node = nodes.get(category.getId());
            Long parentId = category.getParentId();
            if (parentId != null && parentId != 0 && nodes.containsKey(parentId)) {
                ((List<Map<String, Object>>) nodes.get(parentId).get("children")).add(node);
            } else {
                roots.add(node);
            }
        }
        return roots;
    }
}
