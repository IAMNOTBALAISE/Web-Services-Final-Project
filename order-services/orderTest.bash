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
  if echo "$RESPONSE" | grep -qF "$needle"; then
    echo "OK: found $needle"
  else
    echo "FAIL: did not find $needle in response"
    exit 1
  fi
}

function assertNotContains() {
  local needle=$1
  if echo "$RESPONSE" | grep -qF "$needle"; then
    echo "FAIL: unexpectedly found $needle in response"
    exit 1
  else
    echo "OK: did not find $needle"
  fi
}

echo
echo "=== ORDER SERVICE FULL CRUD TESTS ==="

# ─── 1) GET all orders (seed) ───────────────────────────────────────
echo
echo "1) GET /api/v1/orders"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/orders"
SEED_ORDER_ID=$(echo "$RESPONSE" | jq -r '.[0].orderId')
echo "Seed Order ID = '$SEED_ORDER_ID'"

# verify seed payload contains all fields
assertContains "\"orderId\":\"$SEED_ORDER_ID\""
assertContains '"customerId":"123e4567-e89b-12d3-a456-556642440000"'
assertContains '"customerFirstName":"John"'
assertContains '"customerLastName":"Smith"'
assertContains '"catalogId":"catalog-001"'
assertContains '"catalogType":"Smart Watch"'
assertContains '"catalogDescription":"Intelligent connected watches"'
assertContains '"watchId":"WCH-001"'
assertContains '"watchModel":"Apple Watch Ultra"'
assertContains '"watchMaterial":"Aluminum"'
assertContains '"servicePlanId":"SP-001"'
assertContains '"servicePlanCoverageDetails":"Full coverage for 1 year"'
assertContains '"servicePlanExpirationDate":"2026-03-16"'
assertContains '"orderName":"Seed-Order-001"'
assertContains '"salePrice":1200.0'
assertContains '"saleCurrency":"CAD"'
assertContains '"paymentCurrency":"CAD"'
assertContains '"orderStatus":"PURCHASE_CANCELED"'

# ─── 2) GET seed by ID ────────────────────────────────────────────
# URL-encode any spaces
ENC_SEED_ID="${SEED_ORDER_ID// /%20}"
echo
echo "2) GET /api/v1/orders/$SEED_ORDER_ID"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/orders/$ENC_SEED_ID"
# verify round-trip
assertContains "\"orderId\":\"$SEED_ORDER_ID\""
assertContains '"orderName":"Seed-Order-001"'

# ─── Discover downstream resources for creating a new one ───────────
echo
echo "3) GET /api/v1/customers → first customerId"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/customers"
CUSTOMER_ID=$(echo "$RESPONSE" | jq -r '.[0].customerId')
echo "Customer ID = $CUSTOMER_ID"

echo
echo "4) GET /api/v1/catalogs → first catalogId"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/catalogs"
CATALOG_ID=$(echo "$RESPONSE" | jq -r '.[0].catalogId')
echo "Catalog ID = $CATALOG_ID"

echo
echo "5) GET /api/v1/catalogs/$CATALOG_ID/watches → first watchId"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/catalogs/$CATALOG_ID/watches"
WATCH_ID=$(echo "$RESPONSE" | jq -r '.[0].watchId')
echo "Watch ID = $WATCH_ID"

echo
echo "6) GET /api/v1/plans → first planId"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/plans"
PLAN_ID=$(echo "$RESPONSE" | jq -r '.[0].planId')
echo "Plan ID = $PLAN_ID"

# ─── 3) POST a new order ────────────────────────────────────────────
NOW=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
ORDER_NAME="order-$(date +%s%N)"
orderBody=$(cat <<EOF
{
  "orderName":"$ORDER_NAME",
  "customerId":"$CUSTOMER_ID",
  "catalogId":"$CATALOG_ID",
  "watchId":"$WATCH_ID",
  "servicePlanId":"$PLAN_ID",
  "salePrice":150.0,
  "currency":"USD",
  "paymentCurrency":"USD",
  "orderDate":"$NOW",
  "orderStatus":"PURCHASE_COMPLETED"
}
EOF
)

echo
echo "7) POST /api/v1/orders"
assertCurl 201 "curl -s -X POST http://$HOST:$PORT/api/v1/orders \
  -H 'Content-Type: application/json' --data '$orderBody'"
NEW_ORDER_ID=$(echo "$RESPONSE" | jq -r '.orderId')
echo "New Order ID = '$NEW_ORDER_ID'"

# ─── 4) GET all → see the new order ────────────────────────────────
echo
echo "8) GET /api/v1/orders"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/orders"
assertContains "\"orderId\":\"$NEW_ORDER_ID\""

# ─── 5) GET new by ID ──────────────────────────────────────────────
ENC_NEW_ID="${NEW_ORDER_ID// /%20}"
echo
echo "9) GET /api/v1/orders/$NEW_ORDER_ID"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/orders/$ENC_NEW_ID"
assertContains "\"orderName\":\"$ORDER_NAME\""

# ─── 6) PUT new → cancel it ────────────────────────────────────────
updateBody=$(cat <<EOF
{
  "orderName":"$ORDER_NAME",
  "customerId":"$CUSTOMER_ID",
  "catalogId":"$CATALOG_ID",
  "watchId":"$WATCH_ID",
  "servicePlanId":"$PLAN_ID",
  "salePrice":150.0,
  "currency":"USD",
  "paymentCurrency":"USD",
  "orderDate":"$NOW",
  "orderStatus":"PURCHASE_CANCELED"
}
EOF
)

echo
echo "10) PUT /api/v1/orders/$NEW_ORDER_ID (cancel)"
assertCurl 200 "curl -s -X PUT http://$HOST:$PORT/api/v1/orders/$ENC_NEW_ID \
  -H 'Content-Type: application/json' --data '$updateBody'"
assertContains '"orderStatus":"PURCHASE_CANCELED"'

# ─── 7) DELETE the new order ───────────────────────────────────────
echo
echo "11) DELETE /api/v1/orders/$NEW_ORDER_ID"
assertCurl 204 "curl -s -X DELETE http://$HOST:$PORT/api/v1/orders/$ENC_NEW_ID"

# ─── 8) Final GET all → ensure it’s gone ──────────────────────────
echo
echo "12) GET /api/v1/orders (verify deletion)"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/orders"
assertNotContains "\"orderId\":\"$NEW_ORDER_ID\""

echo
echo " All ORDER CRUD tests passed!"
