package com.etu003184.util;

import java.lang.reflect.Method;

public class RouteHandler {
    private final Class<?> controllerClass;
    private final Method method;

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
}
