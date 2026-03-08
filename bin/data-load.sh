#!/usr/bin/env bash

# Tool: data-load.sh
# Loads a given XML data file into Moqui via the command-line loader.

DATA_FILE="$1"

if [ -z "$DATA_FILE" ]; then
    echo "Usage: ./data-load <path_to_xml_file>"
    echo "Example: ./data-load runtime/component/aitree/data/agendaContainerData.xml"
    exit 1
fi

if [ ! -f "$DATA_FILE" ]; then
    echo "Error: Data file not found at $DATA_FILE"
    exit 1
fi

echo "Loading data from: $DATA_FILE..."

# Check if we should use gradlew or moqui.war directly
if [ -f "./moqui.war" ]; then
    java -jar moqui.war load data="$DATA_FILE"
elif [ -f "./gradlew" ]; then
    ./gradlew loadData -PdataFiles="$DATA_FILE"
else
    echo "Error: Neither moqui.war nor gradlew found in current directory."
    exit 1
fi

echo "Data load complete."
