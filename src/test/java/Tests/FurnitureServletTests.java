package Tests;

import com.example.trainapp.model.Apartment;
import com.example.trainapp.model.FurnitureItem;
import com.example.trainapp.model.FurnitureType;
import com.example.trainapp.servlet.FurnitureServlet;
import com.example.trainapp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

public class FurnitureServletTests {

    private FurnitureServlet furnitureServlet;

    @BeforeEach
    public void setUp() {
        furnitureServlet = new FurnitureServlet();
    }

    @Test //Проверка на успешное добавление дом. имущества
    public void testAddFurniture() throws Exception {
        // Создаем квартиру
        Apartment apt = new Apartment();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.save(apt);
            tx.commit();
        }

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("street", apt.getStreet());
        request.setParameter("house", apt.getHouse());
        request.setParameter("building", apt.getBuilding());
        request.setParameter("number", apt.getNumber());
        request.setParameter("typeName", "Chair");
        request.setParameter("unitPrice", "300.0");
        request.setParameter("quantity", "4");

        MockHttpServletResponse response = new MockHttpServletResponse();

        furnitureServlet.doPost(request, response);

        assertEquals(200, response.getStatus());
        assertTrue(response.getContentAsString().contains("Furniture item saved"));
    }

    @Test //Проверка добавление дом. имущества с некорректной ценой
    public void testAddFurnitureItem_InvalidPriceFormat() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("street", "Test");
        request.setParameter("house", "1");
        request.setParameter("number", "2");
        request.setParameter("typeName", "Table");
        request.setParameter("unitPrice", "abc"); // Ошибка!
        request.setParameter("quantity", "3");

        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThrows(NumberFormatException.class, () -> {
            furnitureServlet.doPost(request, response);
        });
    }

    @Test //Проверка добавления дом. имущества с отриц. количеством
    public void testAddFurnitureItem_NegativeQuantity() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("street", "Test");
        request.setParameter("house", "1");
        request.setParameter("number", "2");
        request.setParameter("typeName", "Sofa");
        request.setParameter("unitPrice", "200");
        request.setParameter("quantity", "-2");

        MockHttpServletResponse response = new MockHttpServletResponse();

        furnitureServlet.doPost(request, response);

        assertEquals(200, response.getStatus()); // но это спорный момент — желательно валидировать
        assertTrue(response.getContentAsString().contains("Furniture item saved successfully."));
    }

    @Test //Проверка на изменение домашнего имущества
    public void testUpdateFurniture() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Apartment apt = new Apartment("Update St", "5", "", "1");
            session.save(apt);

            FurnitureType type = new FurnitureType("Table");
            session.save(type);

            FurnitureItem item = new FurnitureItem();
            item.setApartment(apt);
            item.setFurnitureType(type);
            item.setQuantity(1);
            item.setUnitPrice(1000);
            session.save(item);

            item.setQuantity(3);
            item.setUnitPrice(900);
            session.update(item);
            tx.commit();

            FurnitureItem updated = session.get(FurnitureItem.class, item.getId());
            assertEquals(3, updated.getQuantity());
            assertEquals(900, updated.getUnitPrice());
        }
    }

    @Test // Проверка на получение дом. имущества по адресу квартиры
    public void testGetFurnitureByApartmentAddress() throws Exception {
        Apartment apt = new Apartment("Find St", "12", null, "4");
        FurnitureType type = new FurnitureType("Sofa");
        FurnitureItem item = new FurnitureItem();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.save(apt);
            session.save(type);
            item.setApartment(apt);
            item.setFurnitureType(type);
            item.setQuantity(2);
            item.setUnitPrice(1500);
            session.save(item);
            tx.commit();
        }

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("street", apt.getStreet());
        request.setParameter("house", apt.getHouse());
        request.setParameter("number", apt.getNumber());

        MockHttpServletResponse response = new MockHttpServletResponse();

        furnitureServlet.doGet(request, response);

        assertEquals(200, response.getStatus());
        String content = response.getContentAsString();
        assertTrue(content.contains("Furniture in apartment"));
        assertTrue(content.contains("Sofa"));
    }
}
