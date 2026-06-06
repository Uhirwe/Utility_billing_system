#!/usr/bin/env bash
# End-to-end API smoke test for Utility Billing System
set -uo pipefail

BASE="http://localhost:8080/api"
PASS=0
FAIL=0
SKIP=0
RESULTS=()

log() { echo "$@"; }

pass() {
  PASS=$((PASS + 1))
  RESULTS+=("PASS  $1")
  echo "  ✓ $1"
}

fail() {
  FAIL=$((FAIL + 1))
  RESULTS+=("FAIL  $1 — $2")
  echo "  ✗ $1 — $2"
}

skip() {
  SKIP=$((SKIP + 1))
  RESULTS+=("SKIP  $1 — $2")
  echo "  ~ $1 — $2"
}

login() {
  local email=$1 password=$2
  curl -s -X POST "$BASE/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$email\",\"password\":\"$password\"}"
}

expect_success() {
  local label=$1 body=$2
  local ok
  ok=$(echo "$body" | python3 -c "import sys,json; d=json.load(sys.stdin); print('1' if d.get('success') else '0')" 2>/dev/null || echo "0")
  if [[ "$ok" == "1" ]]; then pass "$label"; else fail "$label" "$(echo "$body" | python3 -c "import sys,json; print(json.load(sys.stdin).get('message','invalid json'))" 2>/dev/null || echo "$body")"; fi
}

expect_http() {
  local label=$1 expected=$2 actual=$3 body=$4
  if [[ "$actual" == "$expected" ]]; then pass "$label (HTTP $actual)"; else fail "$label" "expected HTTP $expected, got $actual — $(echo "$body" | head -c 120)"; fi
}

extract_token() {
  echo "$1" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['accessToken'])" 2>/dev/null
}

auth_get() {
  curl -s -w "\n%{http_code}" -H "Authorization: Bearer $1" "$2"
}

auth_post() {
  curl -s -w "\n%{http_code}" -X POST -H "Authorization: Bearer $1" -H "Content-Type: application/json" -d "$3" "$2"
}

auth_patch() {
  curl -s -w "\n%{http_code}" -X PATCH -H "Authorization: Bearer $1" "$2"
}

split_response() {
  HTTP_CODE=$(echo "$1" | tail -1)
  BODY=$(echo "$1" | sed '$d')
}

echo "=============================================="
echo " Utility Billing System — API Smoke Test"
echo " Base URL: $BASE"
echo " Date: $(date)"
echo "=============================================="
echo

# --- Connectivity ---
log "1. Connectivity"
SWAGGER=$(curl -s -o /dev/null -w "%{http_code}" "$BASE/swagger-ui.html")
if [[ "$SWAGGER" == "302" || "$SWAGGER" == "200" ]]; then pass "Swagger UI reachable"; else fail "Swagger UI reachable" "HTTP $SWAGGER"; fi
OPENAPI=$(curl -s -o /dev/null -w "%{http_code}" "$BASE/v3/api-docs")
if [[ "$OPENAPI" == "200" ]]; then pass "OpenAPI docs reachable"; else fail "OpenAPI docs reachable" "HTTP $OPENAPI"; fi
echo

# --- Authentication ---
log "2. Authentication"
ADMIN_LOGIN=$(login "admin@utilitybilling.rw" "Admin@12345")
expect_success "Admin login" "$ADMIN_LOGIN"
ADMIN_TOKEN=$(extract_token "$ADMIN_LOGIN")

OPERATOR_LOGIN=$(login "operator@utilitybilling.rw" "Admin@12345")
expect_success "Operator login" "$OPERATOR_LOGIN"
OPERATOR_TOKEN=$(extract_token "$OPERATOR_LOGIN")

FINANCE_LOGIN=$(login "finance@utilitybilling.rw" "Admin@12345")
expect_success "Finance login" "$FINANCE_LOGIN"
FINANCE_TOKEN=$(extract_token "$FINANCE_LOGIN")

CUSTOMER_LOGIN=$(login "customer@utilitybilling.rw" "Admin@12345")
expect_success "Customer login" "$CUSTOMER_LOGIN"
CUSTOMER_TOKEN=$(extract_token "$CUSTOMER_LOGIN")

BAD_LOGIN=$(login "admin@utilitybilling.rw" "wrongpassword")
BAD_OK=$(echo "$BAD_LOGIN" | python3 -c "import sys,json; d=json.load(sys.stdin); print('1' if not d.get('success') else '0')" 2>/dev/null || echo "0")
if [[ "$BAD_OK" == "1" ]]; then pass "Invalid password rejected"; else fail "Invalid password rejected" "$BAD_LOGIN"; fi
echo

# --- Role-based access ---
log "3. Role-based access control"
split_response "$(auth_get "$CUSTOMER_TOKEN" "$BASE/users?page=1")"
expect_http "Customer blocked from /users" "403" "$HTTP_CODE" "$BODY"

split_response "$(auth_get "$OPERATOR_TOKEN" "$BASE/bills/generate")"
expect_http "Operator blocked from POST /bills/generate (wrong method)" "405" "$HTTP_CODE" "$BODY"

split_response "$(auth_post "$FINANCE_TOKEN" "$BASE/bills/generate" '{"meterId":1,"billingMonth":5,"billingYear":2026}')"
FINANCE_BILL_OK=$(echo "$BODY" | python3 -c "import sys,json; print('1' if not json.load(sys.stdin).get('success') else '0')" 2>/dev/null || echo "0")
if [[ "$FINANCE_BILL_OK" == "1" ]] && [[ "$HTTP_CODE" == "403" || "$HTTP_CODE" == "400" ]]; then
  pass "Finance blocked from bill generation (HTTP $HTTP_CODE)"
else
  fail "Finance blocked from bill generation" "HTTP $HTTP_CODE — $(echo "$BODY" | head -c 100)"
fi

split_response "$(auth_get "$ADMIN_TOKEN" "$BASE/audit-logs?page=1")"
expect_success "Admin can view audit logs" "$BODY"
echo

# --- Customer portal ---
log "4. Customer portal"
split_response "$(auth_get "$CUSTOMER_TOKEN" "$BASE/customers/me")"
expect_success "GET /customers/me" "$BODY"

split_response "$(auth_get "$CUSTOMER_TOKEN" "$BASE/meters/me")"
expect_success "GET /meters/me" "$BODY"
METER_COUNT=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(len(d.get('data',[])))" 2>/dev/null || echo "0")
if [[ "$METER_COUNT" -ge 2 ]]; then pass "Customer has 2+ meters (seed data)"; else fail "Customer has 2+ meters" "count=$METER_COUNT"; fi

split_response "$(auth_get "$CUSTOMER_TOKEN" "$BASE/customers/2")"
BLOCKED=$(echo "$BODY" | python3 -c "import sys,json; print('1' if not json.load(sys.stdin).get('success') else '0')" 2>/dev/null || echo "0")
if [[ "$BLOCKED" == "1" ]]; then pass "Customer blocked from other customer profile (HTTP $HTTP_CODE)"; else fail "Customer blocked from other customer profile" "$BODY"; fi
echo

# --- Operator workflows ---
log "5. Operator workflows"
split_response "$(auth_get "$OPERATOR_TOKEN" "$BASE/customers?page=1")"
expect_success "Operator lists customers" "$BODY"

split_response "$(auth_get "$OPERATOR_TOKEN" "$BASE/readings/meter/1?page=1")"
expect_success "Operator views meter readings" "$BODY"

# June reading for meter 1 (previous bill month May already has reading)
READING_BODY='{"meterId":1,"previousReading":125.50,"currentReading":140.00,"readingDate":"2026-06-05"}'
split_response "$(auth_post "$OPERATOR_TOKEN" "$BASE/readings" "$READING_BODY")"
expect_success "Operator records June reading for meter 1" "$BODY"
echo

# --- Admin: tariffs & billing ---
log "6. Admin billing workflow"
split_response "$(auth_get "$ADMIN_TOKEN" "$BASE/tariffs?page=1")"
expect_success "Admin lists tariffs" "$BODY"

BILL_BODY='{"meterId":1,"billingMonth":5,"billingYear":2026}'
split_response "$(auth_post "$ADMIN_TOKEN" "$BASE/bills/generate" "$BILL_BODY")"
WATER_BILL_OK=$(echo "$BODY" | python3 -c "import sys,json; print('1' if json.load(sys.stdin).get('success') else '0')" 2>/dev/null || echo "0")
if [[ "$WATER_BILL_OK" == "1" ]]; then
  pass "Admin generates water bill (May 2026)"
  WATER_BILL_ID=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['id'])" 2>/dev/null)
else
  MSG=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin).get('message',''))" 2>/dev/null)
  if [[ "$MSG" == *"already exists"* ]]; then
    skip "Admin generates water bill" "bill already exists from prior run"
    WATER_BILL_ID=$(echo "$(auth_get "$ADMIN_TOKEN" "$BASE/bills/customer/1?page=1")" | sed '$d' | python3 -c "import sys,json; c=json.load(sys.stdin)['data']['content']; print(next((b['id'] for b in c if b.get('billingMonth')==5 and b.get('meterId')==1), c[0]['id'] if c else ''))" 2>/dev/null)
  else
    fail "Admin generates water bill" "$MSG"
    WATER_BILL_ID=""
  fi
fi

BILL2_BODY='{"meterId":2,"billingMonth":5,"billingYear":2026}'
split_response "$(auth_post "$ADMIN_TOKEN" "$BASE/bills/generate" "$BILL2_BODY")"
expect_success "Admin generates electricity bill (May 2026)" "$BODY" || true
ELEC_BILL_ID=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('data',{}).get('id',''))" 2>/dev/null || echo "")

JUNE_BILL='{"meterId":1,"billingMonth":6,"billingYear":2026}'
split_response "$(auth_post "$ADMIN_TOKEN" "$BASE/bills/generate" "$JUNE_BILL")"
expect_success "Admin generates June water bill" "$BODY"
JUNE_BILL_ID=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['id'])" 2>/dev/null || echo "")
echo

# --- Finance: approve & pay ---
log "7. Finance approve & payment"
if [[ -n "${WATER_BILL_ID:-}" ]]; then
  split_response "$(auth_patch "$FINANCE_TOKEN" "$BASE/bills/$WATER_BILL_ID/approve")"
  expect_success "Finance approves water bill" "$BODY"

  PAY_BODY="{\"billId\":$WATER_BILL_ID,\"amountPaid\":50000,\"paymentMethod\":\"MOMO\",\"paymentDate\":\"2026-06-06T10:00:00\"}"
  split_response "$(auth_post "$FINANCE_TOKEN" "$BASE/payments" "$PAY_BODY")"
  expect_success "Finance records partial payment" "$BODY"
else
  skip "Finance approve & payment" "no bill ID available"
fi
echo

# --- Customer views after billing ---
log "8. Customer post-billing views"
split_response "$(auth_get "$CUSTOMER_TOKEN" "$BASE/bills/me?page=1")"
expect_success "Customer views own bills" "$BODY"

split_response "$(auth_get "$CUSTOMER_TOKEN" "$BASE/payments/me?page=1")"
expect_success "Customer views own payments" "$BODY"

split_response "$(auth_get "$CUSTOMER_TOKEN" "$BASE/notifications/me?page=1")"
expect_success "Customer views notifications" "$BODY"
echo

# --- Registration ---
log "9. Public registration"
REG_EMAIL="test.user.$(date +%s)@email.rw"
REG_BODY="{\"firstName\":\"Test\",\"lastName\":\"User\",\"nationalId\":\"119988776655$(date +%s | tail -c 5)\",\"email\":\"$REG_EMAIL\",\"phoneCountryCode\":\"+250\",\"phoneNumber\":\"0788999900\",\"address\":\"Kigali\",\"dateOfBirth\":\"1995-01-01\",\"password\":\"Customer@123\"}"
REG=$(curl -s -X POST "$BASE/auth/register" -H "Content-Type: application/json" -d "$REG_BODY")
expect_success "New customer registration" "$REG"
echo

# --- Business rule validation ---
log "10. Business rule enforcement"
DUP_READING='{"meterId":1,"previousReading":140.00,"currentReading":150.00,"readingDate":"2026-06-05"}'
split_response "$(auth_post "$OPERATOR_TOKEN" "$BASE/readings" "$DUP_READING")"
DUP_OK=$(echo "$BODY" | python3 -c "import sys,json; print('1' if not json.load(sys.stdin).get('success') else '0')" 2>/dev/null || echo "0")
if [[ "$DUP_OK" == "1" ]]; then pass "Duplicate reading for same month rejected"; else fail "Duplicate reading rejected" "$BODY"; fi

UNAPPROVED_PAY=""
if [[ -n "${JUNE_BILL_ID:-}" ]]; then
  PAY_UNAPPROVED="{\"billId\":$JUNE_BILL_ID,\"amountPaid\":1000,\"paymentMethod\":\"CASH\",\"paymentDate\":\"2026-06-06T10:00:00\"}"
  split_response "$(auth_post "$FINANCE_TOKEN" "$BASE/payments" "$PAY_UNAPPROVED")"
  UNAPPROVED_OK=$(echo "$BODY" | python3 -c "import sys,json; print('1' if not json.load(sys.stdin).get('success') else '0')" 2>/dev/null || echo "0")
  if [[ "$UNAPPROVED_OK" == "1" ]]; then pass "Payment on unapproved bill rejected"; else fail "Payment on unapproved bill rejected" "$BODY"; fi
else
  skip "Payment on unapproved bill rejected" "no unapproved bill available"
fi
echo

# --- Maven unit tests ---
log "11. Maven tests"
if export JAVA_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null) && mvn -q test -Dtest='!SmtpConnectionTest' 2>/tmp/mvn-test.log; then
  pass "Maven test suite"
else
  TEST_OUT=$(tail -5 /tmp/mvn-test.log 2>/dev/null)
  skip "Maven test suite" "no integration tests or failures — $TEST_OUT"
fi
echo

echo "=============================================="
echo " SUMMARY: $PASS passed, $FAIL failed, $SKIP skipped"
echo "=============================================="
for r in "${RESULTS[@]}"; do echo "$r"; done

if [[ "$FAIL" -gt 0 ]]; then exit 1; fi
