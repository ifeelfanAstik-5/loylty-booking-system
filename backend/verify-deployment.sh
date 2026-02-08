#!/bin/bash

# Railway Deployment Verification Script
# Replace YOUR_RAILWAY_URL with your actual Railway app URL

RAILWAY_URL="https://loylty-booking-production.up.railway.app"

echo "🚀 Railway Deployment Verification"
echo "================================"
echo ""

# Check health endpoint
echo "1. Testing health endpoint..."
HEALTH_RESPONSE=$(curl -s -w "\n%{http_code}" "$RAILWAY_URL/api/health")
HTTP_CODE=$(echo "$HEALTH_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$HEALTH_RESPONSE" | head -n -1)

if [ "$HTTP_CODE" = "200" ]; then
    echo "✅ Health check passed"
    echo "Response: $RESPONSE_BODY"
else
    echo "❌ Health check failed (HTTP $HTTP_CODE)"
    echo "Response: $RESPONSE_BODY"
fi
echo ""

# Test seat layout
echo "2. Testing seat layout endpoint..."
SEAT_RESPONSE=$(curl -s -w "\n%{http_code}" "$RAILWAY_URL/api/shows/1/seats")
SEAT_HTTP_CODE=$(echo "$SEAT_RESPONSE" | tail -n1)
SEAT_BODY=$(echo "$SEAT_RESPONSE" | head -n -1 | head -c 200)

if [ "$SEAT_HTTP_CODE" = "200" ]; then
    echo "✅ Seat layout endpoint working"
    echo "Sample response: $SEAT_BODY..."
else
    echo "❌ Seat layout endpoint failed (HTTP $SEAT_HTTP_CODE)"
    echo "Response: $SEAT_BODY"
fi
echo ""

# Test seat locking
echo "3. Testing seat locking..."
LOCK_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST \
  -H "Content-Type: application/json" \
  -d '{"showId":1,"seatIds":[1,2,3],"userId":"test123"}' \
  "$RAILWAY_URL/api/bookings/lock-seats")
LOCK_HTTP_CODE=$(echo "$LOCK_RESPONSE" | tail -n1)
LOCK_BODY=$(echo "$LOCK_RESPONSE" | head -n -1)

if [ "$LOCK_HTTP_CODE" = "200" ]; then
    echo "✅ Seat locking working"
    echo "Response: $LOCK_BODY"
else
    echo "❌ Seat locking failed (HTTP $LOCK_HTTP_CODE)"
    echo "Response: $LOCK_BODY"
fi
echo ""

echo "🎯 Deployment Summary:"
echo "=================="
echo "Health Check: $([ "$HTTP_CODE" = "200" ] && echo "✅ PASS" || echo "❌ FAIL")"
echo "Seat Layout: $([ "$SEAT_HTTP_CODE" = "200" ] && echo "✅ PASS" || echo "❌ FAIL")"
echo "Seat Locking: $([ "$LOCK_HTTP_CODE" = "200" ] && echo "✅ PASS" || echo "❌ FAIL")"

if [ "$HTTP_CODE" = "200" ] && [ "$SEAT_HTTP_CODE" = "200" ] && [ "$LOCK_HTTP_CODE" = "200" ]; then
    echo ""
    echo "🎉 DEPLOYMENT SUCCESSFUL! Your new ShowSeat system is live!"
else
    echo ""
    echo "⚠️  Deployment needs attention. Check Railway logs for details."
fi

echo ""
echo "📝 Next Steps:"
echo "- Update your frontend to use the new seat layout API"
echo "- Monitor the cleanup of expired locks"
echo "- Check Railway dashboard for any ongoing issues"
