#!/bin/bash
# file: /bin/fn/jlogin.fn.sh

jlogin() {
  local username="$1"
  local password="$2"
  local targeturl="$3"

  # Wenn targeturl nicht übergeben wird, Standard aus BASE_URL verwenden
  if [[ -z "$targeturl" ]]; then
    targeturl="$BASE_URL"
  fi

  # 1. Login und Token speichern
  TOKEN=$(curl -s -X POST "$targeturl/auth/login" \
    -H "accept: */*" \
    -H "Content-Type: application/json" \
    -d '{"username":"'"$username"'","password":"'"$password"'"}')
  if [[ -z "$TOKEN" || "$TOKEN" == "null" ]]; then
    echo "Invalid token returned"
    exit 1
  fi
  export AUTH_TOKEN
  echo "$TOKEN"
}
