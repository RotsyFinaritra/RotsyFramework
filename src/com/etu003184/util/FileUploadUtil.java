package com.etu003184.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.etu003184.model.UploadedFile;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

public class FileUploadUtil {

    /**
     * Vérifie si la requête est de type multipart/form-data
     */
    public static boolean isMultipartRequest(HttpServletRequest req) {
        String contentType = req.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/form-data");
    }

    /**
     * Récupère un seul fichier uploadé par son nom de paramètre
     */
    public static UploadedFile getUploadedFile(HttpServletRequest req, String paramName) 
            throws IOException, ServletException {
        Part part = req.getPart(paramName);
        if (part == null || part.getSize() == 0) {
            return null;
        }
        return new UploadedFile(part);
    }

    /**
     * Récupère plusieurs fichiers uploadés avec le même nom de paramètre
     */
    public static UploadedFile[] getUploadedFiles(HttpServletRequest req, String paramName) 
            throws IOException, ServletException {
        Collection<Part> parts = req.getParts();
        List<UploadedFile> files = new ArrayList<>();

        for (Part part : parts) {
            if (part.getName().equals(paramName) && part.getSize() > 0) {
                files.add(new UploadedFile(part));
            }
        }

        return files.toArray(new UploadedFile[0]);
    }

    /**
     * Récupère tous les fichiers uploadés
     */
    public static List<UploadedFile> getAllUploadedFiles(HttpServletRequest req) 
            throws IOException, ServletException {
        Collection<Part> parts = req.getParts();
        List<UploadedFile> files = new ArrayList<>();

        for (Part part : parts) {
            // Vérifier que c'est un fichier (pas un champ de formulaire normal)
            if (part.getSubmittedFileName() != null && !part.getSubmittedFileName().isEmpty()) {
                files.add(new UploadedFile(part));
            }
        }

        return files;
    }

    /**
     * Récupère la valeur d'un paramètre de formulaire dans une requête multipart
     */
    public static String getFormParameter(HttpServletRequest req, String paramName) 
            throws IOException, ServletException {
        Part part = req.getPart(paramName);
        if (part == null) {
            return null;
        }
        // Si c'est un fichier, retourner null
        if (part.getSubmittedFileName() != null && !part.getSubmittedFileName().isEmpty()) {
            return null;
        }
        return new String(part.getInputStream().readAllBytes(), "UTF-8");
    }

    /**
     * Nettoie tous les fichiers temporaires d'un tableau
     */
    public static void cleanupFiles(UploadedFile... files) {
        if (files != null) {
            for (UploadedFile file : files) {
                if (file != null) {
                    try {
                        file.delete();
                    } catch (IOException e) {
                        System.err.println("Erreur lors de la suppression du fichier: " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Nettoie une liste de fichiers temporaires
     */
    public static void cleanupFiles(List<UploadedFile> files) {
        if (files != null) {
            for (UploadedFile file : files) {
                try {
                    file.delete();
                } catch (IOException e) {
                    System.err.println("Erreur lors de la suppression du fichier: " + e.getMessage());
                }
            }
        }
    }
}