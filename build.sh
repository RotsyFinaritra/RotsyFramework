#!/bin/bash
set -e

# Emplacement du servlet-api.jar (Tomcat déjà installé)
TOMCAT_LIB=./lib/servlet-api.jar

# Nettoyage
rm -rf out
mkdir -p out

# Compilation de tous les fichiers Java dans src
echo "🔨 Compilation de tous les composants dans src..."
find src -name "*.java" -print | xargs javac -d out -cp $TOMCAT_LIB

# Création du jar
jar cf rotsy-framework.jar -C out .
echo "✅ JAR généré : rotsy-framework.jar"

# copie / remplacement du jar dans le dossier lib /home/finaritra/Documents/cours/s5/Mr Naina/framework/myFramework/test-RotsyFramework/lib
cp -f "rotsy-framework.jar" "/home/finaritra/Documents/cours/s5/Mr Naina/framework/myFramework/test-RotsyFramework/webapp/WEB-INF/lib/"