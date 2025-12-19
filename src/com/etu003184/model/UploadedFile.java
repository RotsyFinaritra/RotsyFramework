package com.etu003184.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import jakarta.servlet.http.Part;

public class UploadedFile {
    
    private String fileName;
    private String contentType;
    private long size;
    private Path tempFilePath;  // Fichier stocké sur disque
    private Part part;
    private boolean isTemporary = true;

    public UploadedFile() {}

    public UploadedFile(Part part) throws IOException {
        this.part = part;
        this.fileName = extractFileName(part);
        this.contentType = part.getContentType();
        this.size = part.getSize();
        
        // Sauvegarder dans un fichier temporaire si le fichier n'est pas vide
        if (size > 0 && fileName != null && !fileName.isEmpty()) {
            String prefix = "upload_" + System.currentTimeMillis() + "_";
            String suffix = getFileExtension(fileName);
            this.tempFilePath = Files.createTempFile(prefix, suffix);
            
            // Copier le contenu du Part vers le fichier temporaire
            try (InputStream input = part.getInputStream()) {
                Files.copy(input, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
            }
            
            System.out.println("Fichier temporaire créé: " + tempFilePath.toString());
        }
    }

    private String extractFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition != null) {
            for (String token : contentDisposition.split(";")) {
                if (token.trim().startsWith("filename")) {
                    return token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
                }
            }
        }
        return part.getSubmittedFileName(); // Servlet 3.1+
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return ".tmp";
        }
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot);
        }
        return ".tmp";
    }

    /**
     * Sauvegarde le fichier dans un répertoire permanent
     */
    public void saveTo(String directoryPath) throws IOException {
        saveTo(directoryPath, this.fileName);
    }

    /**
     * Sauvegarde le fichier avec un nouveau nom (copie)
     */
    public void saveTo(String directoryPath, String newFileName) throws IOException {
        java.nio.file.Path destPath = java.nio.file.Paths.get(directoryPath, newFileName);
        java.nio.file.Files.createDirectories(destPath.getParent());
        
        if (tempFilePath != null && Files.exists(tempFilePath)) {
            Files.copy(tempFilePath, destPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Fichier sauvegardé: " + destPath.toString());
        } else {
            throw new IOException("Fichier temporaire introuvable");
        }
    }

    /**
     * Déplace le fichier temporaire vers un emplacement permanent (plus rapide que saveTo)
     */
    public void moveTo(String directoryPath, String newFileName) throws IOException {
        Path destPath = java.nio.file.Paths.get(directoryPath, newFileName);
        Files.createDirectories(destPath.getParent());
        
        if (tempFilePath != null && Files.exists(tempFilePath)) {
            Files.move(tempFilePath, destPath, StandardCopyOption.REPLACE_EXISTING);
            this.tempFilePath = destPath;
            this.isTemporary = false;
            System.out.println("Fichier déplacé: " + destPath.toString());
        } else {
            throw new IOException("Fichier temporaire introuvable");
        }
    }

    /**
     * Retourne un InputStream pour lire le fichier
     */
    public InputStream getInputStream() throws IOException {
        if (tempFilePath != null && Files.exists(tempFilePath)) {
            return Files.newInputStream(tempFilePath);
        }
        throw new IOException("Fichier introuvable");
    }

    /**
     * Charge le contenu du fichier en mémoire (à utiliser avec prudence pour les gros fichiers)
     */
    public byte[] getContent() throws IOException {
        if (tempFilePath != null && Files.exists(tempFilePath)) {
            return Files.readAllBytes(tempFilePath);
        }
        return null;
    }

    /**
     * Supprime le fichier temporaire ou permanent
     */
    public void delete() throws IOException {
        if (tempFilePath != null && Files.exists(tempFilePath)) {
            Files.delete(tempFilePath);
            System.out.println("Fichier supprimé: " + tempFilePath.toString());
            tempFilePath = null;
        }
    }

    /**
     * Vérifie si le fichier existe toujours
     */
    public boolean exists() {
        return tempFilePath != null && Files.exists(tempFilePath);
    }

    /**
     * Retourne le chemin absolu du fichier
     */
    public String getAbsolutePath() {
        return tempFilePath != null ? tempFilePath.toAbsolutePath().toString() : null;
    }

    // Getters
    public String getFileName() { return fileName; }
    public String getContentType() { return contentType; }
    public long getSize() { return size; }
    public Path getTempFilePath() { return tempFilePath; }
    public Part getPart() { return part; }
    public boolean isTemporary() { return isTemporary; }
    
    public boolean isEmpty() {
        return size == 0 || fileName == null || fileName.isEmpty();
    }

    @Override
    public String toString() {
        return "UploadedFile{" +
                "fileName='" + fileName + '\'' +
                ", contentType='" + contentType + '\'' +
                ", size=" + size +
                ", tempFile='" + (tempFilePath != null ? tempFilePath.toString() : "none") + '\'' +
                ", isTemporary=" + isTemporary +
                '}';
    }

    // Setters
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setPart(Part part) {
        this.part = part;
    }
}