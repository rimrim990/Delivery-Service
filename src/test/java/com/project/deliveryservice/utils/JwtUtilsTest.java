package com.project.deliveryservice.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.Key;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class JwtUtilsTest {

    private final String secret = "asadlkfjlkdjasdkfkladjfkasdfsadfldajaskdjfklsjafkjewjfeiojafksdjfakdfjlksafljsaadsfsdafas";
    private final Key secretKey = JwtUtils.generateKey(secret);

    @Test
    @DisplayName("동일한 비밀키가 주어지면 항상 동등한 Key 인스턴스를 생성한다.")
    void test_01() {
        Key sameKey = JwtUtils.generateKey(secret);

        assertThat(secretKey, equalTo(sameKey));
    }
}