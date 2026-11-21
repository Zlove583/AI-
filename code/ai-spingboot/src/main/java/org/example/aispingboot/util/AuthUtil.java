package org.example.aispingboot.util;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.example.aispingboot.exception.BusinessException;
import org.springframework.util.StringUtils;

/**
 * 当前登录用户工具类
 */
public final class AuthUtil {

    private AuthUtil() {
    }

    public static Long currentUserId() {
        String token = JwtTokenUtil.getCurrentToken();
        if (!StringUtils.hasText(token)) {
            throw new BusinessException("用户未登录");
        }
        DecodedJWT jwt = JwtTokenUtil.verifyToken(token);
        return jwt.getClaim("userId").asLong();
    }

    public static boolean isAdmin() {
        String token = JwtTokenUtil.getCurrentToken();
        if (!StringUtils.hasText(token)) {
            return false;
        }
        try {
            Integer roleType = JwtTokenUtil.verifyToken(token).getClaim("roleType").asInt();
            return Integer.valueOf(2).equals(roleType);
        } catch (Exception e) {
            return false;
        }
    }

    public static void requireAdmin() {
        if (!isAdmin()) {
            throw new BusinessException("无权访问该资源");
        }
    }
}
