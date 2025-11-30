package com.etu003184.listener;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.etu003184.util.ClassScanner;
import com.etu003184.util.RouteHandler;

@WebListener
public class AppInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("=== Initialisation de l'application ===");

        ServletContext context = sce.getServletContext();
        ClassScanner scanner = new ClassScanner();

        // scan classpath
        String classesPath = context.getRealPath("/WEB-INF/classes");
        try {
            scanner.scanDirectory(new File(classesPath), "");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Scanner tous les JAR dans WEB-INF/lib
        // File libDir = new File(context.getRealPath("/WEB-INF/lib"));
        // if (libDir.exists() && libDir.isDirectory()) {
        // for (File jar : libDir.listFiles((d, name) -> name.endsWith(".jar"))) {
        // scanner.scanJar(jar);
        // }
        // }

        Map<String, List<RouteHandler>> routes = scanner.getRouteMap();
        context.setAttribute("ROUTES", routes);

        System.out.println("=== Routes enregistrées ===");
        for (String url : routes.keySet()) {
            List<RouteHandler> handlers = routes.get(url);
            System.out.println("url: " + url);
            for (RouteHandler handler : handlers) {
                System.out.println("  " +
                        handler.getControllerClass().getName() + "." +
                        handler.getMethod().getName() + " [" + handler.getHttpMethod() + "]");
            }
            System.out.println("-----------------------");
        }

        System.out.println("=== Initialisation terminée ===");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("=== Application arrêtée ===");
    }
}
