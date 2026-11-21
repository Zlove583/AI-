package org.example.aispingboot.DTO.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 知识文章新增/编辑参数
 */
@Data
public class ArticleSaveDTO {
    private String id;

    @NotBlank(message = "文章标题不能为空")
    @Size(max = 200, message = "文章标题长度不能超过200个字符")
    private String title;

    @NotNull(message = "文章分类不能为空")
    private Long categoryId;

    private String summary;

    @NotBlank(message = "文章内容不能为空")
    private String content;

    private String coverImage;

    private String tags;

    /** 0:草稿 1:已发布 2:已下线 */
    private Integer status;
}
