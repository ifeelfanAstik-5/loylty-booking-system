#!/bin/bash

BASE_URL="http://localhost:8080/api"

echo "Testing Movie Booking System APIs"
echo "================================="

# Health Check
echo -e "\n1. Health Check:"
curl -s "$BASE_URL/health" | jq .

# Get All Cities
echo -e "\n2. Get All Cities:"
curl -s "$BASE_URL/cities" | jq .

# Get City by ID
echo -e "\n3. Get City by ID (1):"
curl -s "$BASE_URL/cities/1" | jq .

# Get Shows by City
echo -e "\n4. Get Shows by City (1):"
curl -s "$BASE_URL/cities/1/shows" | jq .

# Get Movies by City
echo -e "\n5. Get Movies by City (1):"
curl -s "$BASE_URL/movies/city/1" | jq .

# Search Movies
echo -e "\n6. Search Movies (Dunki):"
curl -s "$BASE_URL/movies/search?title=Dunki" | jq .

# Get Shows by Movie and City
echo -e "\n7. Get Shows by Movie (1) and City (1):"
curl -s "$BASE_URL/shows/movie/1/city/1" | jq .

# Get Shows Grouped by Cinema
echo -e "\n8. Get Shows Grouped by Cinema:"
curl -s "$BASE_URL/shows/movie/1/city/1/grouped" | jq .

# Get Show by ID
echo -e "\n9. Get Show by ID (1):"
curl -s "$BASE_URL/shows/1" | jq .

# Get Seats by Show
echo -e "\n10. Get Seats by Show (1):"
curl -s "$BASE_URL/shows/1/seats" | jq .

# Get Seat Layout
echo -e "\n11. Get Seat Layout for Show (1):"
curl -s "$BASE_URL/seats/show/1/layout" | jq .

echo -e "\nAPI Testing Complete!"
