package com.server.integration;

import com.common.entity.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Database Integration Tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatabaseIntegrationTest {

    private SessionFactory sessionFactory;
    private Session session;

    @BeforeAll
    void setupDatabase() {
        // Программная конфигурация Hibernate (без XML файла)
        Configuration configuration = new Configuration();

        // H2 database settings
        configuration.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        configuration.setProperty("hibernate.connection.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL");
        configuration.setProperty("hibernate.connection.username", "sa");
        configuration.setProperty("hibernate.connection.password", "");

        // Hibernate settings
        configuration.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        configuration.setProperty("hibernate.show_sql", "false");
        configuration.setProperty("hibernate.format_sql", "false");

        // Add annotated classes
        configuration.addAnnotatedClass(City.class);
        configuration.addAnnotatedClass(Role.class);
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(Airplane.class);
        configuration.addAnnotatedClass(Flight.class);
        configuration.addAnnotatedClass(Passenger.class);
        configuration.addAnnotatedClass(Ticket.class);

        // Build session factory
        sessionFactory = configuration.buildSessionFactory();
    }

    @BeforeEach
    void setUp() {
        session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        // Create roles
        Role adminRole = new Role("ADMIN");
        Role dispatcherRole = new Role("DISPATCHER");
        Role clientRole = new Role("CLIENT");
        session.persist(adminRole);
        session.persist(dispatcherRole);
        session.persist(clientRole);

        tx.commit();
    }

    @AfterEach
    void tearDown() {
        if (session != null && session.isOpen()) {
            // Clean up all data
            Transaction tx = session.beginTransaction();
            session.createMutationQuery("DELETE FROM Ticket").executeUpdate();
            session.createMutationQuery("DELETE FROM Flight").executeUpdate();
            session.createMutationQuery("DELETE FROM Passenger").executeUpdate();
            session.createMutationQuery("DELETE FROM User").executeUpdate();
            session.createMutationQuery("DELETE FROM Airplane").executeUpdate();
            session.createMutationQuery("DELETE FROM City").executeUpdate();
            session.createMutationQuery("DELETE FROM Role").executeUpdate();
            tx.commit();
            session.close();
        }
    }

    @AfterAll
    void cleanup() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Nested
    @DisplayName("User CRUD Operations")
    class UserCrudTests {

        @Test
        @DisplayName("Should save and retrieve user successfully")
        void testUserCrud() {
            Transaction tx = session.beginTransaction();

            // Create
            Role clientRole = session.createQuery("from Role where roleName = 'CLIENT'", Role.class).uniqueResult();
            User user = new User("testuser", "hashedpassword", clientRole);
            user.setBlocked(false);
            session.persist(user);
            tx.commit();

            // Retrieve
            User retrieved = session.get(User.class, user.getId());
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getLogin()).isEqualTo("testuser");
            assertThat(retrieved.getRole().getRoleName()).isEqualTo("CLIENT");
            assertThat(retrieved.isBlocked()).isFalse();

            // Update
            Transaction tx2 = session.beginTransaction();
            retrieved.setBlocked(true);
            session.merge(retrieved);
            tx2.commit();

            User updated = session.get(User.class, user.getId());
            assertThat(updated.isBlocked()).isTrue();

            // Delete
            Transaction tx3 = session.beginTransaction();
            session.remove(updated);
            tx3.commit();

            User deleted = session.get(User.class, user.getId());
            assertThat(deleted).isNull();
        }

        @Test
        @DisplayName("Should find user by login")
        void testFindUserByLogin() {
            Transaction tx = session.beginTransaction();

            Role clientRole = session.createQuery("from Role where roleName = 'CLIENT'", Role.class).uniqueResult();
            User user = new User("john_doe", "hashed123", clientRole);
            session.persist(user);
            tx.commit();

            // Query by login
            User found = session.createQuery("from User where login = :login", User.class)
                    .setParameter("login", "john_doe")
                    .uniqueResult();

            assertThat(found).isNotNull();
            assertThat(found.getLogin()).isEqualTo("john_doe");
        }
    }

    @Nested
    @DisplayName("Flight CRUD Operations")
    class FlightCrudTests {

        private City minsk;
        private City moscow;
        private Airplane airplane;

        @BeforeEach
        void setupFlightData() {
            Transaction tx = session.beginTransaction();

            // Create cities
            minsk = new City("Minsk");
            moscow = new City("Moscow");
            session.persist(minsk);
            session.persist(moscow);

            // Create airplane
            airplane = new Airplane();
            airplane.setModel("Boeing 737");
            airplane.setCapacity(180);
            airplane.setStatus(Airplane.AirplaneStatus.ACTIVE);
            session.persist(airplane);

            tx.commit();
        }

        @Test
        @DisplayName("Should save and retrieve flight correctly")
        void testFlightCrud() {
            Transaction tx = session.beginTransaction();

            // Create flight
            Flight flight = new Flight();
            flight.setFlightNumber("SU1234");
            flight.setDepartureCity(minsk);
            flight.setArrivalCity(moscow);
            flight.setDepartureTime(LocalDateTime.now().plusDays(1));
            flight.setAirplane(airplane);
            flight.setBasePrice(BigDecimal.valueOf(199.99));
            session.persist(flight);

            tx.commit();

            // Retrieve flight
            Flight retrieved = session.get(Flight.class, flight.getId());
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getFlightNumber()).isEqualTo("SU1234");
            assertThat(retrieved.getDepartureCity().getCityName()).isEqualTo("Minsk");
            assertThat(retrieved.getArrivalCity().getCityName()).isEqualTo("Moscow");
            assertThat(retrieved.getBasePrice()).isEqualByComparingTo(BigDecimal.valueOf(199.99));
            assertThat(retrieved.getAirplane().getModel()).isEqualTo("Boeing 737");
        }

        @Test
        @DisplayName("Should find flights by cities")
        void testFindFlightsByCities() {
            Transaction tx = session.beginTransaction();

            Flight flight1 = new Flight();
            flight1.setFlightNumber("SU1234");
            flight1.setDepartureCity(minsk);
            flight1.setArrivalCity(moscow);
            flight1.setDepartureTime(LocalDateTime.now().plusDays(1));
            flight1.setAirplane(airplane);
            flight1.setBasePrice(BigDecimal.valueOf(150.00));
            session.persist(flight1);

            Flight flight2 = new Flight();
            flight2.setFlightNumber("SU5678");
            flight2.setDepartureCity(minsk);
            flight2.setArrivalCity(moscow);
            flight2.setDepartureTime(LocalDateTime.now().plusDays(2));
            flight2.setAirplane(airplane);
            flight2.setBasePrice(BigDecimal.valueOf(200.00));
            session.persist(flight2);

            tx.commit();

            // Search flights from Minsk to Moscow
            List<Flight> foundFlights = session.createQuery(
                            "from Flight f where f.departureCity.cityName = :departure and f.arrivalCity.cityName = :arrival",
                            Flight.class)
                    .setParameter("departure", "Minsk")
                    .setParameter("arrival", "Moscow")
                    .list();

            assertThat(foundFlights).hasSize(2);
            assertThat(foundFlights).extracting(Flight::getFlightNumber)
                    .containsExactlyInAnyOrder("SU1234", "SU5678");
        }
    }

    @Nested
    @DisplayName("Ticket CRUD Operations")
    class TicketCrudTests {

        private Flight testFlight;
        private Passenger testPassenger;
        private User testUser;

        @BeforeEach
        void setupTicketData() {
            Transaction tx = session.beginTransaction();

            // Create role
            Role clientRole = session.createQuery("from Role where roleName = 'CLIENT'", Role.class).uniqueResult();

            // Create user
            testUser = new User("ticket_user", "hashedpass", clientRole);
            session.persist(testUser);

            // Create passenger
            testPassenger = new Passenger();
            testPassenger.setFirstName("John");
            testPassenger.setLastName("Doe");
            testPassenger.setPassportNumber("AB987654");
            testPassenger.setUser(testUser);
            session.persist(testPassenger);

            // Create city and airplane
            City minsk = new City("Minsk");
            City kiev = new City("Kiev");
            session.persist(minsk);
            session.persist(kiev);

            Airplane airplane = new Airplane();
            airplane.setModel("Airbus A320");
            airplane.setCapacity(150);
            airplane.setStatus(Airplane.AirplaneStatus.ACTIVE);
            session.persist(airplane);

            // Create flight
            testFlight = new Flight();
            testFlight.setFlightNumber("PS123");
            testFlight.setDepartureCity(minsk);
            testFlight.setArrivalCity(kiev);
            testFlight.setDepartureTime(LocalDateTime.now().plusDays(3));
            testFlight.setAirplane(airplane);
            testFlight.setBasePrice(BigDecimal.valueOf(120.00));
            session.persist(testFlight);

            tx.commit();
        }

        @Test
        @DisplayName("Should save and retrieve ticket correctly")
        void testTicketCrud() {
            Transaction tx = session.beginTransaction();

            // Create ticket
            Ticket ticket = new Ticket();
            ticket.setFlight(testFlight);
            ticket.setPassenger(testPassenger);
            ticket.setSeatNumber("A15");
            ticket.setPrice(testFlight.getBasePrice());
            ticket.setStatus(Ticket.TicketStatus.PAID);
            ticket.setBookingTime(LocalDateTime.now());
            session.persist(ticket);

            tx.commit();

            // Retrieve ticket
            Ticket retrieved = session.get(Ticket.class, ticket.getId());
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getSeatNumber()).isEqualTo("A15");
            assertThat(retrieved.getStatus()).isEqualTo(Ticket.TicketStatus.PAID);
            assertThat(retrieved.getFlight().getFlightNumber()).isEqualTo("PS123");
            assertThat(retrieved.getPassenger().getFirstName()).isEqualTo("John");
        }
    }

    @Nested
    @DisplayName("Relationships Tests")
    class RelationshipsTests {

        @Test
        @DisplayName("Should maintain proper relationships between entities")
        void testEntityRelationships() {
            Transaction tx = session.beginTransaction();

            // Create complete hierarchy
            Role adminRole = session.createQuery("from Role where roleName = 'ADMIN'", Role.class).uniqueResult();

            User admin = new User("admin_user", "hashed", adminRole);
            session.persist(admin);

            Passenger passenger = new Passenger();
            passenger.setFirstName("Admin");
            passenger.setLastName("User");
            passenger.setPassportNumber("ADMIN001");
            passenger.setUser(admin);
            session.persist(passenger);

            City london = new City("London");
            City paris = new City("Paris");
            session.persist(london);
            session.persist(paris);

            Airplane plane = new Airplane();
            plane.setModel("Boeing 787");
            plane.setCapacity(300);
            plane.setStatus(Airplane.AirplaneStatus.ACTIVE);
            session.persist(plane);

            Flight flight = new Flight();
            flight.setFlightNumber("BA123");
            flight.setDepartureCity(london);
            flight.setArrivalCity(paris);
            flight.setDepartureTime(LocalDateTime.now().plusDays(1));
            flight.setAirplane(plane);
            flight.setBasePrice(BigDecimal.valueOf(250.00));
            session.persist(flight);

            Ticket ticket = new Ticket();
            ticket.setFlight(flight);
            ticket.setPassenger(passenger);
            ticket.setSeatNumber("D1");
            ticket.setPrice(flight.getBasePrice());
            ticket.setStatus(Ticket.TicketStatus.PAID);
            session.persist(ticket);

            tx.commit();

            // Verify relationships
            Ticket retrieved = session.get(Ticket.class, ticket.getId());
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getFlight().getFlightNumber()).isEqualTo("BA123");
            assertThat(retrieved.getPassenger().getFirstName()).isEqualTo("Admin");
            assertThat(retrieved.getFlight().getAirplane().getModel()).isEqualTo("Boeing 787");
            assertThat(retrieved.getFlight().getDepartureCity().getCityName()).isEqualTo("London");
            assertThat(retrieved.getFlight().getArrivalCity().getCityName()).isEqualTo("Paris");
        }
    }
}