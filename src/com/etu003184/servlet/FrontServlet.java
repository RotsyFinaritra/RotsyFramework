package com.etu003184.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class FrontServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        logRequest(req);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        logRequest(req);
    }

    private void logRequest(HttpServletRequest req) {
        String url = req.getRequestURL().toString();
        System.out.println("== Nouvelle requête ==");
        System.out.println("URL demandée : " + url);
    }
}
