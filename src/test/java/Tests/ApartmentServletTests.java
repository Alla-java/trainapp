package Tests;

import com.example.trainapp.servlet.ApartmentServlet;
import org.junit.jupiter.api.BeforeEach;
import com.example.trainapp.model.Apartment;
import com.example.trainapp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

public class ApartmentServletTests {
    private ApartmentServlet apartmentServlet;

    @BeforeEach
    public void setUp() {
        apartmentServlet = new ApartmentServlet();
    }

    @Test //Проверка на успешное добавление квартиры
    public void testAddApartment() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("street", "lenina Street");
        request.setParameter("house", "101");
        request.setParameter("building", "1A");
        request.setParameter("number", "55");

        MockHttpServletResponse response = new MockHttpServletResponse();

        apartmentServlet.doPost(request, response);

        assertEquals(200, response.getStatus());
        assertTrue(response.getContentAsString().contains("Apartment saved"));
    }

    @Test //Проверка на неуспешное добавление квартиры (отсутствует street)
    public void testAddApartment_MissingStreet() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("house", "10");
        request.setParameter("building", "1A");
        request.setParameter("number", "101");

        MockHttpServletResponse response = new MockHttpServletResponse();

        apartmentServlet.doPost(request, response);

        assertEquals(400, response.getStatus());
        assertTrue(response.getErrorMessage().contains("Missing apartment parameters"));
    }

    @Test //Проверка на неуспешное добавление квартиры (отстутвует number)
    public void testAddApartment_MissingNumber() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("street", "Lenina");
        request.setParameter("house", "10");

        MockHttpServletResponse response = new MockHttpServletResponse();

        apartmentServlet.doPost(request, response);

        assertEquals(400, response.getStatus());
    }

    @Test // Проверка на поиск квартиры
    public void testGetApartment() throws Exception {
        Apartment apt = new Apartment("Lenina","1", "", "2");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.save(apt);
            tx.commit();
        }

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("id", String.valueOf(apt.getId()));

        MockHttpServletResponse response = new MockHttpServletResponse();

        apartmentServlet.doGet(request, response);

        assertEquals(200, response.getStatus());
        String content = response.getContentAsString();
        assertTrue(content.contains("Apartment Details"));
        assertTrue(content.contains("Lenina"));
    }

    @Test //Проверка на изменение квартиры
    public void testUpdateApartment() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Apartment apt = new Apartment("Change St", "1", "", "2");
            session.save(apt);
            apt.setStreet("Changed Street");
            apt.setNumber("22");
            session.update(apt);
            tx.commit();

            Apartment updated = session.get(Apartment.class, apt.getId());
            assertEquals("Changed Street", updated.getStreet());
            assertEquals("22", updated.getNumber());
        }
    }
}
