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
echo "=== CUSTOMERS ==="

# 1) POST → create a new customer and capture the UUID
EMAIL="cust.$(date +%s%N)@example.com"
custBody=$(cat <<EOF
{
  "firstName":"Jane",
  "lastName":"Doe",
  "emailAddress":"$EMAIL",
  "streetAddress":"100 Broadway Blvd",
  "postalCode":"A1A 2B2",
  "city":"Testville",
  "province":"Ontario",
  "username":"jdoe",
  "password1":"pass123",
  "password2":"pass123",
  "phoneNumbers":[{"type":"MOBILE","number":"555-1234"}]
}
EOF
)

echo
echo "1) POST /api/v1/customers"
assertCurl 201 "curl -s -X POST http://$HOST:$PORT/api/v1/customers \
  -H 'Content-Type: application/json' --data '$custBody'"

# grab the real customerId (UUID)
CUSTOMER_ID=$(echo "$RESPONSE" | jq -r '.customerId')
echo "Customer ID = $CUSTOMER_ID"

# 2) GET all → 200 and our new customerId appears
echo
echo "2) GET /api/v1/customers"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/customers"
assertContains "\"customerId\":\"$CUSTOMER_ID\"" "$RESPONSE"

echo
echo "All customer tests passed!"
