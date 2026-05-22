package com.server.service;

import com.common.entity.Role;
import com.common.entity.User;
import com.server.dao.PassengerDao;
import com.server.dao.RoleDao;
import com.server.dao.UserDao;
import com.server.utils.HashUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Transaction transaction;

    @Mock
    private UserDao userDao;

    @Mock
    private RoleDao roleDao;

    @Mock
    private PassengerDao passengerDao;

    private AuthService authService;

    private User testUser;
    private Role clientRole;

    @BeforeEach
    void setUp() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.beginTransaction()).thenReturn(transaction);

        authService = new AuthService(sessionFactory, userDao, roleDao, passengerDao);

        clientRole = new Role("CLIENT");
        clientRole.setId(1);

        testUser = new User("testuser", HashUtil.hashPassword("password123"), clientRole);
        testUser.setId(1);
        testUser.setBlocked(false);
    }

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should successfully register new user")
        void register_shouldSucceed_whenUserDoesNotExist() {
            // Given
            when(userDao.findByLogin(any(Session.class), eq("newuser"))).thenReturn(null);
            when(roleDao.findByName(any(Session.class), eq("CLIENT"))).thenReturn(clientRole);
            doNothing().when(userDao).save(any(Session.class), any(User.class));
            doNothing().when(passengerDao).save(any(Session.class), any(com.common.entity.Passenger.class));

            // When
            String result = authService.register("newuser", "pass123", "John", "Doe", "AB1234567");

            // Then
            assertThat(result).startsWith("Успех");
            verify(userDao, times(1)).save(any(Session.class), any(User.class));
        }

        @Test
        @DisplayName("Should fail when user already exists")
        void register_shouldFail_whenUserExists() {
            // Given
            when(userDao.findByLogin(any(Session.class), eq("existinguser"))).thenReturn(testUser);

            // When
            String result = authService.register("existinguser", "pass123", "John", "Doe", "AB1234567");

            // Then
            assertThat(result)
                    .contains("Ошибка")
                    .contains("уже существует");
            verify(userDao, never()).save(any(Session.class), any(User.class));
        }

        @Test
        @DisplayName("Should fail when role CLIENT not found")
        void register_shouldFail_whenRoleNotFound() {
            // Given
            when(userDao.findByLogin(any(Session.class), eq("newuser"))).thenReturn(null);
            when(roleDao.findByName(any(Session.class), eq("CLIENT"))).thenReturn(null);

            // When
            String result = authService.register("newuser", "pass123", "John", "Doe", "AB1234567");

            // Then
            assertThat(result)
                    .contains("Ошибка")
                    .contains("не найдена");
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should successfully login with correct credentials")
        void login_shouldSucceed_whenCredentialsAreCorrect() {
            // Given
            when(userDao.findByLogin(any(Session.class), eq("testuser"))).thenReturn(testUser);

            // When
            User result = authService.login("testuser", "password123");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getLogin()).isEqualTo("testuser");
            assertThat(result.isBlocked()).isFalse();
        }

        @Test
        @DisplayName("Should fail with wrong password")
        void login_shouldFail_whenPasswordIsWrong() {
            // Given
            when(userDao.findByLogin(any(Session.class), eq("testuser"))).thenReturn(testUser);

            // When
            User result = authService.login("testuser", "wrongpassword");

            // Then
            assertThat(result).isNull();
        }


        @Test
        @DisplayName("Should fail when user does not exist")
        void login_shouldFail_whenUserDoesNotExist() {
            // Given
            when(userDao.findByLogin(any(Session.class), eq("nonexistent"))).thenReturn(null);

            // When
            User result = authService.login("nonexistent", "password123");

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Update Password Tests")
    class UpdatePasswordTests {

        @Test
        @DisplayName("Should successfully update password")
        void updatePassword_shouldSucceed_whenUserExists() {
            // Given
            when(userDao.findById(any(Session.class), eq(1))).thenReturn(testUser);
            doNothing().when(userDao).update(any(Session.class), any(User.class));

            // When
            String result = authService.updatePassword(1, "newPassword456");

            // Then
            assertThat(result).startsWith("Успех");
            verify(userDao, times(1)).update(any(Session.class), any(User.class));
        }

        @Test
        @DisplayName("Should fail when user not found")
        void updatePassword_shouldFail_whenUserNotFound() {
            // Given
            when(userDao.findById(any(Session.class), eq(999))).thenReturn(null);

            // When
            String result = authService.updatePassword(999, "newPassword");

            // Then
            assertThat(result)
                    .contains("Ошибка")
                    .contains("не найден");
            verify(userDao, never()).update(any(Session.class), any(User.class));
        }
    }
}