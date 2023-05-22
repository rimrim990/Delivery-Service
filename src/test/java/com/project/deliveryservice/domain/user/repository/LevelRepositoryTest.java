package com.project.deliveryservice.domain.user.repository;

import com.project.deliveryservice.domain.user.entity.Level;
import com.project.deliveryservice.domain.user.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LevelRepositoryTest {

    @Autowired
    private LevelRepository levelRepository;

    @Test
    @DisplayName("NORMAL role 으로 조회 시에 1 건의 데이터를 반환한다")
    void test_01() {

        // when
        Optional<Level> normal = levelRepository.findByRole(Role.ROLE_NORMAL);

        // then
        assertNotEquals(normal, Optional.empty());
        assertThat(normal.get().getRole(), equalTo(Role.ROLE_NORMAL));
        assertThat(normal.get().getAuthority(), equalTo(Role.ROLE_NORMAL.toString()));
    }
}