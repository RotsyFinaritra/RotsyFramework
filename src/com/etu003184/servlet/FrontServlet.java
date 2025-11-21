package com.etu003184.servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Map;

import com.etu003184.model.ModelView;
import com.etu003184.util.RouteHandler;

@WebServlet(name = "FrontServlet", urlPatterns = "/")
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

        try {
            handleRequest(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().println(e.getMessage());
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            handleRequest(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().println(e.getMessage());
        }
    }

    private void handleRequest(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {

        String resourcePath = req.getRequestURI().substring(req.getContextPath().length());
        ServletContext ctx = req.getServletContext();

        System.out.println("resourcePath: " + resourcePath);

        if (resourcePath.isEmpty() || "/".equals(resourcePath)) {
            RequestDispatcher defaultDispatcher = req.getRequestDispatcher("/index.jsp");
            System.out.println("marina");
            defaultDispatcher.forward(req, resp);
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

            
            executeMethod(handler, req, resp);
            
            // out.println("Controller : " + handler.getControllerClass().getName() + "<br>");
            // out.println("Méthode : " + handler.getMethod().getName() + "<br>");
            return;
        }

        if (ctx.getResource(resourcePath) != null) {
            System.out.println("➡️  Ressource statique trouvée : " + resourcePath);
            RequestDispatcher dispatcher = ctx.getNamedDispatcher("default");
            dispatcher.forward(req, resp);
            return;
        }

        // out.println("Tsy haiko par respect : " + resourcePath + "<br>");
        System.out.println("== Tsy haiko par respect : " + resourcePath);
        throw new Exception("Tsy haiko ity  : " + resourcePath);
    }

    private void executeMethod(RouteHandler handler, HttpServletRequest req, HttpServletResponse resp) {
        try {
            Object controllerInstance = handler.getControllerClass().getDeclaredConstructor().newInstance();

            Method method = handler.getMethod();

            if (method.getParameterCount() == 0) {
                Object result = method.invoke(controllerInstance);
                if (result != null && result instanceof String) {
                    resp.getWriter().println("Résultat : " + result);
                } else if (result != null && result instanceof ModelView) {
                    ModelView modelView = (ModelView) result;
                    req.setAttribute("view", modelView.getView());
                    if (!modelView.getData().isEmpty()) {
                        modelView.getData().forEach((key, value) -> {
                            req.setAttribute(key, value);
                        });
                    }
                    RequestDispatcher dispatcher = req.getRequestDispatcher("/" + modelView.getView());
                    dispatcher.forward(req, resp);
                }
            } else if (method.getParameterCount() > 0) {
                Object result = method.invoke(controllerInstance, req, resp);
                if (result != null && result instanceof String) {
                    resp.getWriter().println("Résultat : " + result);
                }
            } else {
                resp.getWriter().println("Tsa metyyy.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                resp.getWriter().println("Erreur lors de l'exécution du contrôleur : " + e.getMessage());
            } catch (IOException ignored) {
            }
        }
    }

}
