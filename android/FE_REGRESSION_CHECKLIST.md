# FE Regression Checklist

## Scope
- Login
- Field list
- Field detail
- Create booking
- My bookings

## Steps
1. Login
   - Open app -> Login screen
   - Enter demo credentials and login
   - Expect: navigates to Main screen

2. Field list
   - Main screen shows list of fields
   - Search and filter change results
   - Expect: loading state then success list or empty message

3. Field detail
   - Tap a field from list
   - Expect: loading state then detail content

4. Create booking
   - From field detail, tap Book Now
   - Fill date/time and confirm
   - Expect: loading state then success toast and return

5. My bookings
   - From detail or bottom nav, open My Bookings
   - Expect: loading state then list or empty message
   - Cancel a booking (if any)
   - Expect: success toast and list refresh

