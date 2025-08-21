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
echo "=== SERVICE PLANS ==="

# 1) POST /api/v1/plans → 201 and capture planId
COV="coverage-$(date +%s%N)"
EXP_DATE=$(date -d "+1 year" +%Y-%m-%d)
planBody=$(cat <<EOF
{
  "coverageDetails":"$COV",
  "expirationDate":"$EXP_DATE"
}
EOF
)

echo
echo "1) POST /api/v1/plans"
assertCurl 201 "curl -s -X POST http://$HOST:$PORT/api/v1/plans \
  -H 'Content-Type: application/json' --data '$planBody'"
PLAN_ID=$(echo "$RESPONSE" | jq -r '.planId')
echo "Plan ID = $PLAN_ID"

# 2) GET /api/v1/plans → 200 and our new plan appears
echo
echo "2) GET /api/v1/plans"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/plans"
assertContains "\"planId\":\"$PLAN_ID\""      "$RESPONSE"


echo
echo "All service-plan tests passed!"
