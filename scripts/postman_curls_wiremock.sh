#!/bin/bash
# ============================================
# Script de cURL para probar WireMock
# ============================================
# Ejecuta múltiples requests para generar métricas en los dashboards

BASE_URL="http://localhost:8080/api/v1/events"

echo "============================================"
echo "Enviando eventos a Kafka para testing WireMock"
echo "============================================"
echo ""

# CLIENT001 - Éxito (200 OK)
echo "1. CLIENT001 - Éxito (200 OK)"
for i in {1..5}; do
  curl -X POST "$BASE_URL" \
    -H "Content-Type: application/json" \
    -d '{
      "client_id": "CLIENT001",
      "event_type": "credit_card_payment",
      "content": "{\"amount\": 150.00, \"currency\": \"USD\", \"transaction_id\": \"TXN-001-'$i'\"}"
    }'
  echo ""
  sleep 1
done

echo ""
echo "2. CLIENT002 - Error 500"
for i in {1..5}; do
  curl -X POST "$BASE_URL" \
    -H "Content-Type: application/json" \
    -d '{
      "client_id": "CLIENT002",
      "event_type": "credit_card_payment",
      "content": "{\"amount\": 200.00, \"currency\": \"USD\", \"transaction_id\": \"TXN-002-'$i'\"}"
    }'
  echo ""
  sleep 1
done

echo ""
echo "3. CLIENT003 - Timeout"
for i in {1..3}; do
  curl -X POST "$BASE_URL" \
    -H "Content-Type: application/json" \
    -d '{
      "client_id": "CLIENT003",
      "event_type": "debit_card_withdrawal",
      "content": "{\"amount\": 75.50, \"currency\": \"USD\", \"transaction_id\": \"TXN-003-'$i'\"}"
    }'
  echo ""
  sleep 1
done

echo ""
echo "4. CLIENT004 - Error 400"
for i in {1..5}; do
  curl -X POST "$BASE_URL" \
    -H "Content-Type: application/json" \
    -d '{
      "client_id": "CLIENT004",
      "event_type": "credit_card_payment",
      "content": "{\"amount\": 300.00, \"currency\": \"USD\", \"transaction_id\": \"TXN-004-'$i'\"}"
    }'
  echo ""
  sleep 1
done

echo ""
echo "============================================"
echo "Eventos enviados. Revisa las métricas en Grafana"
echo "============================================"

