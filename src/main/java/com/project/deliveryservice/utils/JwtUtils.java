package com.project.deliveryservice.utils;

import com.project.deliveryservice.common.constants.AuthConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public class JwtUtils {

    /**
     *
     * @param request HttpServletRequest
     * @return HTTP 요청 헤더에 포함된 Bearer 토큰 값
     */
    public static String resolveJwtToken(HttpServletRequest request) {
        String bearerToken = getTokenFromHeader(request);
        return JwtUtils.resolveJwtToken(bearerToken);
    }

    public static String resolveJwtToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(AuthConstants.BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private static String getTokenFromHeader(HttpServletRequest request) {
        return request.getHeader(AuthConstants.AUTHORIZATION_HEADER);
    }
}
