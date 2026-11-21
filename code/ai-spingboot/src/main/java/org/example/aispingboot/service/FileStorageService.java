package org.example.aispingboot.service;

import cn.hutool.core.util.StrUtil;
import org.example.aispingboot.entity.SysFileInfo;
import org.example.aispingboot.exception.BusinessException;
import org.example.aispingboot.mapper.SysFileInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 本地文件上传服务
 */
@Service
public class FileStorageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024L;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "webp", "bmp");

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Autowired
    private SysFileInfoMapper sysFileInfoMapper;

    public Map<String, Object> upload(MultipartFile file, String businessType, String businessId,
                                      String businessField, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小不能超过5MB");
        }

        String originalName = file.getOriginalFilename();
        String extension = StrUtil.isBlank(originalName) ? "" : StrUtil.subAfter(originalName, ".", true);
        extension = extension.toLowerCase(Locale.ROOT);

        String contentType = file.getContentType();
        if (StrUtil.isNotBlank(contentType) && !contentType.startsWith("image/")) {
            throw new BusinessException("仅支持上传图片文件");
        }
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("仅支持 png、jpg、jpeg、gif、webp、bmp 格式的图片");
        }

        String type = StrUtil.isBlank(businessType)
                ? "common"
                : businessType.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        if (StrUtil.isBlank(type)) {
            type = "common";
        }

        String fileName = System.currentTimeMillis() + "_"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8) + "." + extension;

        Path directory = Paths.get(uploadDir).toAbsolutePath().normalize()
                .resolve("business").resolve(type);
        Path target = directory.resolve(fileName);

        try {
            Files.createDirectories(directory);
            file.transferTo(target.toFile());
        } catch (IOException e) {
            throw new BusinessException("文件保存失败");
        }

        String filePath = "/files/business/" + type + "/" + fileName;
        SysFileInfo fileInfo = SysFileInfo.builder()
                .originalName(originalName)
                .filePath(filePath)
                .fileSize(file.getSize())
                .fileType("IMG")
                .businessType(businessType)
                .businessId(businessId)
                .businessField(businessField)
                .uploadUserId(userId)
                .isTemp(0)
                .status(1)
                .createTime(LocalDateTime.now())
                .build();
        sysFileInfoMapper.insert(fileInfo);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", fileInfo.getId());
        data.put("originalName", originalName);
        data.put("filePath", filePath);
        data.put("fileSize", file.getSize());
        data.put("fileType", "IMG");
        return data;
    }
}
