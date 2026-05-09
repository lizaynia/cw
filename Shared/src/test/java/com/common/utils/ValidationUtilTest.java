package com.common.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ValidationUtil Tests")
class ValidationUtilTest {

    @Nested
    @DisplayName("isNotEmpty Tests")
    class IsNotEmptyTests {

        @Test
        @DisplayName("Should return true when all fields are non-empty")
        void isNotEmpty_shouldReturnTrue_whenAllFieldsPresent() {
            String field1 = "value1";
            String field2 = "value2";
            String field3 = "value3";

            boolean result = ValidationUtil.isNotEmpty(field1, field2, field3);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when any field is null")
        void isNotEmpty_shouldReturnFalse_whenAnyFieldIsNull() {
            String field1 = "value1";
            String field2 = null;
            String field3 = "value3";

            boolean result = ValidationUtil.isNotEmpty(field1, field2, field3);

            assertThat(result).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n", "  \t  "})
        @DisplayName("Should return false when any field is blank")
        void isNotEmpty_shouldReturnFalse_whenAnyFieldIsBlank(String blankValue) {
            String field1 = "value1";

            boolean result = ValidationUtil.isNotEmpty(field1, blankValue);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when field has leading/trailing spaces")
        void isNotEmpty_shouldReturnFalse_whenFieldHasOnlySpaces() {
            boolean result = ValidationUtil.isNotEmpty("   ");
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("isValidLogin Tests")
    class IsValidLoginTests {

        @ParameterizedTest
        @ValueSource(strings = {"abc", "user123", "valid_login", "User123", "john_doe_2024", "admin", "test-user", "user_123", "USER"})
        @DisplayName("Should return true for valid logins")
        void isValidLogin_shouldReturnTrue_whenLoginValid(String login) {
            boolean result = ValidationUtil.isValidLogin(login);
            assertThat(result).as("Login '%s' should be valid", login).isTrue();
        }

        @Test
        @DisplayName("Should return true for login with exactly 20 characters (max length)")
        void isValidLogin_shouldReturnTrue_whenLoginExactlyMaxLength() {
            String login = "a".repeat(20);
            boolean result = ValidationUtil.isValidLogin(login);
            assertThat(result).isTrue();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {
                "ab",           // too short
                "a",            // too short
                "  ",           // spaces only
                "\t",           // tab only
                "user@#$",      // special characters
                "user name",    // space inside
                "user%name",    // percent sign
                "user&name",    // ampersand
                "user*name",    // asterisk
                "user=name",    // equals
                "+user",        // plus at start
                "user+",        // plus at end
                "user\nname",   // newline
                "user\tname"    // tab inside
        })
        @DisplayName("Should return false for invalid logins")
        void isValidLogin_shouldReturnFalse_whenLoginInvalid(String login) {
            boolean result = ValidationUtil.isValidLogin(login);
            assertThat(result).as("Login '%s' should be invalid", login).isFalse();
        }

        @Test
        @DisplayName("Should return false when login is too long (21 chars)")
        void isValidLogin_shouldReturnFalse_whenLoginTooLong() {
            String login = "a".repeat(21);
            boolean result = ValidationUtil.isValidLogin(login);
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return true when login with underscore")
        void isValidLogin_shouldReturnTrue_whenLoginWithUnderscore() {
            boolean result = ValidationUtil.isValidLogin("user_name_123");
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return true when login with dash")
        void isValidLogin_shouldReturnTrue_whenLoginWithDash() {
            boolean result = ValidationUtil.isValidLogin("user-name-123");
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should trim login before validation")
        void isValidLogin_shouldTrimInput() {
            boolean result = ValidationUtil.isValidLogin("  admin  ");
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("isValidPassword Tests")
    class IsValidPasswordTests {

        @ParameterizedTest
        @ValueSource(strings = {"1234", "pass", "password123", "longpassword", "1234567890", "P@ssw0rd", "my secret phrase", "  pass  ", "\tpassword\t"})
        @DisplayName("Should return true for valid passwords")
        void isValidPassword_shouldReturnTrue_whenPasswordValid(String password) {
            boolean result = ValidationUtil.isValidPassword(password);
            assertThat(result).as("Password '%s' should be valid", password).isTrue();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"123", "ab", "a", "   ", "\t\n"})
        @DisplayName("Should return false for invalid passwords")
        void isValidPassword_shouldReturnFalse_whenPasswordInvalid(String password) {
            boolean result = ValidationUtil.isValidPassword(password);
            assertThat(result).as("Password '%s' should be invalid", password).isFalse();
        }

        @Test
        @DisplayName("Should return true for password with spaces inside")
        void isValidPassword_shouldReturnTrue_whenPasswordHasSpaces() {
            boolean result = ValidationUtil.isValidPassword("my pass word");
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should trim password before validation")
        void isValidPassword_shouldTrimInput() {
            boolean result = ValidationUtil.isValidPassword("  pass123  ");
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for password that is only spaces after trim")
        void isValidPassword_shouldReturnFalse_whenOnlySpaces() {
            boolean result = ValidationUtil.isValidPassword("     ");
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("isValidPassport Tests")
    class IsValidPassportTests {

        private static final String VALID_PASSPORT_7_CHARS = "AB12345";
        private static final String VALID_PASSPORT_15_CHARS = "AAAAAAAAAAAAAAA";

        @ParameterizedTest
        @ValueSource(strings = {"AB12345", "1234567", "AB1234567", "ABCDEFG123", "XX123456789", "A1B2C3D4E5", "MP1234567"})
        @DisplayName("Should return true for valid passport numbers")
        void isValidPassport_shouldReturnTrue_whenPassportValid(String passport) {
            boolean result = ValidationUtil.isValidPassport(passport);
            assertThat(result).as("Passport '%s' should be valid", passport).isTrue();
        }

        @Test
        @DisplayName("Should return true for passport with exactly 7 chars (min length)")
        void isValidPassport_shouldReturnTrue_whenPassportMinLength() {
            boolean result = ValidationUtil.isValidPassport(VALID_PASSPORT_7_CHARS);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return true for passport with exactly 15 chars (max length)")
        void isValidPassport_shouldReturnTrue_whenPassportMaxLength() {
            boolean result = ValidationUtil.isValidPassport(VALID_PASSPORT_15_CHARS);
            assertThat(result).isTrue();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {
                "AB123",                    // too short (5 chars)
                "123",                      // too short (3 chars)
                "AB123456789012345",        // too long (16 chars)
                "AB 12345",                 // contains space
                "AB@12345",                 // contains special char
                "AB-12345",                 // contains dash
                "AB_12345",                 // contains underscore
                "аб12345",                  // cyrillic letters
                "   ",                      // spaces only
                "\t\n\t",                   // whitespace only
                "AB.12345",                 // dot
                "AB,12345"                  // comma
        })
        @DisplayName("Should return false for invalid passport numbers")
        void isValidPassport_shouldReturnFalse_whenPassportInvalid(String passport) {
            boolean result = ValidationUtil.isValidPassport(passport);
            assertThat(result).as("Passport '%s' should be invalid", passport).isFalse();
        }

        @Test
        @DisplayName("Should trim passport before validation")
        void isValidPassport_shouldTrimInput() {
            boolean result = ValidationUtil.isValidPassport("  AB12345  ");
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when passport is too short (6 chars)")
        void isValidPassport_shouldReturnFalse_whenPassportTooShort() {
            String passport = "AB1234";
            boolean result = ValidationUtil.isValidPassport(passport);
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty array")
        void isNotEmpty_shouldReturnTrue_whenNoArguments() {
            boolean result = ValidationUtil.isNotEmpty();
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should handle single field")
        void isNotEmpty_shouldHandleSingleField() {
            boolean resultWithValue = ValidationUtil.isNotEmpty("value");
            boolean resultWithEmpty = ValidationUtil.isNotEmpty("");

            assertThat(resultWithValue).isTrue();
            assertThat(resultWithEmpty).isFalse();
        }

        @ParameterizedTest
        @CsvSource({
                "admin, ADMIN123, true",
                "test, test123, true",
                "user, pass, true",
                "ab, password, false",
                "valid_user, 123, false"
        })
        @DisplayName("Should validate multiple combinations")
        void shouldValidateMultipleCombinations(String login, String password, boolean expected) {
            boolean loginValid = ValidationUtil.isValidLogin(login);
            boolean passwordValid = ValidationUtil.isValidPassword(password);

            assertThat(loginValid && passwordValid).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should handle mixed valid and invalid fields")
        void isNotEmpty_shouldReturnFalse_whenMixedValidAndInvalid() {
            boolean result = ValidationUtil.isNotEmpty("valid", "", "also valid");
            assertThat(result).isFalse();
        }
    }
}