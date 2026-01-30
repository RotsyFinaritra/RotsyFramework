package com.etu003184.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Enumeration;
import java.util.Map;

import com.etu003184.annotation.RequestParam;
import com.etu003184.annotation.Session;
import com.etu003184.model.UploadedFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class GenericUtil {

    public static Object getArrayFromParameters(HttpServletRequest req, Class<?> componentType, String arrayPrefix) {
        try {
            java.util.List<Object> objectList = new java.util.ArrayList<>();

            // Récupérer tous les noms de paramètres pour détecter les indices
            java.util.Set<Integer> indices = new java.util.TreeSet<>(); // Utiliser TreeSet pour maintenir l'ordre
            java.util.Enumeration<String> paramNames = req.getParameterNames();

            while (paramNames.hasMoreElements()) {
                String paramName = paramNames.nextElement();
                if (paramName.startsWith(arrayPrefix + "[") && paramName.contains("].")) {
                    // Extraire l'indice du paramètre arrayPrefix[index].fieldName
                    String indexPart = paramName.substring(arrayPrefix.length() + 1);
                    int closeBracket = indexPart.indexOf(']');
                    if (closeBracket > 0) {
                        try {
                            int index = Integer.parseInt(indexPart.substring(0, closeBracket));
                            indices.add(index);
                        } catch (NumberFormatException e) {
                            // Ignorer si ce n'est pas un nombre valide
                        }
                    }
                }
            }

            // Créer les objets pour chaque indice trouvé
            for (Integer index : indices) {
                String elementPrefix = arrayPrefix + "[" + index + "]";
                Object element = getObjectFromParameters(req, componentType, elementPrefix);
                objectList.add(element);
            }

            // Convertir ArrayList en tableau du bon type
            Object array = java.lang.reflect.Array.newInstance(componentType, objectList.size());
            for (int i = 0; i < objectList.size(); i++) {
                java.lang.reflect.Array.set(array, i, objectList.get(i));
            }

            return array;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la création du tableau d'objets complexes.");
        }
    }

    public static Object getObjectFromParameters(HttpServletRequest req, Class<?> clazz, String mere) {
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            Field[] fields = clazz.getDeclaredFields();
            String className = clazz.getSimpleName().toLowerCase();

            mere = mere.isEmpty() ? className : mere;

            for (Field field : fields) {
                String fieldName = field.getName();
                String paramName = mere + "." + fieldName;
                String paramValue = req.getParameter(paramName);
                field.setAccessible(true);
                Class<?> fieldType = field.getType();

                if (paramValue != null) {

                    System.out.println("Setting field: " + fieldName + " of type " + fieldType.getName()
                            + " with value from param: " + paramName);

                    if (fieldType == String.class) {
                        field.set(instance, paramValue);
                    } else if (fieldType == int.class || fieldType == Integer.class) {
                        field.set(instance, Integer.parseInt(paramValue));
                    } else if (fieldType == double.class || fieldType == Double.class) {
                        field.set(instance, Double.parseDouble(paramValue));
                    } else if (isComplexObjectArray(fieldType)) {
                        System.out.println("Tableau d'objets complexes détecté pour le champ: " + fieldName);
                        Class<?> componentType = fieldType.getComponentType();
                        Object arrayResult = getArrayFromParameters(req, componentType, paramName);
                        field.set(instance, arrayResult);
                    } else if (isComplexObject(fieldType)) {
                        System.out.println("complex field detected: " + fieldName + " of type " + fieldType.getName());
                        Object objetResult = getObjectFromParameters(req, fieldType, paramName);
                        field.set(instance, objetResult);
                    } else {
                        System.out
                                .println("Unsupported field type: " + fieldType.getName() + " for field " + fieldName);
                        field.set(instance, null);
                    }
                } else if (isComplexObjectArray(fieldType)) {
                    // Gérer les tableaux d'objets complexes comme dept[0], dept[1], etc.
                    System.out.println("Tableau d'objets complexes détecté pour le champ: " + fieldName);
                    Class<?> componentType = fieldType.getComponentType();
                    Object arrayResult = getArrayFromParameters(req, componentType, paramName);
                    field.set(instance, arrayResult);
                } else if (isComplexObject(fieldType)) {
                    // Vérifier s'il y a des paramètres pour cet objet complexe
                    boolean hasComplexParams = hasParametersWithPrefix(req, paramName);
                    if (hasComplexParams) {
                        Object objetResult = getObjectFromParameters(req, fieldType, paramName);
                        field.set(instance, objetResult);
                    }
                }
            }

            return instance;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la création de l'objet à partir des paramètres.");
        }
    }

    public static boolean hasParametersWithPrefix(HttpServletRequest req, String prefix) {
        java.util.Enumeration<String> paramNames = req.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            // Vérifier pour les objets simples (prefix.field) et les tableaux (prefix[0].field)
            if (paramName.startsWith(prefix + ".") || paramName.startsWith(prefix + "[")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isComplexObjectArray(Class<?> type) {
        if (!type.isArray())
            return false;

        Class<?> component = type.getComponentType();
        return !component.isPrimitive() && isComplexObject(component);
    }

    public static boolean isComplexObject(Class<?> type) {

        if (type.isPrimitive())
            return false;

        if (type == Boolean.class || type == Byte.class || type == Character.class ||
                type == Short.class || type == Integer.class || type == Long.class ||
                type == Float.class || type == Double.class ||
                type == String.class ||
                Number.class.isAssignableFrom(type) || // BigDecimal, BigInteger, etc.
                type.isEnum() ||
                type == java.util.Date.class ||
                type == java.sql.Date.class ||
                type == java.time.LocalDate.class ||
                type == java.time.LocalDateTime.class ||
                type == java.time.LocalTime.class ||
                type == HttpServletRequest.class ||
                type == HttpServletResponse.class ||
                type == Map.class ||
                Map.class.isAssignableFrom(type)) {

            return false;
        }

        return true;
    }

    public static boolean isMapStringObject(Parameter parameter) {
        if (!Map.class.isAssignableFrom(parameter.getType())) {
            return false;
        }

        // Vérifier les types génériques
        if (parameter.getParameterizedType() instanceof ParameterizedType pType) {
            Type[] actualTypes = pType.getActualTypeArguments();
            if (actualTypes.length == 2) {
                return actualTypes[0] == String.class && actualTypes[1] == Object.class;
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
        
        boolean isMultipart = FileUploadUtil.isMultipartRequest(req);
        
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

            Session sessionAnnotation = parameter.getAnnotation(Session.class);

            try {
                if (paramType == HttpServletRequest.class) {
                    args[i] = req;
                } else if (paramType == HttpServletResponse.class) {
                    args[i] = resp;
                } else if (paramType == jakarta.servlet.http.HttpSession.class) {
                    // Allow direct access to the session if requested
                    args[i] = req.getSession();
                } else if (paramType == UploadedFile.class) {
                    // Gestion d'un seul fichier uploadé
                    if (isMultipart) {
                        args[i] = FileUploadUtil.getUploadedFile(req, paramName);
                    } else {
                        args[i] = null;
                    }
                } else if (paramType == UploadedFile[].class) {
                    // Gestion de plusieurs fichiers uploadés
                    if (isMultipart) {
                        args[i] = FileUploadUtil.getUploadedFiles(req, paramName);
                    } else {
                        args[i] = new UploadedFile[0];
                    }
                } else if (paramType == String.class) {
                    String paramValue;
                    if (isMultipart) {
                        paramValue = FileUploadUtil.getFormParameter(req, paramName);
                    } else {
                        paramValue = req.getParameter(paramName);
                    }
                    args[i] = paramValue;
                } else if (paramType == int.class || paramType == Integer.class) {
                    String paramValue;
                    if (isMultipart) {
                        paramValue = FileUploadUtil.getFormParameter(req, paramName);
                    } else {
                        paramValue = req.getParameter(paramName);
                    }
                    args[i] = paramValue != null ? Integer.parseInt(paramValue) : 0;
                } else if (paramType == double.class || paramType == Double.class) {
                    String paramValue;
                    if (isMultipart) {
                        paramValue = FileUploadUtil.getFormParameter(req, paramName);
                    } else {
                        paramValue = req.getParameter(paramName);
                    }
                    args[i] = paramValue != null ? Double.parseDouble(paramValue) : 0.0;
                } else if (GenericUtil.isMapStringObject(parameter)) {
                    // If annotated with @Session, bind to HttpSession attributes (live view)
                    if (sessionAnnotation != null) {
                        args[i] = new SessionMap(req.getSession());
                    } else {
                        args[i] = GenericUtil.getMappedParameters(req);
                    }
                } else if (GenericUtil.isComplexObjectArray(paramType)) {
                    System.out.println("Tableau d'objets complexes détecté pour le paramètre: " + paramName);
                    Class<?> componentType = paramType.getComponentType();
                    args[i] = getArrayFromParameters(req, componentType, paramName);
                } else if (GenericUtil.isComplexObject(paramType)) {
                    System.out.println("Objet complexe détecté pour le paramètre: " + paramName + " de type "
                            + paramType.getName());
                    args[i] = GenericUtil.getObjectFromParameters(req, paramType, "");
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