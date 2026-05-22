package com.server.service;

import com.common.entity.Airplane;
import com.common.entity.Role;
import com.common.entity.User;
import com.server.dao.AirplaneDao;
import com.server.dao.RoleDao;
import com.server.dao.UserDao;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService Tests")
class AdminServiceTest {

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
    private AirplaneDao airplaneDao;

    private AdminService adminService;

    private User testUser;
    private Role adminRole;
    private Role clientRole;

    @BeforeEach
    void setUp() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.beginTransaction()).thenReturn(transaction);

        adminService = new AdminService(sessionFactory, userDao, roleDao, airplaneDao);

        adminRole = new Role("ADMIN");
        adminRole.setId(1);

        clientRole = new Role("CLIENT");
        clientRole.setId(2);

        testUser = new User("testuser", "hashedpass", clientRole);
        testUser.setId(1);
        testUser.setBlocked(false);
    }



    @Nested
    @DisplayName("Change User Role Tests")
    class ChangeUserRoleTests {

        @Test
        @DisplayName("Should successfully change user role")
        void changeUserRole_shouldSucceed_whenUserAndRoleExist() {
            // Given
            when(userDao.findById(any(Session.class), eq(1))).thenReturn(testUser);
            when(roleDao.findByName(any(Session.class), eq("ADMIN"))).thenReturn(adminRole);
            doNothing().when(userDao).update(any(Session.class), any(User.class));

            // When
            String result = adminService.changeUserRole(1, "ADMIN");

            // Then
            assertThat(result).startsWith("Успех");
            verify(userDao, times(1)).update(any(Session.class), any(User.class));
        }

        @Test
        @DisplayName("Should fail when user not found")
        void changeUserRole_shouldFail_whenUserNotFound() {
            // Given
            when(userDao.findById(any(Session.class), eq(999))).thenReturn(null);

            // When
            String result = adminService.changeUserRole(999, "ADMIN");

            // Then
            assertThat(result)
                    .contains("Ошибка")
                    .contains("Пользователь не найден");
            verify(userDao, never()).update(any(Session.class), any(User.class));
        }

        @Test
        @DisplayName("Should fail when role not found")
        void changeUserRole_shouldFail_whenRoleNotFound() {
            // Given
            when(userDao.findById(any(Session.class), eq(1))).thenReturn(testUser);
            when(roleDao.findByName(any(Session.class), eq("NONEXISTENT"))).thenReturn(null);

            // When
            String result = adminService.changeUserRole(1, "NONEXISTENT");

            // Then
            assertThat(result)
                    .contains("Ошибка")
                    .contains("Роль не найдена");
        }
    }

    @Nested
    @DisplayName("Toggle User Block Tests")
    class ToggleUserBlockTests {

        @Test
        @DisplayName("Should successfully block user")
        void toggleUserBlock_shouldBlockUser() {
            // Given
            when(userDao.findById(any(Session.class), eq(1))).thenReturn(testUser);
            doNothing().when(userDao).update(any(Session.class), any(User.class));

            // When
            String result = adminService.toggleUserBlock(1, true);

            // Then
            assertThat(result).startsWith("Успех");
            assertThat(testUser.isBlocked()).isTrue();
        }

        @Test
        @DisplayName("Should successfully unblock user")
        void toggleUserBlock_shouldUnblockUser() {
            // Given
            testUser.setBlocked(true);
            when(userDao.findById(any(Session.class), eq(1))).thenReturn(testUser);
            doNothing().when(userDao).update(any(Session.class), any(User.class));

            // When
            String result = adminService.toggleUserBlock(1, false);

            // Then
            assertThat(result).startsWith("Успех");
            assertThat(testUser.isBlocked()).isFalse();
        }

        @Test
        @DisplayName("Should fail when user not found")
        void toggleUserBlock_shouldFail_whenUserNotFound() {
            // Given
            when(userDao.findById(any(Session.class), eq(999))).thenReturn(null);

            // When
            String result = adminService.toggleUserBlock(999, true);

            // Then
            assertThat(result)
                    .contains("Ошибка")
                    .contains("Пользователь не найден");
        }
    }

    @Nested
    @DisplayName("Update Airplane Status Tests")
    class UpdateAirplaneStatusTests {

        private Airplane testAirplane;

        @BeforeEach
        void setUp() {
            testAirplane = new Airplane();
            testAirplane.setId(1);
            testAirplane.setModel("Boeing 737");
            testAirplane.setCapacity(180);
            testAirplane.setStatus(Airplane.AirplaneStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should successfully update airplane status")
        void updateAirplaneStatus_shouldSucceed() {
            // Given
            when(airplaneDao.findById(any(Session.class), eq(1))).thenReturn(testAirplane);
            doNothing().when(airplaneDao).update(any(Session.class), any(Airplane.class));

            // When
            String result = adminService.updateAirplaneStatus(1, Airplane.AirplaneStatus.MAINTENANCE);

            // Then
            assertThat(result).startsWith("Успех");
            assertThat(testAirplane.getStatus()).isEqualTo(Airplane.AirplaneStatus.MAINTENANCE);
        }

        @Test
        @DisplayName("Should fail when airplane not found")
        void updateAirplaneStatus_shouldFail_whenAirplaneNotFound() {
            // Given
            when(airplaneDao.findById(any(Session.class), eq(999))).thenReturn(null);

            // When
            String result = adminService.updateAirplaneStatus(999, Airplane.AirplaneStatus.MAINTENANCE);

            // Then
            assertThat(result)
                    .contains("Ошибка")
                    .contains("Самолет не найден");
        }
    }

}