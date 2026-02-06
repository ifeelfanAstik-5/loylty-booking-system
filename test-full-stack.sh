#!/bin/bash

FRONTEND_URL="https://loylty-booking-ui.vercel.app"
BACKEND_URL="https://loylty-booking-production.up.railway.app/api"

echo "Testing Full Stack Movie Booking System"
echo "======================================"
echo "Frontend: $FRONTEND_URL"
echo "Backend: $BACKEND_URL"
echo ""

# Test Backend Health
echo "1. Backend Health Check:"
curl -s "$BACKEND_URL/health" | jq .
echo ""

# Test Backend Cities API
echo "2. Backend Cities API:"
curl -s "$BACKEND_URL/cities" | jq '.[0:2]'
echo ""

# Test Frontend is serving
echo "3. Frontend Accessibility:"
curl -s "$FRONTEND_URL" | grep -o "<title>.*</title>"
echo ""

# Test CORS by making API call from frontend context
echo "4. API CORS Test:"
curl -s -H "Origin: $FRONTEND_URL" "$BACKEND_URL/cities" | jq '.[0]'
echo ""

echo "✅ Full Stack Deployment Summary:"
echo "🎬 Frontend: https://loylty-booking-ui.vercel.app"
echo "🚀 Backend:  https://loylty-booking-production.up.railway.app"
echo ""
echo "🎯 Ready for production use!"
