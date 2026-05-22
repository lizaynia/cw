//package com.server.dao;
//
//import com.common.entity.Role;
//import com.common.entity.User;
//import com.server.utils.HibernateUtil;
//import org.hibernate.Session;
//import org.hibernate.Transaction;
//import org.junit.jupiter.api.*;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DisplayName("UserDao Tests")
//class UserDaoTest {
//
//    private UserDao userDao;
//    private Session session;
//
//    @BeforeEach
//    void setUp() {
//        userDao = new UserDao();
//        session = HibernateUtil.getSessionFactory().openSession();
//
//        // Clean up before test
//        Transaction cleanTx = session.beginTransaction();
//        session.createMutationQuery("DELETE FROM User").executeUpdate();
//        session.createMutationQuery("DELETE FROM Role").executeUpdate();
//        cleanTx.commit();
//
//        // Create roles
//        Transaction tx = session.beginTransaction();
//        Role adminRole = new Role("ADMIN");
//        Role clientRole = new Role("CLIENT");
//        session.persist(adminRole);
//        session.persist(clientRole);
//        tx.commit();
//    }
//
//    @AfterEach
//    void tearDown() {
//        if (session != null && session.isOpen()) {
//            session.close();
//        }
//    }
//
//    @AfterAll
//    static void cleanup() {
//        HibernateUtil.shutdown();
//    }
//
//    @Test
//    @DisplayName("Should save and find user by id")
//    void testSaveAndFindById() {
//        Transaction tx = session.beginTransaction();
//
//        Role clientRole = session.createQuery("from Role where roleName = 'CLIENT'", Role.class).uniqueResult();
//        User user = new User("testuser", "hashedpass", clientRole);
//        userDao.save(session, user);
//        tx.commit();
//
//        User found = userDao.findById(session, user.getId());
//        assertThat(found).isNotNull();
//        assertThat(found.getLogin()).isEqualTo("testuser");
//    }
//
//    @Test
//    @DisplayName("Should find user by login")
//    void testFindByLogin() {
//        Transaction tx = session.beginTransaction();
//
//        Role clientRole = session.createQuery("from Role where roleName ='CLIENT'", Role.class).uniqueResult();
//        User user = new User("john_doe", "hashed123", clientRole);
//        userDao.save(session, user);
//        tx.commit();
//
//        User found = userDao.findByLogin(session, "john_doe");
//        assertThat(found).isNotNull();
//        assertThat(found.getLogin()).isEqualTo("john_doe");
//    }
//
//    @Test
//    @DisplayName("Should return null when login not found")
//    void testFindByLoginNotFound() {
//        User found = userDao.findByLogin(session, "nonexistent");
//        assertThat(found).isNull();
//    }
//
//    @Test
//    @DisplayName("Should update user")
//    void testUpdateUser() {
//        Transaction tx = session.beginTransaction();
//
//        Role clientRole = session.createQuery("from Role where roleName = 'CLIENT'", Role.class).uniqueResult();
//        User user = new User("update_test", "oldpass", clientRole);
//        userDao.save(session, user);
//        tx.commit();
//
//        user.setPassword("newpass");
//        Transaction tx2 = session.beginTransaction();
//        userDao.update(session, user);
//        tx2.commit();
//
//        User updated = userDao.findById(session, user.getId());
//        assertThat(updated.getPassword()).isEqualTo("newpass");
//    }
//
//    @Test
//    @DisplayName("Should delete user")
//    void testDeleteUser() {
//        Transaction tx = session.beginTransaction();
//
//        Role clientRole = session.createQuery("from Role where roleName = 'CLIENT'", Role.class).uniqueResult();
//        User user = new User("delete_test", "pass", clientRole);
//        userDao.save(session, user);
//        tx.commit();
//
//        Transaction tx2 = session.beginTransaction();
//        userDao.delete(session, user);
//        tx2.commit();
//
//        User deleted = userDao.findById(session, user.getId());
//        assertThat(deleted).isNull();
//    }
//
//    @Test
//    @DisplayName("Should find all users")
//    void testFindAllUsers() {
//        Transaction tx = session.beginTransaction();
//
//        Role clientRole = session.createQuery("from Role where roleName = 'CLIENT'", Role.class).uniqueResult();
//        userDao.save(session, new User("user1", "pass1", clientRole));
//        userDao.save(session, new User("user2", "pass2", clientRole));
//        tx.commit();
//
//        var users = userDao.findAll(session);
//        assertThat(users).hasSize(2);
//    }
//}