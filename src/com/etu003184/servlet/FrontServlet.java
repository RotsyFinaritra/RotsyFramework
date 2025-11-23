package com.etu003184.servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import com.etu003184.model.ModelView;
import com.etu003184.util.ConstanteRequestMethod;
import com.etu003184.util.GenericUtil;
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

        if (req.getAttribute("__jsp_processing") != null) {
            System.out.println("⚠️ Boucle JSP détectée, miala amin'ny traitement");
            return;
        }

        if (resourcePath.isEmpty() || "/".equals(resourcePath)) {
            RequestDispatcher defaultDispatcher = req.getRequestDispatcher("/index.jsp");
            System.out.println("marina");
            defaultDispatcher.forward(req, resp);
            return;
        }

        // gestion mitokana ny fichier JSP
        if (resourcePath.endsWith(".jsp")) {
            System.out.println("🎯 Forward vers JSP : " + resourcePath);
            RequestDispatcher dispatcher = ctx.getNamedDispatcher("jsp");
            if (dispatcher != null) {
                System.out.println("✅ Utilisation du dispatcher JSP");
                dispatcher.forward(req, resp);
            } else {
                System.out.println("⚠️ Dispatcher JSP non trouvé, utilisation du dispatcher direct");
                // manampy attribut hiala amin'ny boucle infini
                req.setAttribute("__jsp_processing", true);
                RequestDispatcher directDispatcher = ctx.getRequestDispatcher(resourcePath);
                directDispatcher.forward(req, resp);
            }
            return;
        }

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        String[] methods = ConstanteRequestMethod.methods;
        String reqMethod = req.getMethod();

        boolean canExecute = false;
        boolean containsKey = false;
        for (String method : methods) {
            String key = method + " " + resourcePath;
            if (routeMap != null && routeMap.containsKey(key)) {
                containsKey = true;
                if (method.equalsIgnoreCase("ALL")) {
                    canExecute = true;
                } else if (method.equalsIgnoreCase(reqMethod)) {
                    canExecute = true;
                }

                if (canExecute) {
                    RouteHandler handler = routeMap.get(key);
                    System.out.println("== Route trouvée ==");
                    System.out.println("URL : " + resourcePath);
                    System.out.println("Controller : " + handler.getControllerClass().getName());
                    System.out.println("Méthode : " + handler.getMethod().getName());

                    executeMethod(handler, req, resp);
                    return;
                }
                // out.println("Controller : " + handler.getControllerClass().getName() +
                // "<br>");
                // out.println("Méthode : " + handler.getMethod().getName() + "<br>");
            }

        }

        Object[] patternCheckResult = checkUrlParam(resourcePath, out, req, resp);
        containsKey = (boolean) patternCheckResult[0];
        String[] patternAndParamStrings = (String[]) patternCheckResult[1];

        if (patternAndParamStrings != null) {
            // out.println("== Route avec pattern trouvée == <br>");
            // out.println("URL : " + resourcePath + "<br>");
            // out.println("Pattern : " + patternAndParamStrings[0] + "<br>");
            // out.println("Controller : " +
            // routeMap.get(patternAndParamStrings[0]).getControllerClass().getName() +
            // "<br>");
            // out.println("Méthode : " +
            // routeMap.get(patternAndParamStrings[0]).getMethod().getName() + "<br>");
            // out.println("Paramètre extrait : " + patternAndParamStrings[1] + " = " +
            // patternAndParamStrings[2] + "<br>");
            // out.println("== Route avec pattern trouvée == <br>");
            String method2 = patternAndParamStrings[0].split(" ")[0];

            if (method2.equalsIgnoreCase("ALL") || method2.equalsIgnoreCase(reqMethod)) {
                canExecute = true;
            }

            if (canExecute) {
                RouteHandler handler = routeMap.get(patternAndParamStrings[0]);
                executeMethodForParameterizedRoute(handler, req, resp, patternAndParamStrings[2]);
                return;
            }
        }

        if (!canExecute && containsKey) {
            out.println("Méthode HTTP non autorisée pour cette route.");
            resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
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

    private void executeMethodForParameterizedRoute(RouteHandler handler, HttpServletRequest req,
            HttpServletResponse resp, String paramValue) {
        try {
            Object controllerInstance = handler.getControllerClass().getDeclaredConstructor().newInstance();

            Method method = handler.getMethod();

            if (method.getParameterCount() == 0) {
                Object result = method.invoke(controllerInstance);
                handleResultObject(result, req, resp);
            } else if (method.getParameterCount() > 0) {
                Parameter[] parameters = method.getParameters();
                Object[] args = new Object[parameters.length];

                if (parameters[0].getType() == int.class || parameters[0].getType() == Integer.class) {
                    args[0] = Integer.parseInt(paramValue);
                } else if (parameters[0].getType() == String.class) {
                    args[0] = paramValue;
                } else if (parameters[0].getType() == double.class || parameters[0].getType() == Double.class) {
                    args[0] = Double.parseDouble(paramValue);
                }
                Object result = method.invoke(controllerInstance, args);
                handleResultObject(result, req, resp);
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

    private void executeMethod(RouteHandler handler, HttpServletRequest req, HttpServletResponse resp) {
        try {
            Object controllerInstance = handler.getControllerClass().getDeclaredConstructor().newInstance();

            Method method = handler.getMethod();

            if (method.getParameterCount() == 0) {
                Object result = method.invoke(controllerInstance);
                handleResultObject(result, req, resp);
            } else if (method.getParameterCount() > 0) {
                Object result = GenericUtil.handleMethodWithParameters(controllerInstance, method, req, resp);
                handleResultObject(result, req, resp);
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

    private void handleResultObject(Object result, HttpServletRequest req, HttpServletResponse resp) throws Exception {
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
    }

    private Object[] checkUrlParam(String resourcePath, PrintWriter out, HttpServletRequest req,
            HttpServletResponse resp) {
        Object[] resultObjects = new Object[2];
        String[] result = null;
        boolean containsKey = false;

        for (Map.Entry<String, RouteHandler> entry : routeMap.entrySet()) {
            String[] urlParts = entry.getKey().split(" ");
            String pattern = urlParts[1];
            RouteHandler handler = entry.getValue();

            String[] matches = matchesPattern(pattern, resourcePath, req);

            if (matches != null) {
                containsKey = true;

                System.out.println("== Route avec pattern trouvée ==");
                System.out.println("URL : " + resourcePath);
                System.out.println("Pattern : " + pattern);
                System.out.println("Controller : " + handler.getControllerClass().getName());
                System.out.println("Méthode : " + handler.getMethod().getName());

                if (urlParts[0].equalsIgnoreCase("ALL") || urlParts[0].equalsIgnoreCase(req.getMethod())) {
                    result = new String[3];
                    result[0] = entry.getKey();
                    result[1] = matches[0];
                    result[2] = matches[1];
                }
            }
        }
        resultObjects[0] = containsKey;
        resultObjects[1] = result;

        return resultObjects;
    }

    private String[] matchesPattern(String pattern, String path, HttpServletRequest req) {
        // Diviser les chemins en segments
        String[] patternParts = pattern.split("/");
        String[] pathParts = path.split("/");

        String[] result = new String[2];

        // Même nombre de segments requis
        if (patternParts.length != pathParts.length) {
            return null;
        }

        // Comparer chaque segment
        for (int i = 0; i < patternParts.length; i++) {
            String patternPart = patternParts[i];
            String pathPart = pathParts[i];

            if (patternPart.startsWith("{") && patternPart.endsWith("}")) {
                // C'est un paramètre - extraire la valeur
                String paramName = patternPart.substring(1, patternPart.length() - 1);
                req.setAttribute("param_" + paramName, pathPart);
                System.out.println("Paramètre extrait : " + paramName + " = " + pathPart);
                result[0] = paramName;
                result[1] = pathPart;
            } else {
                // Segment fixe - doit correspondre exactement
                if (!patternPart.equals(pathPart)) {
                    return null;
                }
            }
        }
        return result;
    }

}
