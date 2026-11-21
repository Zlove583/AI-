/*
 Navicat Premium Dump SQL

 Source Server         : mysql8.0
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3306
 Source Schema         : mental_health_assistant

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 15/11/2025 11:15:33
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_analysis_task
-- ----------------------------
DROP TABLE IF EXISTS `ai_analysis_task`;
CREATE TABLE `ai_analysis_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '浠诲姟ID',
  `diary_id` bigint NOT NULL COMMENT '鏃ヨID',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '浠诲姟鐘舵€侊細PENDING-寰呭鐞嗭紝PROCESSING-澶勭悊涓紝COMPLETED-宸插畬鎴愶紝FAILED-澶辫触',
  `task_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '浠诲姟绫诲瀷锛欰UTO-鑷姩瑙﹀彂锛孧ANUAL-鎵嬪姩瑙﹀彂锛孉DMIN-绠＄悊鍛樿Е鍙戯紝BATCH-鎵归噺瑙﹀彂',
  `priority` int NOT NULL DEFAULT 2 COMMENT '浼樺厛绾э細1-浣庯紝2-姝ｅ父锛?-楂橈紝4-绱ф€?,
  `retry_count` int NOT NULL DEFAULT 0 COMMENT '閲嶈瘯娆℃暟',
  `max_retry_count` int NOT NULL DEFAULT 3 COMMENT '鏈€澶ч噸璇曟鏁?,
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '閿欒淇℃伅',
  `started_at` datetime NULL DEFAULT NULL COMMENT '澶勭悊寮€濮嬫椂闂?,
  `completed_at` datetime NULL DEFAULT NULL COMMENT '澶勭悊瀹屾垚鏃堕棿',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_diary_id`(`diary_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_task_type`(`task_type` ASC) USING BTREE,
  INDEX `idx_priority`(`priority` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_status_priority`(`status` ASC, `priority` ASC) USING BTREE,
  INDEX `idx_status_created_at`(`status` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_task_type_created_at`(`task_type` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_retry_status`(`status` ASC, `retry_count` ASC, `max_retry_count` ASC) USING BTREE,
  CONSTRAINT `fk_ai_task_diary` FOREIGN KEY (`diary_id`) REFERENCES `emotion_diary` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_ai_task_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 46 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI鍒嗘瀽浠诲姟琛? ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_analysis_task
-- ----------------------------

-- ----------------------------
-- Table structure for consultation_message
-- ----------------------------
DROP TABLE IF EXISTS `consultation_message`;
CREATE TABLE `consultation_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '娑堟伅ID',
  `session_id` bigint NOT NULL COMMENT '浼氳瘽ID',
  `sender_type` tinyint NOT NULL COMMENT '鍙戦€佽€呯被鍨?1:鐢ㄦ埛 2:AI鍔╂墜',
  `message_type` tinyint NULL DEFAULT 1 COMMENT '娑堟伅绫诲瀷 1:鏂囨湰',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '娑堟伅鍐呭',
  `emotion_tag` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '鎯呯华鏍囩',
  `ai_model` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '浣跨敤鐨凙I妯″瀷',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_session_message`(`session_id` ASC, `created_at` ASC) USING BTREE,
  CONSTRAINT `consultation_message_ibfk_1` FOREIGN KEY (`session_id`) REFERENCES `consultation_session` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 111 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '鍜ㄨ娑堟伅琛? ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of consultation_message
-- ----------------------------

-- ----------------------------
-- Table structure for consultation_session
-- ----------------------------
DROP TABLE IF EXISTS `consultation_session`;
CREATE TABLE `consultation_session`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '浼氳瘽ID',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `session_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '浼氳瘽鏍囬',
  `started_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `last_emotion_analysis` json NULL COMMENT '鏈€鍚庝竴娆℃儏缁垎鏋愮粨鏋?JSON鏍煎紡)',
  `last_emotion_updated_at` datetime NULL DEFAULT NULL COMMENT '鏈€鍚庝竴娆℃儏缁垎鏋愭洿鏂版椂闂?,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_session`(`user_id` ASC, `started_at` ASC) USING BTREE,
  INDEX `idx_last_emotion_updated_at`(`last_emotion_updated_at` ASC) USING BTREE,
  CONSTRAINT `consultation_session_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '鍜ㄨ浼氳瘽琛? ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of consultation_session
-- ----------------------------

-- ----------------------------
-- Table structure for emotion_diary
-- ----------------------------
DROP TABLE IF EXISTS `emotion_diary`;
CREATE TABLE `emotion_diary`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鏃ヨID',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `diary_date` date NOT NULL COMMENT '鏃ヨ鏃ユ湡',
  `mood_score` tinyint NOT NULL COMMENT '鎯呯华璇勫垎(1-10)',
  `dominant_emotion` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '涓昏鎯呯华',
  `emotion_triggers` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '鎯呯华瑙﹀彂鍥犵礌',
  `diary_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '鏃ヨ鍐呭',
  `sleep_quality` tinyint NULL DEFAULT NULL COMMENT '鐫＄湢璐ㄩ噺(1-5)',
  `stress_level` tinyint NULL DEFAULT NULL COMMENT '鍘嬪姏姘村钩(1-5)',
  `ai_emotion_analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'AI鎯呯华鍒嗘瀽缁撴灉(JSON鏍煎紡)',
  `ai_analysis_updated_at` datetime NULL DEFAULT NULL COMMENT 'AI鍒嗘瀽鏇存柊鏃堕棿',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_date_unique`(`user_id` ASC, `diary_date` ASC) USING BTREE,
  INDEX `idx_user_diary`(`user_id` ASC, `diary_date` ASC) USING BTREE,
  INDEX `idx_ai_analysis_time`(`ai_analysis_updated_at` ASC) USING BTREE,
  CONSTRAINT `emotion_diary_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '鎯呯华鏃ヨ琛? ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of emotion_diary
-- ----------------------------

-- ----------------------------
-- Table structure for knowledge_article
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_article`;
CREATE TABLE `knowledge_article`  (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鏂囩珷ID(UUID)',
  `category_id` bigint NOT NULL COMMENT '鍒嗙被ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鏂囩珷鏍囬',
  `summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '鏂囩珷鎽樿',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鏂囩珷鍐呭',
  `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '灏侀潰鍥剧墖',
  `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '鏍囩',
  `author_id` bigint NULL DEFAULT NULL COMMENT '浣滆€匢D',
  `read_count` int NULL DEFAULT 0 COMMENT '闃呰娆℃暟',
  `status` tinyint NULL DEFAULT 1 COMMENT '鐘舵€?1:宸插彂甯?,
  `published_at` datetime NULL DEFAULT NULL COMMENT '鍙戝竷鏃堕棿',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `author_id`(`author_id` ASC) USING BTREE,
  INDEX `idx_category_article`(`category_id` ASC, `published_at` ASC) USING BTREE,
  INDEX `idx_title`(`title` ASC) USING BTREE,
  CONSTRAINT `knowledge_article_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `knowledge_category` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `knowledge_article_ibfk_2` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '鐭ヨ瘑鏂囩珷琛? ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of knowledge_article
-- ----------------------------

-- ----------------------------
-- Table structure for knowledge_category
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_category`;
CREATE TABLE `knowledge_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鍒嗙被ID',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '鐖跺垎绫籌D',
  `category_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鍒嗙被鍚嶇О',
  `category_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '鍒嗙被浠ｇ爜',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '鍒嗙被鎻忚堪',
  `sort_order` int NULL DEFAULT 0 COMMENT '鎺掑簭',
  `status` tinyint NULL DEFAULT 1 COMMENT '鐘舵€?0:绂佺敤 1:鍚敤',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `category_code`(`category_code` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '鐭ヨ瘑鏂囩珷鍒嗙被琛? ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of knowledge_category
-- ----------------------------

-- ----------------------------
-- Table structure for sys_file_info
-- ----------------------------
DROP TABLE IF EXISTS `sys_file_info`;
CREATE TABLE `sys_file_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鏂囦欢ID锛屼富閿嚜澧?,
  `original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鍘熷鏂囦欢鍚嶏紙鐢ㄦ埛涓婁紶鏃剁殑鏂囦欢鍚嶏級',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鏂囦欢璁块棶璺緞锛堟湇鍔″櫒瀛樺偍璺緞锛?,
  `file_size` bigint NOT NULL DEFAULT 0 COMMENT '鏂囦欢澶у皬锛屽崟浣嶏細瀛楄妭',
  `file_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鏂囦欢绫诲瀷锛圛MG/PDF/TXT/DOC/XLS绛夛級',
  `business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '涓氬姟绫诲瀷锛堢敤浜庡尯鍒嗘枃浠剁敤閫旓紝濡傦細avatar/document/attachment锛?,
  `business_id` char(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '涓氬姟瀵硅薄ID锛堝叧鑱旂殑涓氬姟鏁版嵁涓婚敭锛?,
  `business_field` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '涓氬姟瀛楁鍚嶏紙瀵瑰簲涓氬姟琛ㄤ腑鐨勫瓧娈靛悕锛?,
  `upload_user_id` bigint NULL DEFAULT NULL COMMENT '涓婁紶鐢ㄦ埛ID锛堣褰曡皝涓婁紶鐨勬枃浠讹級',
  `is_temp` tinyint(1) NOT NULL DEFAULT 0 COMMENT '鏄惁涓存椂鏂囦欢锛?:鍚?1:鏄級',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '鐘舵€侊紙0:鍒犻櫎 1:姝ｅ父锛?,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '杩囨湡鏃堕棿锛堜粎瀵逛复鏃舵枃浠舵湁鏁堬級',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_business`(`business_type` ASC, `business_id` ASC) USING BTREE,
  INDEX `idx_upload_user`(`upload_user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_temp_expire`(`is_temp` ASC, `expire_time` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 57 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '绯荤粺鏂囦欢淇℃伅琛? ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_file_info
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鐢ㄦ埛ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鐢ㄦ埛鍚?,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '閭',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '鎵嬫満鍙?,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '瀵嗙爜',
  `nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '鏄电О',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '澶村儚URL',
  `gender` tinyint NULL DEFAULT NULL COMMENT '鎬у埆 0:鏈煡 1:鐢?2:濂?,
  `birthday` date NULL DEFAULT NULL COMMENT '鐢熸棩',
  `user_type` tinyint NULL DEFAULT 1 COMMENT '鐢ㄦ埛绫诲瀷 1:鏅€氱敤鎴?2:绠＄悊鍛?,
  `status` tinyint NULL DEFAULT 1 COMMENT '鐘舵€?0:绂佺敤 1:姝ｅ父',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `email`(`email` ASC) USING BTREE,
  UNIQUE INDEX `phone`(`phone` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_user_type`(`user_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '鐢ㄦ埛琛? ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------

-- ----------------------------
-- Table structure for user_favorite
-- ----------------------------
DROP TABLE IF EXISTS `user_favorite`;
CREATE TABLE `user_favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鏀惰棌ID',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `article_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鏂囩珷ID',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鏀惰棌鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_article_unique`(`user_id` ASC, `article_id` ASC) USING BTREE,
  INDEX `article_id`(`article_id` ASC) USING BTREE,
  CONSTRAINT `user_favorite_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `user_favorite_ibfk_2` FOREIGN KEY (`article_id`) REFERENCES `knowledge_article` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '鐢ㄦ埛鏀惰棌琛? ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_favorite
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
