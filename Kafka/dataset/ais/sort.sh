#!/bin/bash

INPUT_FILE=./nari_dynamic.csv
OUTPUT_FILE=./nari_dynamic_sorted.csv

if [[ ! -f "$INPUT_FILE" ]]; then
    echo "Error: File '$INPUT_FILE' not found!"
    exit 1
fi

{ head -n 1 "$INPUT_FILE"; tail -n +2 "$INPUT_FILE" | sort -t"," -k9,9n; } > "$OUTPUT_FILE"

