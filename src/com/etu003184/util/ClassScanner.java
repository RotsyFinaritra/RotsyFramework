package com.etu003184.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.etu003184.annotation.Controller;
import com.etu003184.annotation.GetMapping;
import com.etu003184.annotation.UrlMapping;

public class ClassScanner {

    private final Map<String, RouteHandler> routeMap = new HashMap<>();

    public Map<String, RouteHandler> getRouteMap() {
        return routeMap;
    }

    // --------------------------
    // Scanner un répertoire (WEB-INF/classes)
    // --------------------------
    public void scanDirectory(File dir, String packageName) throws Exception {
        if (!dir.exists())
            return;

        File[] files = dir.listFiles();
        if (files == null)
            return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + file.getName() + ".");
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + file.getName().replace(".class", "");
                try {
                    Class<?> cls = Class.forName(className);
                    scanClassForRoutes(cls);
                } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
                    throw new Exception("Tsy nahita classe");
                }
            }
        }
    }

    // --------------------------
    // Scanner un JAR (WEB-INF/lib)
    // --------------------------
    public void scanJar(File jarFile) {
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            URL[] urls = { jarFile.toURI().toURL() };
            URLClassLoader cl = new URLClassLoader(urls, this.getClass().getClassLoader());

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName().replace("/", ".").replace(".class", "");
                    try {
                        Class<?> cls = cl.loadClass(className);
                        scanClassForRoutes(cls);
                    } catch (Throwable ignored) {
                    }
                }
            }
            cl.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --------------------------
    // Vérifie si la classe a @Controller et ses méthodes @UrlMapping
    // --------------------------
    private void scanClassForRoutes(Class<?> cls) {
        if (cls.isAnnotationPresent(Controller.class)) {
            for (Method method : cls.getDeclaredMethods()) {
                if (method.isAnnotationPresent(UrlMapping.class)) {
                    String url = method.getAnnotation(UrlMapping.class).value();
                    url = "ALL " + url;
                    routeMap.put(url, new RouteHandler(cls, method));
                    System.out.println("Mapped: " + url + " -> " +
                            cls.getName() + "." + method.getName());
                } else if (method.isAnnotationPresent(GetMapping.class)) {
                    String url = method.getAnnotation(GetMapping.class).value();
                    url = "GET " + url;
                    routeMap.put(url, new RouteHandler(cls, method));
                    System.out.println("Mapped: " + url + " -> " +
                            cls.getName() + "." + method.getName());
                } else if (method.isAnnotationPresent(com.etu003184.annotation.PostMapping.class)) {
                    String url = method.getAnnotation(com.etu003184.annotation.PostMapping.class).value();
                    url = "POST " + url;
                    routeMap.put(url, new RouteHandler(cls, method));
                    System.out.println("Mapped: " + url + " -> " +
                            cls.getName() + "." + method.getName());
                }
            }
        }
    }
}
