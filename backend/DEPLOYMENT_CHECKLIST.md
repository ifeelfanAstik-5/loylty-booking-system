# Railway Deployment Checklist

## ✅ Deployment Status: IN PROGRESS

Railway is automatically deploying your latest commit: `e6da7ec changes to keys`

## 🔍 What to Monitor:

### 1. Railway Dashboard
- Check your Railway project dashboard
- Look for build logs and deployment status
- Monitor for any build errors

### 2. Expected Deployment Process:
1. **Build Phase**: Docker build using Maven
2. **Database Migration**: V2 migration will run automatically
3. **Health Check**: Railway will verify `/api/health` endpoint
4. **Service Start**: Application will start with production profile

## 🧪 Post-Deployment Verification:

### 1. Health Check
```bash
curl https://your-app-name.railway.app/api/health
```
Expected: `{"application":"Movie Booking Backend","status":"UP","timestamp":"..."}`

### 2. Test ShowSeat System
```bash
# Get seat layout
curl https://your-app-name.railway.app/api/shows/1/seats

# Test seat locking
curl -X POST -H "Content-Type: application/json" \
  -d '{"showId":1,"seatIds":[1,2,3],"userId":"test123"}' \
  https://your-app-name.railway.app/api/bookings/lock-seats
```

### 3. Database Verification
The V2 migration should have:
- ✅ Created `show_seats` table
- ✅ Populated with 1,800 seats (120 seats × 15 shows)
- ✅ Set up indexes and triggers

## 🚨 If Deployment Fails:

### Common Issues:
1. **Database Connection**: Check Railway environment variables
2. **Migration Conflicts**: Flyway might need baseline
3. **Memory Issues**: Railway might need more RAM

### Debug Commands:
```bash
# Check Railway logs
railway logs

# Check specific service
railway logs <service-name>

# Restart service
railway restart <service-name>
```

## 📊 Expected Results:

### New Features Deployed:
- ✅ Real-world theater seating logic
- ✅ ShowSeat entity with immutable seating plans
- ✅ Seat locking and booking system
- ✅ Premium seat pricing
- ✅ Automatic cleanup of expired locks

### API Endpoints Available:
- `GET /api/shows/{id}/seats` - Seat layout with status
- `POST /api/bookings/lock-seats` - Lock seats
- `POST /api/internal/shows/cleanup-expired-locks` - Cleanup

### Database Structure:
- `show_seats` table with 1,800 seats
- Proper indexes for performance
- Seat categories: REGULAR, PREMIUM
- Seat states: AVAILABLE, LOCKED, BOOKED

## 🎯 Success Indicators:

1. **Health Check Returns 200**
2. **Seat Layout Returns Data**
3. **Seat Locking Works**
4. **No Database Errors in Logs**
5. **Frontend Can Connect**

## 🔧 Environment Variables Needed:

Railway should automatically provide:
- `DATABASE_URL`
- `SPRING_DATASOURCE_USERNAME` 
- `SPRING_DATASOURCE_PASSWORD`
- `PORT`

## 📞 Support:

If deployment fails:
1. Check Railway dashboard for error logs
2. Verify database connection
3. Check migration status
4. Review build logs

The deployment should take 5-10 minutes to complete.
