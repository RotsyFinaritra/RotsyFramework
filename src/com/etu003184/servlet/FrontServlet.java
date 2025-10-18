package com.etu003184.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

public class FrontServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handleRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handleRequest(req, resp);
    }

    private void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String path = req.getRequestURI();
        String resourcePath = req.getRequestURI().substring(req.getContextPath().length());

        if ("/".equals(resourcePath) || "".equals(resourcePath)) {
            resp.getWriter().println("/");
            return;
        }

        boolean resourceExists = req.getServletContext().getResource(resourcePath) != null;

        if (resourceExists) {
            System.out.println("La ressource demandée existe : " + resourcePath);
            RequestDispatcher dispatcher = req.getServletContext().getNamedDispatcher("default");
            dispatcher.forward(req, resp);
        } else {
            PrintWriter out = resp.getWriter();
            resp.setContentType("text/html");
            out.println("URL demandée : " + path + "<br>");
            out.println("Ressource demandée : " + resourcePath + "<br>");
        }

        // String url = req.getRequestURL().toString();
        // // System.out.println("== Nouvelle requête ==");
        // System.out.println("URL demandée : " + url);
    }
}
