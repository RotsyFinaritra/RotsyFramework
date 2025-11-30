package com.etu003184.util;

import java.lang.reflect.Method;

public class RouteHandler {
    private final Class<?> controllerClass;
    private final Method method;
    private String httpMethod;

    public RouteHandler(Class<?> controllerClass, Method method) {
        this.controllerClass = controllerClass;
        this.method = method;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public Method getMethod() {
        return method;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }
}
