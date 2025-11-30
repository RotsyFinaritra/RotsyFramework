package com.etu003184.util;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Enumeration;
import java.util.Map;

import com.etu003184.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class GenericUtil {

    public static boolean isMapStringObject(Parameter parameter) {
        if (!Map.class.isAssignableFrom(parameter.getType())) {
            return false;
        }

        // Vérifier les types génériques
        if (parameter.getParameterizedType() instanceof ParameterizedType pType) {
            Type[] actualTypes = pType.getActualTypeArguments();
            if (actualTypes.length == 2) {
                return actualTypes[0] == String.class;
            }
        }
        return false;
    }

    public static Map<String, Object> getMappedParameters(HttpServletRequest req) {
        Map<String, Object> paramMap = new java.util.HashMap<>();

        Enumeration<String> names = req.getParameterNames();

        while (names.hasMoreElements()) {
            String name = names.nextElement();
            Object values = req.getParameterValues(name);
            paramMap.put(name, values);
        }

        return paramMap;
    }

    public static Object handleMethodWithParameters(Object instance, Method method, Object[] args, int startIndex,
            HttpServletRequest req,
            HttpServletResponse resp) {
        Parameter[] parameters = method.getParameters();
        System.out.println("Parameters length: " + parameters.length);
        if (args == null) {
            args = new Object[parameters.length];
        }
        // Object[] args = new Object[parameters.length];
        for (int i = startIndex; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> paramType = parameter.getType();

            System.out.println("Preparing parameter " + i + " of type " + paramType.getName());
            System.out.println("Parameter name: " + parameter.getName());
            String paramName = parameter.getName();

            RequestParam requestParamAnnotation = parameter.getAnnotation(RequestParam.class);
            if (requestParamAnnotation != null && !requestParamAnnotation.value().isEmpty()) {
                paramName = requestParamAnnotation.value();
            }

            try {
                if (paramType == HttpServletRequest.class) {
                    args[i] = req;
                } else if (paramType == HttpServletResponse.class) {
                    args[i] = resp;
                } else if (paramType == String.class) {
                    String paramValue = req.getParameter(paramName);
                    args[i] = paramValue;
                } else if (paramType == int.class || paramType == Integer.class) {
                    String paramValue = req.getParameter(paramName);
                    args[i] = paramValue != null ? Integer.parseInt(paramValue) : 0;
                } else if (paramType == double.class || paramType == Double.class) {
                    String paramValue = req.getParameter(paramName);
                    args[i] = paramValue != null ? Double.parseDouble(paramValue) : 0.0;
                } else if (GenericUtil.isMapStringObject(parameter)) {
                    args[i] = GenericUtil.getMappedParameters(req);
                } else {
                    args[i] = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                args[i] = null;
            }
        }
        System.out.println("Arguments prepared: " + java.util.Arrays.toString(args));
        try {
            return method.invoke(instance, args);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Misy erreur ny execution ny méthode miaraka amin'ny paramètres.");
        }
    }
}
