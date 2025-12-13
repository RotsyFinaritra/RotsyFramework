#!/bin/bash

# Script pour télécharger Jackson
# Usage: 
#   ./download-jackson.sh /chemin/vers/WEB-INF/lib    (recommandé)
#   ./download-jackson.sh /chemin/vers/lib            (si lib à la racine)

if [ $# -eq 0 ]; then
    echo "Usage: $0 <chemin-vers-dossier-lib>"
    echo "Exemples:"
    echo "  $0 /path/to/your-project/WEB-INF/lib  (recommandé)"
    echo "  $0 /path/to/your-project/lib          (alternatif)"
    exit 1
fi

LIB_DIR=$1
JACKSON_VERSION="2.16.1"

# Créer le dossier s'il n'existe pas
mkdir -p "$LIB_DIR"

echo "Téléchargement de Jackson $JACKSON_VERSION dans $LIB_DIR"

# Jackson Core
echo "Téléchargement de jackson-core..."
wget -O "$LIB_DIR/jackson-core-$JACKSON_VERSION.jar" \
     "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/$JACKSON_VERSION/jackson-core-$JACKSON_VERSION.jar"

# Jackson Annotations
echo "Téléchargement de jackson-annotations..."
wget -O "$LIB_DIR/jackson-annotations-$JACKSON_VERSION.jar" \
     "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/$JACKSON_VERSION/jackson-annotations-$JACKSON_VERSION.jar"

# Jackson Databind
echo "Téléchargement de jackson-databind..."
wget -O "$LIB_DIR/jackson-databind-$JACKSON_VERSION.jar" \
     "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/$JACKSON_VERSION/jackson-databind-$JACKSON_VERSION.jar"

echo "Jackson téléchargé avec succès !"
echo "Les JARs sont maintenant dans : $LIB_DIR"