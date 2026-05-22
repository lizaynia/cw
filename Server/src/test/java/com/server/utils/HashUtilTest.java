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

    private static final int SHA256_HEX_LENGTH = 64;
    private static final String HEX_PATTERN = "^[a-f0-9]{" + SHA256_HEX_LENGTH + "}$";

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
                .hasSize(SHA256_HEX_LENGTH)
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

    @Test
    @DisplayName("Should generate same hash for same password across multiple calls")
    void hashPassword_shouldBeDeterministic() {
        // Given
        String password = "deterministicTest";

        // When
        String hash1 = HashUtil.hashPassword(password);
        String hash2 = HashUtil.hashPassword(password);
        String hash3 = HashUtil.hashPassword(password);

        // Then
        assertThat(hash1)
                .isEqualTo(hash2)
                .isEqualTo(hash3);
    }

    @ParameterizedTest
    @ValueSource(strings = {"admin", "123456", "pass@word", "veryLongPasswordWithSpecialChars!@#$%^&*()"})
    @DisplayName("Should handle various password formats")
    void hashPassword_shouldHandleVariousPasswords(String password) {
        // When
        String hash = HashUtil.hashPassword(password);

        // Then
        assertThat(hash)
                .isNotBlank()
                .hasSize(SHA256_HEX_LENGTH)
                .matches(HEX_PATTERN);
    }

    @Test
    @DisplayName("Should handle empty password")
    void hashPassword_shouldHandleEmptyPassword() {
        // When
        String hash = HashUtil.hashPassword("");

        // Then
        assertThat(hash)
                .isNotBlank()
                .hasSize(SHA256_HEX_LENGTH)
                .matches(HEX_PATTERN);
    }

    @Test
    @DisplayName("Should handle password with spaces")
    void hashPassword_shouldHandlePasswordWithSpaces() {
        // Given
        String password = "  password with spaces  ";

        // When
        String hash = HashUtil.hashPassword(password);

        // Then
        assertThat(hash)
                .isNotBlank()
                .hasSize(SHA256_HEX_LENGTH);
    }

    @Test
    @DisplayName("Should handle password with unicode characters")
    void hashPassword_shouldHandleUnicodeCharacters() {
        // Given
        String password = "пароль123密码😀";

        // When
        String hash = HashUtil.hashPassword(password);

        // Then
        assertThat(hash)
                .isNotBlank()
                .hasSize(SHA256_HEX_LENGTH);
    }

    @Test
    @DisplayName("Should handle very long password")
    void hashPassword_shouldHandleVeryLongPassword() {
        // Given
        String password = "a".repeat(1000);

        // When
        String hash = HashUtil.hashPassword(password);

        // Then
        assertThat(hash)
                .isNotBlank()
                .hasSize(SHA256_HEX_LENGTH);
    }

    @Test
    @DisplayName("Should not return the original password")
    void hashPassword_shouldNotReturnOriginalPassword() {
        // Given
        String password = "mySecretPassword";

        // When
        String hash = HashUtil.hashPassword(password);

        // Then
        assertThat(hash).isNotEqualTo(password);
    }

    @Test
    @DisplayName("Should not contain special characters - only hex")
    void hashPassword_shouldOnlyContainHexCharacters() {
        // Given
        String password = "test123";

        // When
        String hash = HashUtil.hashPassword(password);

        // Then
        assertThat(hash).matches(HEX_PATTERN);
        assertThat(hash).doesNotContainAnyWhitespaces();
        assertThat(hash).doesNotContain("g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z");
    }

    @Test
    @DisplayName("Should produce different hashes for similar passwords")
    void hashPassword_shouldBeSensitiveToSmallChanges() {
        // Given
        String password1 = "password";
        String password2 = "Password";  // Capital P
        String password3 = "passworc";   // Last character changed

        // When
        String hash1 = HashUtil.hashPassword(password1);
        String hash2 = HashUtil.hashPassword(password2);
        String hash3 = HashUtil.hashPassword(password3);

        // Then
        assertThat(hash1).isNotEqualTo(hash2);
        assertThat(hash1).isNotEqualTo(hash3);
        assertThat(hash2).isNotEqualTo(hash3);
    }
}