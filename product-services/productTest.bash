#!/usr/bin/env bash

HOST=localhost
PORT=8080

function assertCurl() {
  local expected=$1
  local cmd="$2 -w \"%{http_code}\""
  local out
  out=$(eval "$cmd")
  local code="${out: -3}"
  RESPONSE=''
  (( ${#out} > 3 )) && RESPONSE="${out:0:${#out}-3}"

  if [ "$code" = "$expected" ]; then
    echo "OK: HTTP $code"
  else
    echo "FAIL: expected HTTP $expected but got $code"
    echo "  CMD: $cmd"
    echo "  BODY: $RESPONSE"
    exit 1
  fi
}

function assertContains() {
  local needle=$1
  local haystack=$2
  if echo "$haystack" | grep -qF "$needle"; then
    echo "OK: found '$needle'"
  else
    echo "FAIL: did not find '$needle' in response"
    exit 1
  fi
}

echo
echo "=== CATALOG & WATCH TESTS ==="

# 1) POST /api/v1/catalogs → 201 and capture catalogId
CAT_TYPE="catalog-$(date +%s%N)"
catalogBody=$(cat <<EOF
{
  "type":"$CAT_TYPE",
  "description":"A test catalog"
}
EOF
)

echo
echo "1) POST /api/v1/catalogs"
assertCurl 201 "curl -s -X POST http://$HOST:$PORT/api/v1/catalogs \
  -H 'Content-Type: application/json' --data '$catalogBody'"
CATALOG_ID=$(echo "$RESPONSE" | jq -r '.catalogId')
echo "Catalog ID = $CATALOG_ID"

# 2) GET /api/v1/catalogs → 200 and our new type appears
echo
echo "2) GET /api/v1/catalogs"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/catalogs"
assertContains "\"catalogId\":\"$CATALOG_ID\"" "$RESPONSE"


# 3) POST /api/v1/catalogs/{id}/watches → 201 and capture watchId
WATCH_MODEL="watch-$(date +%s%N)"
watchBody=$(cat <<EOF
{
  "model":"$WATCH_MODEL",
  "material":"Steel",
  "usageType":"NEW",
  "quantity":1,
  "watchBrand":{"brandName":"TestBrand","brandCountry":"TestLand"},
  "price":{"msrp":100.00,"cost":80.00,"totalOptionsCost":0.00}
}
EOF
)

echo
echo "3) POST /api/v1/catalogs/$CATALOG_ID/watches"
assertCurl 201 "curl -s -X POST http://$HOST:$PORT/api/v1/catalogs/$CATALOG_ID/watches \
  -H 'Content-Type: application/json' --data '$watchBody'"
WATCH_ID=$(echo "$RESPONSE" | jq -r '.watchId')
echo "Watch ID = $WATCH_ID"

# 4) GET /api/v1/catalogs/{id}/watches → 200 and our watch appears
echo
echo "4) GET /api/v1/catalogs/$CATALOG_ID/watches"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/catalogs/$CATALOG_ID/watches"
assertContains "\"watchId\":\"$WATCH_ID\"" "$RESPONSE"


echo
echo "All catalog+watch tests passed!"
