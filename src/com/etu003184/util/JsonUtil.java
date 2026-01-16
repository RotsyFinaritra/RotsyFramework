package com.etu003184.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

public class JsonUtil {

    /**
     * Convertit un objet en JSON en utilisant Jackson si disponible, sinon utilise une implémentation de base
     */
    public static String toJson(Object obj) {
        // Essayer d'utiliser Jackson si disponible dans le classpath
        try {
            Class<?> objectMapperClass = Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
            Object mapper = objectMapperClass.getDeclaredConstructor().newInstance();
            java.lang.reflect.Method writeValueAsStringMethod = objectMapperClass.getMethod("writeValueAsString", Object.class);
            System.out.println("Nahita jackson");
            return (String) writeValueAsStringMethod.invoke(mapper, obj);
        } catch (Exception e) {
            // Jackson non disponible, utiliser l'implémentation de base
            System.out.println("Jackson non trouvé, utilisation de l'implémentation JSON de base");
            return toJsonBasic(obj);
        }
    }

    /**
     * Implémentation JSON de base (fallback)
     */
    private static String toJsonBasic(Object obj) {
        if (obj == null) {
            return "null";
        }

        if (obj instanceof String) {
            return "\"" + escapeString((String) obj) + "\"";
        }

        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }

        if (obj instanceof Collection) {
            return collectionToJson((Collection<?>) obj);
        }

        if (obj.getClass().isArray()) {
            return arrayToJson(obj);
        }

        if (obj instanceof Map) {
            return mapToJson((Map<?, ?>) obj);
        }

        return objectToJson(obj);
    }

    private static String objectToJson(Object obj) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        Field[] fields = obj.getClass().getDeclaredFields();
        boolean first = true;

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (value != null) {
                    if (!first) {
                        sb.append(",");
                    }
                    sb.append("\"").append(field.getName()).append("\":");
                    sb.append(toJson(value));
                    first = false;
                }
            } catch (IllegalAccessException e) {
                // Ignorer les champs inaccessibles
            }
        }

        sb.append("}");
        return sb.toString();
    }

    private static String collectionToJson(Collection<?> collection) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        boolean first = true;
        for (Object item : collection) {
            if (!first) {
                sb.append(",");
            }
            sb.append(toJson(item));
            first = false;
        }

        sb.append("]");
        return sb.toString();
    }

    private static String arrayToJson(Object array) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(toJson(Array.get(array, i)));
        }

        sb.append("]");
        return sb.toString();
    }

    private static String mapToJson(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(entry.getKey().toString()).append("\":");
            sb.append(toJson(entry.getValue()));
            first = false;
        }

        sb.append("}");
        return sb.toString();
    }

    private static String escapeString(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    public static String createJsonResponse(String status, int code, Object data) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"status\":\"").append(status).append("\",");
        sb.append("\"code\":").append(code).append(",");
        sb.append("\"data\":").append(toJson(data));
        sb.append("}");
        return sb.toString();
    }
}