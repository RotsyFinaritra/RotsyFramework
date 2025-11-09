package com.etu003184.servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import com.etu003184.util.RouteHandler;

@WebServlet(name = "FrontServlet", urlPatterns = "/*")
public class FrontServlet extends HttpServlet {

    private Map<String, RouteHandler> routeMap;

    @Override
    public void init() throws ServletException {
        // Récupérer les routes du contexte
        ServletContext context = getServletContext();
        routeMap = (Map<String, RouteHandler>) context.getAttribute("ROUTES");

        System.out.println("=== FrontServlet initialisé ===");
    }

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

    private void handleRequest(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String resourcePath = req.getRequestURI().substring(req.getContextPath().length());
        ServletContext ctx = req.getServletContext();

        System.out.println("resourcePath: " + resourcePath);

        if (resourcePath.isEmpty() || "/".equals(resourcePath)) {
            RequestDispatcher defaultDispatcher = req.getRequestDispatcher("/index.jsp");
            defaultDispatcher.forward(req, resp);
            System.out.println("marina");
        }

        if (ctx.getResource(resourcePath) != null) {
            System.out.println("➡️  Ressource statique trouvée : " + resourcePath);
            RequestDispatcher dispatcher = ctx.getNamedDispatcher("default");
            dispatcher.forward(req, resp);
            return;
        }

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        if (routeMap != null && routeMap.containsKey(resourcePath)) {

            RouteHandler handler = routeMap.get(resourcePath);
            System.out.println("== Route trouvée ==");
            System.out.println("URL : " + resourcePath);
            System.out.println("Controller : " + handler.getControllerClass().getName());
            System.out.println("Méthode : " + handler.getMethod().getName());

            out.println("Controller : " + handler.getControllerClass().getName() + "<br>");
            out.println("Méthode : " + handler.getMethod().getName() + "<br>");

        } else {
            out.println("Tsy haiko par respect  : " + resourcePath + "<br>");
            System.out.println("== Tsy haiko par respect : " + resourcePath);
        }
    }

}
