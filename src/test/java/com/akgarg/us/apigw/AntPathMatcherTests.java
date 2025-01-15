package com.akgarg.us.apigw;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.AntPathMatcher;

class AntPathMatcherTests {

    @Test
    void testSamePathMatch() {
        final var matcher = new AntPathMatcher();
        Assertions.assertTrue(matcher.match("/api/v1/subscriptions/packs", "/api/v1/subscriptions/packs"));
    }

}
