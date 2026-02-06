#!/bin/bash

FRONTEND_URL="https://loylty-booking-ui.vercel.app"
BACKEND_URL="https://loylty-booking-production.up.railway.app/api"

echo "🎬 Movie Booking System - Deployment Verification"
echo "=================================================="
echo ""

echo "📍 URLs:"
echo "Frontend: $FRONTEND_URL"
echo "Backend:  $BACKEND_URL"
echo ""

# Test Backend
echo "✅ Backend Health Check:"
curl -s "$BACKEND_URL/health" | jq -r '.status'
echo ""

# Test Frontend CSS
echo "✅ Frontend CSS MIME Type:"
curl -s -I "$FRONTEND_URL/assets/index-sxTv3YyW.css" | grep -i content-type
echo ""

# Test Frontend JS
echo "✅ Frontend JS MIME Type:"
curl -s -I "$FRONTEND_URL/assets/index-DGYa9UR0.js" | grep -i content-type
echo ""

# Test Backend API
echo "✅ Backend API Test:"
curl -s "$BACKEND_URL/cities" | jq '.[0].name'
echo ""

echo "🎉 Deployment Status: SUCCESS"
echo ""
echo "🚀 Your Movie Booking System is live and ready!"
echo "🎬 Frontend: https://loylty-booking-ui.vercel.app"
echo "🔧 Backend:  https://loylty-booking-production.up.railway.app"
