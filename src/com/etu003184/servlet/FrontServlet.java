package com.etu003184.servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

import com.etu003184.annotation.Authentified;
import com.etu003184.annotation.Json;
import com.etu003184.annotation.Authorized;
import com.etu003184.annotation.Role;
import com.etu003184.model.ModelView;
import com.etu003184.util.GenericUtil;
import com.etu003184.util.JsonUtil;
import com.etu003184.util.RouteHandler;

@WebServlet(name = "FrontServlet", urlPatterns = "/*")
public class FrontServlet extends HttpServlet {

    private Map<String, List<RouteHandler>> routeMap;

    @Override
    public void init() throws ServletException {
        // Récupérer les routes du contexte
        ServletContext context = getServletContext();
        routeMap = (Map<String, List<RouteHandler>>) context.getAttribute("ROUTES");

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
        if (routeMap != null && routeMap.containsKey(resourcePath)) {

            List<RouteHandler> handlers = routeMap.get(resourcePath);

            RouteHandler matchedHandler = getMatchedHandler(handlers, req);
            if (matchedHandler != null) {
                System.out.println("== Route trouvée ==");
                System.out.println("URL : " + resourcePath);
                System.out.println("Controller : " + matchedHandler.getControllerClass().getName());
                System.out.println("Méthode : " + matchedHandler.getMethod().getName());

                executeMethod(matchedHandler, req, resp);
            }

            // out.println("Controller : " + handler.getControllerClass().getName() +
            // "<br>");
            // out.println("Méthode : " + handler.getMethod().getName() + "<br>");
            return;
        }

        String[] patternAndParamStrings = checkUrlParam(resourcePath, out, req, resp);

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

            RouteHandler handler = getMatchedHandler(routeMap.get(patternAndParamStrings[0]), req);
            System.out.println("Controller miasa : " + handler.getControllerClass().getName());
            System.out.println("Méthode miasa : " + handler.getMethod().getName());
            executeMethodForParameterizedRoute(handler, req, resp, patternAndParamStrings[2]);
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

    private RouteHandler getMatchedHandler(List<RouteHandler> handlers, HttpServletRequest req) {
        RouteHandler result = null;
        for (RouteHandler handler : handlers) {
            if (handler.getHttpMethod().equalsIgnoreCase(req.getMethod())) {
                result = handler;
                break;
            } else if (result == null && handler.getHttpMethod().equalsIgnoreCase("ANY")) {
                result = handler;
            }
        }
        return result;
    }

    private void executeMethodForParameterizedRoute(RouteHandler handler, HttpServletRequest req,
            HttpServletResponse resp, String paramValue) {
        try {
            Object controllerInstance = handler.getControllerClass().getDeclaredConstructor().newInstance();

            Method method = handler.getMethod();

            if (!checkSecurity(handler.getControllerClass(), method, req, resp)) {
                return;
            }

            if (method.getParameterCount() == 0) {
                Object result = method.invoke(controllerInstance);
                handleResultObject(result, method, req, resp);
            } else if (method.getParameterCount() > 0) {
                Parameter[] parameters = method.getParameters();
                Object[] args = new Object[parameters.length];

                if (parameters[0].getType() == int.class || parameters[0].getType() == Integer.class) {
                    args[0] = Integer.parseInt(paramValue);
                } else if (parameters[0].getType() == String.class) {
                    args[0] = paramValue;
                } else if (parameters[0].getType() == double.class || parameters[0].getType() == Double.class) {
                    args[0] = Double.parseDouble(paramValue);
                } else {
                    args[0] = null;
                }

                Object result = GenericUtil.handleMethodWithParameters(controllerInstance, method, args, 1, req, resp);
                handleResultObject(result, method, req, resp);
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

            if (!checkSecurity(handler.getControllerClass(), method, req, resp)) {
                return;
            }

            if (method.getParameterCount() == 0) {
                Object result = method.invoke(controllerInstance);
                handleResultObject(result, method, req, resp);
            } else if (method.getParameterCount() > 0) {
                Object result = GenericUtil.handleMethodWithParameters(controllerInstance, method, null, 0, req, resp);
                handleResultObject(result, method, req, resp);
            } else {
                resp.getWriter().println("Tsa metyyy.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                Method method = handler.getMethod();
                boolean isJsonAnnotated = method.isAnnotationPresent(Json.class);
                
                if (isJsonAnnotated) {
                    // Retourner l'erreur en JSON
                    resp.setContentType("application/json");
                    resp.setCharacterEncoding("UTF-8");
                    resp.setStatus(500);
                    
                    String errorMessage = e.getMessage() != null ? e.getMessage() : "Internal server error";
                    String jsonResponse = JsonUtil.createJsonResponse("error", 500, errorMessage);
                    resp.getWriter().println(jsonResponse);
                } else {
                    // Comportement normal pour les erreurs
                    resp.getWriter().println("Erreur lors de l'exécution du contrôleur : " + e.getMessage());
                }
            } catch (IOException ignored) {
            }
        }
    }

    private void handleResultObject(Object result, Method method, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        // Vérifier si la méthode a l'annotation @Json
        boolean isJsonAnnotated = method.isAnnotationPresent(Json.class);
        
        if (isJsonAnnotated) {
            handleJsonResponse(result, resp);
        } else {
            handleNormalResponse(result, req, resp);
        }
    }

    private boolean checkSecurity(Class<?> controllerClass, Method method, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        boolean isJsonAnnotated = method.isAnnotationPresent(Json.class);

        // Configurable conventions (via web.xml <context-param>)
        // Defaults keep the framework usable without configuration.
        ServletContext servletContext = req.getServletContext();
        String configuredAuthKey = servletContext.getInitParameter("auth.sessionKey");
        if (configuredAuthKey == null || configuredAuthKey.isBlank()) {
            configuredAuthKey = "user";
        }
        String configuredRolesKey = servletContext.getInitParameter("roles.sessionKey");
        if (configuredRolesKey == null || configuredRolesKey.isBlank()) {
            configuredRolesKey = "roles";
        }
        String rolesSeparator = servletContext.getInitParameter("roles.separator");
        if (rolesSeparator == null || rolesSeparator.isBlank()) {
            rolesSeparator = ",";
        }

        Authentified authentified = method.getAnnotation(Authentified.class);
        if (authentified == null) {
            authentified = controllerClass.getAnnotation(Authentified.class);
        }

        Authorized authorized = method.getAnnotation(Authorized.class);
        if (authorized == null) {
            authorized = controllerClass.getAnnotation(Authorized.class);
        }

        Role role = method.getAnnotation(Role.class);
        if (role == null) {
            role = controllerClass.getAnnotation(Role.class);
        }

        boolean needsAuth = authentified != null || authorized != null || role != null;
        if (!needsAuth) {
            return true;
        }

        HttpSession session = req.getSession(false);
        if (session == null) {
            writeSecurityError(resp, isJsonAnnotated, 401, "Unauthorized: session not found");
            return false;
        }

        // Determine which session key indicates authentication
        String authKey = configuredAuthKey;
        if (authentified != null && authentified.value() != null && !authentified.value().isBlank()) {
            authKey = authentified.value().trim();
        } else if (authorized != null && authorized.value() != null && !authorized.value().isBlank()) {
            // Backward compatible: @Authorized("userKey")
            authKey = authorized.value().trim();
        }

        Object principal = session.getAttribute(authKey);
        if (principal == null) {
            writeSecurityError(resp, isJsonAnnotated, 401, "Unauthorized: not authenticated");
            return false;
        }

        if (role != null && role.value() != null && !role.value().isBlank()) {
            String requiredRoles = role.value().trim();
            if (!hasAnyRole(session, configuredRolesKey, rolesSeparator, requiredRoles)) {
                writeSecurityError(resp, isJsonAnnotated, 403, "Forbidden: you do not have the required role to access this resource");
                return false;
            }
        }

        return true;
    }

    private boolean hasAnyRole(HttpSession session, String rolesKey, String rolesSeparator, String requiredRolesCsv) {
        // Configurable convention (rolesKey) + backward compatible fallbacks ("role"/"roles")
        Object configuredRolesAttr = session.getAttribute(rolesKey);
        Object roleAttr = session.getAttribute("role");
        Object rolesAttr = session.getAttribute("roles");

        String[] required = requiredRolesCsv.split(",");
        for (String raw : required) {
            String requiredRole = raw.trim();
            if (requiredRole.isEmpty()) continue;

            if (matchesRole(configuredRolesAttr, rolesSeparator, requiredRole)
                    || matchesRole(roleAttr, rolesSeparator, requiredRole)
                    || matchesRole(rolesAttr, rolesSeparator, requiredRole)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesRole(Object roleObj, String rolesSeparator, String requiredRole) {
        if (roleObj == null) return false;

        if (roleObj instanceof String s) {
            // Accept either a single role ("chef") or a list ("chef,admin") depending on separator
            String trimmed = s.trim();
            if (trimmed.equalsIgnoreCase(requiredRole)) return true;

            if (rolesSeparator != null && !rolesSeparator.isEmpty() && trimmed.contains(rolesSeparator)) {
                String[] parts = trimmed.split(java.util.regex.Pattern.quote(rolesSeparator));
                for (String p : parts) {
                    if (p != null && p.trim().equalsIgnoreCase(requiredRole)) return true;
                }
            }
            return false;
        }

        if (roleObj instanceof String[] arr) {
            for (String s : arr) {
                if (s != null && s.equalsIgnoreCase(requiredRole)) return true;
            }
            return false;
        }

        if (roleObj instanceof java.util.Collection<?> col) {
            for (Object o : col) {
                if (o != null && o.toString().equalsIgnoreCase(requiredRole)) return true;
            }
            return false;
        }

        return roleObj.toString().equalsIgnoreCase(requiredRole);
    }

    private void writeSecurityError(HttpServletResponse resp, boolean json, int status, String message)
            throws IOException {
        resp.setStatus(status);
        if (json) {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().println(JsonUtil.createJsonResponse("error", status, message));
        } else {
            resp.setContentType("text/plain");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().println(message);
        }
    }

    private void handleJsonResponse(Object result, HttpServletResponse resp) throws Exception {
        // Retourner la réponse en JSON
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        String status = "success";
        int code = 200;
        Object data = result;
        
        // Gérer les différents types de résultats
        if (result == null) {
            // Cas où rien n'est trouvé
            status = "error";
            code = 404;
            data = "Resource not found";
        } else if (result instanceof ModelView) {
            ModelView modelView = (ModelView) result;
            data = modelView.getData();
            
            // Vérifier si le ModelView contient des erreurs
            if (modelView.getData().containsKey("error")) {
                status = "error";
                Object errorInfo = modelView.getData().get("error");
                if (errorInfo instanceof String) {
                    String errorMsg = (String) errorInfo;
                    if (errorMsg.toLowerCase().contains("not found")) {
                        code = 404;
                    } else if (errorMsg.toLowerCase().contains("invalid") || 
                              errorMsg.toLowerCase().contains("bad")) {
                        code = 400;
                    } else {
                        code = 500;
                    }
                }
            }
        } else if (result instanceof java.util.Collection) {
            // Vérifier si la collection est vide
            java.util.Collection<?> collection = (java.util.Collection<?>) result;
            if (collection.isEmpty()) {
                status = "error";
                code = 404;
                data = "No data found";
            }
        }
        
        String jsonResponse = JsonUtil.createJsonResponse(status, code, data);
        resp.setStatus(code); // Définir le code de statut HTTP
        resp.getWriter().println(jsonResponse);
    }

    private void handleNormalResponse(Object result, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        // Comportement normal (non-JSON)
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

    private String[] checkUrlParam(String resourcePath, PrintWriter out, HttpServletRequest req,
            HttpServletResponse resp) {
        String[] result = new String[3];
        for (Map.Entry<String, List<RouteHandler>> entry : routeMap.entrySet()) {
            String pattern = entry.getKey();
            List<RouteHandler> handlers = entry.getValue();

            String[] matches = matchesPattern(pattern, resourcePath, req);

            if (matches != null) {
                System.out.println("== Route avec pattern trouvée ==");
                System.out.println("URL : " + resourcePath);
                System.out.println("Pattern : " + pattern);

                for (RouteHandler handler : handlers) {
                    System.out.println("Controller : " + handler.getControllerClass().getName());
                    System.out.println("Méthode : " + handler.getMethod().getName());
                }
                result[0] = pattern;
                result[1] = matches[0];
                result[2] = matches[1];
                return result;
            }
        }
        return null;
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
