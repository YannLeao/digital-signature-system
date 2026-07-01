#!/usr/bin/env bash
set -euo pipefail

if [ "$#" -lt 1 ] || [ "$#" -gt 2 ]; then
  echo "Uso: $0 <pdf-assinado> [pdf-adulterado]" >&2
  exit 1
fi

input_path=$1
output_path=${2:-}

if [ ! -f "$input_path" ]; then
  echo "Arquivo nao encontrado: $input_path" >&2
  exit 1
fi

if [ -z "$output_path" ]; then
  directory=$(dirname -- "$input_path")
  filename=$(basename -- "$input_path")
  name=${filename%.*}
  output_path="$directory/$name-adulterado.pdf"
fi

cp -- "$input_path" "$output_path" 2>/dev/null || cp "$input_path" "$output_path"

timestamp=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
printf '\r\n%% demo-adulteracao: byte extra apos assinatura %s\r\n' "$timestamp" >> "$output_path"

echo "PDF adulterado gerado em: $output_path"
