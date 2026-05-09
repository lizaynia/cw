package com.server.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("HashUtil Tests")
class HashUtilTest {

    @Test
    @DisplayName("Should generate consistent hash for same input")
    void hashPassword_shouldGenerateConsistentHash() {
        // Given
        String password = "mySecret123";

        // When
        String hash1 = HashUtil.hashPassword(password);
        String hash2 = HashUtil.hashPassword(password);

        // Then
        assertThat(hash1)
                .isNotBlank()
                .hasSize(64) // SHA-256 produces 64 hex characters
                .isEqualTo(hash2);
    }

    @Test
    @DisplayName("Should generate different hashes for different passwords")
    void hashPassword_shouldGenerateDifferentHashes() {
        // Given
        String password1 = "password123";
        String password2 = "password456";

        // When
        String hash1 = HashUtil.hashPassword(password1);
        String hash2 = HashUtil.hashPassword(password2);

        // Then
        assertThat(hash1).isNotEqualTo(hash2);
    }

    @ParameterizedTest
    @ValueSource(strings = {"admin", "123456", "pass@word", ""})
    @DisplayName("Should handle various password formats")
    void hashPassword_shouldHandleVariousPasswords(String password) {
        // When
        String hash = HashUtil.hashPassword(password);

        // Then
        assertThat(hash)
                .isNotBlank()
                .hasSize(64)
                .matches("^[a-f0-9]{64}$"); // Only hex characters
    }
}