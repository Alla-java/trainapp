package com.example.trainapp.servlet;

import com.example.trainapp.model.Apartment;
import com.example.trainapp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


public class ApartmentServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String street = request.getParameter("street");
        String house = request.getParameter("house");
        String building = request.getParameter("building");
        String number = request.getParameter("number");

        if (street == null || house == null || number == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing apartment parameters.");
            return;
        }

        Transaction tx = null;
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

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

            List<Apartment> existing = query.getResultList();

            if (!existing.isEmpty()) {
                response.sendError(HttpServletResponse.SC_CONFLICT, "Apartment already exists.");
                tx.rollback();
                return;
            }

            Apartment apartment = new Apartment();
            apartment.setStreet(street);
            apartment.setHouse(house);
            apartment.setBuilding(building);
            apartment.setNumber(number);

            session.save(apartment);
            tx.commit();

            response.setContentType("text/plain; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().println("Apartment saved successfully.");
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                tx.rollback();
            }
            e.printStackTrace();
            throw new ServletException("Error saving apartment", e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String idParam = request.getParameter("id");
        if (idParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing 'id' parameter.");
            return;
        }

        Long id;
        try {
            id = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid 'id' format.");
            return;
        }

        Apartment apartment;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            apartment = session.get(Apartment.class, id);
        }

        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<html><body>");
        if (apartment != null) {
            out.println("<h2>Apartment Details</h2>");
            out.println("<p><b>Street:</b> " + apartment.getStreet() + "<br>");
            out.println("<b>House:</b> " + apartment.getHouse() + "<br>");
            out.println("<b>Building:</b> " + apartment.getBuilding() + "<br>");
            out.println("<b>Number:</b> " + apartment.getNumber() + "</p>");
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.println("<h2>Apartment not found.</h2>");
        }
        out.println("</body></html>");
    }
}
