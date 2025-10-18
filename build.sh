#!/bin/bash
set -e

# Emplacement du servlet-api.jar (Tomcat déjà installé)
TOMCAT_LIB=/opt/apache-tomcat-10.1.28/lib/servlet-api.jar

# Nettoyage
rm -rf out
mkdir -p out

# Compilation de tous les fichiers Java dans src
echo "🔨 Compilation de tous les composants dans src..."
find src -name "*.java" -print | xargs javac -d out -cp $TOMCAT_LIB

# Création du jar
jar cf my-framework.jar -C out .
echo "✅ JAR généré : my-framework.jar"
