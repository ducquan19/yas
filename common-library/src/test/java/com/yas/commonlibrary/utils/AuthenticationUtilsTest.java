package com.yas.commonlibrary.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.yas.commonlibrary.constants.ApiConstant;
import com.yas.commonlibrary.exception.AccessDeniedException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class AuthenticationUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void extractUserId_whenAnonymous_throwsAccessDenied() {
        AnonymousAuthenticationToken authentication = new AnonymousAuthenticationToken(
            "key",
            "anonymous",
            List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        AccessDeniedException thrown = assertThrows(AccessDeniedException.class, AuthenticationUtils::extractUserId);

        assertEquals(ApiConstant.ACCESS_DENIED, thrown.getMessage());
    }

    @Test
    void extractUserId_returnsSubjectFromJwt() {
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(buildJwt("token-1", "user-1"));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String userId = AuthenticationUtils.extractUserId();

        assertEquals("user-1", userId);
    }

    @Test
    void extractJwt_returnsTokenValue() {
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(buildJwt("token-2", "user-2"));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = AuthenticationUtils.extractJwt();

        assertEquals("token-2", token);
    }

    @Test
    void getAuthentication_returnsContextAuthentication() {
        Authentication authentication = new JwtAuthenticationToken(buildJwt("token-3", "user-3"));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Authentication current = AuthenticationUtils.getAuthentication();

        assertSame(authentication, current);
    }

    private Jwt buildJwt(String tokenValue, String subject) {
        return Jwt.withTokenValue(tokenValue)
            .header("alg", "none")
            .claim("sub", subject)
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(300))
            .build();
    }
}
