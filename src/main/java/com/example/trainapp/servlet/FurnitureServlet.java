package com.example.trainapp.servlet;
import com.example.trainapp.model.Apartment;
import com.example.trainapp.model.FurnitureItem;
import com.example.trainapp.model.FurnitureType;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.example.trainapp.util.HibernateUtil;

import org.hibernate.query.Query;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class FurnitureServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String street = request.getParameter("street");
        String house = request.getParameter("house");
        String building = request.getParameter("building");
        String number = request.getParameter("number");

        String typeName = request.getParameter("typeName");
        double unitPrice = Double.parseDouble(request.getParameter("unitPrice"));
        int quantity = Integer.parseInt(request.getParameter("quantity"));

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();

        try {
            // Найти или создать тип мебели
            FurnitureType furnitureType = session.createQuery(
                            "FROM FurnitureType ft WHERE ft.name = :name", FurnitureType.class)
                    .setParameter("name", typeName)
                    .uniqueResult();

            if (furnitureType == null) {
                furnitureType = new FurnitureType();
                furnitureType.setName(typeName);
                session.save(furnitureType);
            }

            // Найти или создать квартиру
            String hql = "FROM Apartment a WHERE a.street = :street AND a.house = :house AND a.number = :number";
            if (building != null && !building.trim().isEmpty()) {
                hql += " AND a.building = :building";
            }

            Query<Apartment> query = session.createQuery(hql, Apartment.class)
                    .setParameter("street", street)
                    .setParameter("house", house)
                    .setParameter("number", number);

            if (building != null && !building.trim().isEmpty()) {
                query.setParameter("building", building);
            }

            Apartment apartment = query.uniqueResult();

            if (apartment == null) {
                apartment = new Apartment();
                apartment.setStreet(street);
                apartment.setHouse(house);
                apartment.setBuilding(building);
                apartment.setNumber(number);
                session.save(apartment);
            }

            // Сохранение предмета мебели
            FurnitureItem item = new FurnitureItem();
            item.setUnitPrice(unitPrice);
            item.setQuantity(quantity);
            item.setFurnitureType(furnitureType);
            item.setApartment(apartment);
            session.save(item);

            tx.commit();

            response.setContentType("text/plain; charset=UTF-8");
            response.setCharacterEncoding("UTF-8"); ;
            response.getWriter().println("Furniture item saved successfully.");
        } catch (Exception e) {
            tx.rollback();
            throw new ServletException("Error saving furniture item", e);
        } finally {
            session.close();
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String street = request.getParameter("street");
        String house = request.getParameter("house");
        String number = request.getParameter("number");
        String building = request.getParameter("building"); // необязательный

        if (street == null || house == null || number == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required apartment parameters.");
            return;
        }

        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            String hql = "FROM Apartment a WHERE a.street = :street AND a.house = :house AND a.number = :number";
            if (building != null && !building.trim().isEmpty()) {
                hql += " AND a.building = :building";
            }

            Query<Apartment> query = session.createQuery(hql, Apartment.class)
                    .setParameter("street", street)
                    .setParameter("house", house)
                    .setParameter("number", number);

            if (building != null && !building.trim().isEmpty()) {
                query.setParameter("building", building);
            }

            List<Apartment> apartments = query.getResultList();

            if (apartments.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Apartment not found.");
                return;
            }

            if (apartments.size() > 1) {
                response.sendError(HttpServletResponse.SC_CONFLICT, "Multiple apartments match the criteria.");
                return;
            }

            Apartment apartment = apartments.get(0);

            List<FurnitureItem> items = session.createQuery(
                            "SELECT DISTINCT fi FROM FurnitureItem fi " +
                                    "JOIN FETCH fi.type " +
                                    "WHERE fi.apartment.id = :aptId", FurnitureItem.class)
                    .setParameter("aptId", apartment.getId())
                    .getResultList();

            response.setContentType("text/html; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();

            out.println("<html><body>");
            out.println("<h2>Furniture in apartment " + street + ", house " + house +
                    (building != null && !building.trim().isEmpty() ? ", building " + building : "") +
                    ", apt. " + number + "</h2>");

            if (items.isEmpty()) {
                out.println("<p>No furniture items found.</p>");
            } else {
                for (FurnitureItem item : items) {
                    out.println("<p><b>FurnitureItem</b> " + item.getFurnitureType().getName() +
                            " — " + item.getQuantity() + " × " + item.getUnitPrice() +
                            " = <b>" + item.getTotalPrice() + "</b></p><hr>");
                }
            }

            out.println("</body></html>");
        } finally {
            session.close();
        }
    }
}
