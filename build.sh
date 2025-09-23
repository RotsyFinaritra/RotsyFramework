#!/bin/bash
set -e

# Emplacement du servlet-api.jar (Tomcat déjà installé)
TOMCAT_LIB=/opt/apache-tomcat-10.1.28/lib/servlet-api.jar

# Nettoyage
rm -rf out
mkdir -p out

# Compilation
javac -d out -cp $TOMCAT_LIB src/com/etu003184/servlet/FrontServlet.java

# Création du jar
jar cf my-framework.jar -C out .
echo "✅ JAR généré : my-framework.jar"
