package com.client.controller;

import com.client.ServerConnection;
import com.common.CommandType;
import com.common.Request;
import com.common.Response;
import com.common.dto.UserDto;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginController Tests")
class LoginControllerTest {

    @Mock
    private ServerConnection serverConnection;

    @InjectMocks
    private LoginController controller;

    @BeforeEach
    void setUp() {
        // Создаем реальный контроллер с mock-зависимостями
        controller = new LoginController();

        // Используем reflection для установки mock-полей
        try {
            java.lang.reflect.Field loginField = LoginController.class.getDeclaredField("loginField");
            loginField.setAccessible(true);
            javafx.scene.control.TextField mockLoginField = mock(javafx.scene.control.TextField.class);
            loginField.set(controller, mockLoginField);

            java.lang.reflect.Field passwordField = LoginController.class.getDeclaredField("passwordField");
            passwordField.setAccessible(true);
            javafx.scene.control.PasswordField mockPasswordField = mock(javafx.scene.control.PasswordField.class);
            passwordField.set(controller, mockPasswordField);

            java.lang.reflect.Field errorLabel = LoginController.class.getDeclaredField("errorLabel");
            errorLabel.setAccessible(true);
            javafx.scene.control.Label mockErrorLabel = mock(javafx.scene.control.Label.class);
            errorLabel.set(controller, mockErrorLabel);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    @DisplayName("Login Validation Tests")
    class LoginValidationTests {

        @Test
        @DisplayName("Should show error when login is empty")
        void handleLogin_shouldShowError_whenLoginEmpty() throws Exception {
            // Given
            javafx.scene.control.TextField loginField = getLoginField();
            javafx.scene.control.PasswordField passwordField = getPasswordField();
            javafx.scene.control.Label errorLabel = getErrorLabel();

            when(loginField.getText()).thenReturn("");
            when(passwordField.getText()).thenReturn("password123");

            // When
            controller.doLogin();

            // Then
            verify(errorLabel).setText("Заполните все поля!");
            verify(serverConnection, never()).sendRequest(any(Request.class));
        }

        @Test
        @DisplayName("Should show error when password is empty")
        void handleLogin_shouldShowError_whenPasswordEmpty() throws Exception {
            // Given
            javafx.scene.control.TextField loginField = getLoginField();
            javafx.scene.control.PasswordField passwordField = getPasswordField();
            javafx.scene.control.Label errorLabel = getErrorLabel();

            when(loginField.getText()).thenReturn("validuser");
            when(passwordField.getText()).thenReturn("");

            // When
            controller.doLogin();

            // Then
            verify(errorLabel).setText("Заполните все поля!");
        }

        @Test
        @DisplayName("Should show error when login is too short")
        void handleLogin_shouldShowError_whenLoginTooShort() throws Exception {
            // Given
            javafx.scene.control.TextField loginField = getLoginField();
            javafx.scene.control.PasswordField passwordField = getPasswordField();
            javafx.scene.control.Label errorLabel = getErrorLabel();

            when(loginField.getText()).thenReturn("ab");
            when(passwordField.getText()).thenReturn("password123");

            // When
            controller.doLogin();

            // Then
            verify(errorLabel).setText("Логин должен быть от 3 до 20 символов");
        }

        @Test
        @DisplayName("Should show error when login is too long")
        void handleLogin_shouldShowError_whenLoginTooLong() throws Exception {
            // Given
            javafx.scene.control.TextField loginField = getLoginField();
            javafx.scene.control.PasswordField passwordField = getPasswordField();
            javafx.scene.control.Label errorLabel = getErrorLabel();

            when(loginField.getText()).thenReturn("a".repeat(21));
            when(passwordField.getText()).thenReturn("password123");

            // When
            controller.doLogin();

            // Then
            verify(errorLabel).setText("Логин должен быть от 3 до 20 символов");
        }
    }

    @Nested
    @DisplayName("Successful Login Tests")
    class SuccessfulLoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void handleLogin_shouldSucceed_whenCredentialsValid() throws Exception {
            // Given
            javafx.scene.control.TextField loginField = getLoginField();
            javafx.scene.control.PasswordField passwordField = getPasswordField();

            when(loginField.getText()).thenReturn("validuser");
            when(passwordField.getText()).thenReturn("password123");

            // Mock ServerConnection.getInstance()
            try (MockedStatic<ServerConnection> mockedStatic = mockStatic(ServerConnection.class)) {
                mockedStatic.when(ServerConnection::getInstance).thenReturn(serverConnection);

                UserDto userDto = new UserDto(1, "validuser", "CLIENT");
                Response mockResponse = new Response(true, "Success");
                mockResponse.setData(userDto);

                when(serverConnection.sendRequest(any(Request.class))).thenReturn(mockResponse);

                // When
                controller.doLogin();

                // Then
                verify(serverConnection).setCurrentUser(userDto);
            }
        }
    }

    // Helper methods to access private fields via reflection
    private javafx.scene.control.TextField getLoginField() throws Exception {
        java.lang.reflect.Field field = LoginController.class.getDeclaredField("loginField");
        field.setAccessible(true);
        return (javafx.scene.control.TextField) field.get(controller);
    }

    private javafx.scene.control.PasswordField getPasswordField() throws Exception {
        java.lang.reflect.Field field = LoginController.class.getDeclaredField("passwordField");
        field.setAccessible(true);
        return (javafx.scene.control.PasswordField) field.get(controller);
    }

    private javafx.scene.control.Label getErrorLabel() throws Exception {
        java.lang.reflect.Field field = LoginController.class.getDeclaredField("errorLabel");
        field.setAccessible(true);
        return (javafx.scene.control.Label) field.get(controller);
    }
}