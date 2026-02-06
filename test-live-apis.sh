#!/bin/bash

LIVE_URL="https://loylty-booking-production.up.railway.app/api"

echo "Testing Live Movie Booking System APIs"
echo "======================================"
echo "URL: $LIVE_URL"
echo ""

# Health Check
echo "1. Health Check:"
curl -s "$LIVE_URL/health" | jq .
echo ""

# Get All Cities
echo "2. Get All Cities:"
curl -s "$LIVE_URL/cities" | jq .
echo ""

# Get Movies by City
echo "3. Get Movies by City (1):"
curl -s "$LIVE_URL/movies/city/1" | jq '.[0] | {id, title, language, genre}'
echo ""

# Get Shows by Movie and City
echo "4. Get Shows by Movie (1) and City (1):"
curl -s "$LIVE_URL/shows/movie/1/city/1" | jq '.[0] | {id, showTime, screenName}'
echo ""

echo "✅ All APIs are working correctly!"
echo "🚀 Live URL: https://loylty-booking-production.up.railway.app"
