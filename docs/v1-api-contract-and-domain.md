# v1 API Contract and Domain Model

Frozen before implementation. This is the contract we plan to build against.

## Roles

```text
USER
THEATER_ADMIN
ADMIN
SUPER_ADMIN
```

Fine-grained permission names (`SHOW_CREATE`, etc.) derived from these roles: [v1-authorities.md](v1-authorities.md).

## Domain entities

```text
User
Movie
Review
Theatre
Screen
Seat
Show
Booking
BookingSeat
Payment
```

## Relationships

```text
Movie
  │
  └──< Show >── Theatre
             │
             └── Screen
                  │
                  └── Seat

User
  │
  ├──< Booking >── Show
  │                 │
  │                 └──< BookingSeat >── Seat
  │
  └──< Review >── Movie

Booking
   │
   └── Payment
```

---

# 1. Complete API List

## Authentication

### Register

```http
POST /api/v1/auth/register
```

### Login

```http
POST /api/v1/auth/login
```

### Refresh token

```http
POST /api/v1/auth/refresh
```

### Logout

```http
POST /api/v1/auth/logout
```

### Current user

```http
GET /api/v1/users/me
```

---

# 2. Movies

## USER

### Get movies

```http
GET /api/v1/movies
```

Query parameters:

```text
?page=0
&size=20
&sort=title,asc
```

Supported sorting:

```text
title,asc
title,desc
rating,desc
reviewCount,desc
releaseDate,desc
```

Filters:

```text
?genre=ACTION
&language=ENGLISH
&minRating=4
&releaseYear=2026
```

### Search movies

```http
GET /api/v1/movies/search?q=avengers
```

### Movie details

```http
GET /api/v1/movies/{movieId}
```

### Movies currently showing

```http
GET /api/v1/movies/now-showing
```

### Upcoming movies

```http
GET /api/v1/movies/upcoming
```

## ADMIN

### Create movie

```http
POST /api/v1/movies
```

### Update movie

```http
PUT /api/v1/movies/{movieId}
```

### Delete/deactivate movie

```http
DELETE /api/v1/movies/{movieId}
```

Implement this as a soft delete / deactivation internally.

---

# 3. Reviews

## USER

### Get movie reviews

```http
GET /api/v1/movies/{movieId}/reviews
```

Query:

```text
?page=0
&size=20
&sort=createdAt,desc
```

### Add review

```http
POST /api/v1/movies/{movieId}/reviews
```

### Update own review

```http
PUT /api/v1/reviews/{reviewId}
```

### Delete own review

```http
DELETE /api/v1/reviews/{reviewId}
```

### Get my reviews

```http
GET /api/v1/users/me/reviews
```

---

# 4. Theatres

## USER

### Get theatres

```http
GET /api/v1/theatres
```

Filters:

```text
?city=Bangalore
```

Eventually:

```text
?latitude=...
&longitude=...
&radius=10
```

### Theatre details

```http
GET /api/v1/theatres/{theatreId}
```

### Get theatre shows

```http
GET /api/v1/theatres/{theatreId}/shows
```

Query:

```text
?date=2026-08-20
```

## ADMIN

### Create theatre

```http
POST /api/v1/theatres
```

### Update theatre

```http
PUT /api/v1/theatres/{theatreId}
```

### Delete/deactivate theatre

```http
DELETE /api/v1/theatres/{theatreId}
```

### Approve theatre

```http
POST /api/v1/theatres/{theatreId}/approve
```

### Reject theatre

```http
POST /api/v1/theatres/{theatreId}/reject
```

---

# 5. Screens

A theatre contains screens.

```text
PVR Bangalore
   │
   ├── Screen 1
   ├── Screen 2
   └── Screen 3
```

## USER

### Get screens

```http
GET /api/v1/theatres/{theatreId}/screens
```

### Screen details

```http
GET /api/v1/screens/{screenId}
```

## THEATER_ADMIN

### Create screen

```http
POST /api/v1/theatres/{theatreId}/screens
```

### Update screen

```http
PUT /api/v1/screens/{screenId}
```

### Delete/deactivate screen

```http
DELETE /api/v1/screens/{screenId}
```

---

# 6. Seats

Seats belong to a **Screen**, not a Show.

```text
Screen 1
 ├── A1
 ├── A2
 ├── A3
 ├── B1
 └── B2
```

The show determines whether those seats are:

```text
AVAILABLE
LOCKED
BOOKED
```

## USER

### Get seats for a show

```http
GET /api/v1/shows/{showId}/seats
```

This should return the seat layout **and current availability**.

## THEATER_ADMIN

### Create seats

```http
POST /api/v1/screens/{screenId}/seats
```

Potentially bulk:

```http
POST /api/v1/screens/{screenId}/seats/bulk
```

### Update seat

```http
PUT /api/v1/seats/{seatId}
```

### Deactivate seat

```http
DELETE /api/v1/seats/{seatId}
```

---

# 7. Shows

This is the most important relationship in the system.

```text
Movie
 +
Theatre
 +
Screen
 +
Start Time
 +
End Time
 =
Show
```

Example:

```text
Avengers
PVR
Screen 3
19:30 - 22:30
```

## USER

### Get movie shows

```http
GET /api/v1/movies/{movieId}/shows
```

Query:

```text
?date=2026-08-20
&city=Bangalore
&theatreId=123
```

### Show details

```http
GET /api/v1/shows/{showId}
```

## THEATER_ADMIN

### Create show

```http
POST /api/v1/theatres/{theatreId}/shows
```

### Update show

```http
PUT /api/v1/shows/{showId}
```

### Cancel show

```http
POST /api/v1/shows/{showId}/cancel
```

### Get theatre's shows

```http
GET /api/v1/theatres/{theatreId}/shows
```

Filters:

```text
?date=2026-08-20
&status=ACTIVE
```

---

# 8. Seat Locking

This is where we will eventually introduce Redis.

### Lock seats

```http
POST /api/v1/shows/{showId}/seat-locks
```

Request:

```json
{
  "seatIds": [101, 102, 103]
}
```

Response:

```json
{
  "lockId": "abc123",
  "expiresAt": "2026-08-19T01:10:00Z"
}
```

### Release seats

```http
DELETE /api/v1/shows/{showId}/seat-locks/{lockId}
```

We may ultimately make seat locking an internal operation of booking rather than exposing both endpoints publicly. Conceptually, this is the API we will build around.

---

# 9. Bookings

## USER

### Create booking

```http
POST /api/v1/bookings
```

Request:

```json
{
  "showId": 500,
  "seatIds": [101, 102]
}
```

Response:

```json
{
  "bookingId": 9001,
  "status": "PENDING_PAYMENT",
  "amount": 700,
  "expiresAt": "2026-08-19T01:10:00Z"
}
```

### Get my bookings

```http
GET /api/v1/bookings
```

Query:

```text
?page=0
&size=20
&status=CONFIRMED
```

### Booking details

```http
GET /api/v1/bookings/{bookingId}
```

### Cancel booking

```http
POST /api/v1/bookings/{bookingId}/cancel
```

### Get booking ticket

```http
GET /api/v1/bookings/{bookingId}/ticket
```

Could return ticket / QR information.

---

# 10. Payments

### Create payment

```http
POST /api/v1/payments
```

Request:

```json
{
  "bookingId": 9001,
  "paymentMethod": "CARD"
}
```

### Payment details

```http
GET /api/v1/payments/{paymentId}
```

### Payment webhook

```http
POST /api/v1/payments/webhook
```

This is deliberately separate from the normal user API because the **payment provider** calls this endpoint.

### Refund

```http
POST /api/v1/payments/{paymentId}/refund
```

Authorization:

```text
ADMIN
```

or potentially an internal / admin payment service.

---

# 11. User APIs

### Get profile

```http
GET /api/v1/users/me
```

### Update profile

```http
PUT /api/v1/users/me
```

### Get booking history

```http
GET /api/v1/users/me/bookings
```

### Get reviews

```http
GET /api/v1/users/me/reviews
```

---

# 12. Theater Admin Dashboard

Do not overload `/movies/{id}` with admin statistics.

### Theatre statistics

```http
GET /api/v1/theatres/{theatreId}/statistics
```

Could return:

```text
totalShows
todayShows
totalBookings
todayBookings
ticketsSold
todayRevenue
occupancyRate
```

### Show statistics

```http
GET /api/v1/shows/{showId}/statistics
```

```text
totalSeats
bookedSeats
availableSeats
occupancyRate
revenue
```

### Movie performance in theatre

```http
GET /api/v1/theatres/{theatreId}/movies/{movieId}/statistics
```

```text
totalShows
totalTicketsSold
totalRevenue
averageOccupancy
```

---

# 13. ADMIN Dashboard

### Platform statistics

```http
GET /api/v1/admin/statistics
```

Could return:

```text
totalUsers
totalMovies
totalTheatres
totalBookings
totalTicketsSold
totalRevenue
```

### User management

```http
GET /api/v1/admin/users
GET /api/v1/admin/users/{userId}
PUT /api/v1/admin/users/{userId}/status
```

### Theatre management

```http
GET /api/v1/admin/theatres
GET /api/v1/admin/theatres/pending
```

---

# Data Models

These are the **JPA entities**, not DTOs.

## 1. User

```text
User
-----
id
name
email
passwordHash
phoneNumber
role
status
createdAt
updatedAt
```

Enums:

```text
Role:
USER
THEATER_ADMIN
ADMIN
SUPER_ADMIN

UserStatus:
ACTIVE
BLOCKED
```

## 2. Movie

```text
Movie
-----
id
title
description
thumbnailUrl
posterUrl
trailerUrl
durationMinutes
language
releaseDate
status
createdAt
updatedAt
```

Potentially:

```text
MovieStatus:
UPCOMING
NOW_SHOWING
ENDED
INACTIVE
```

Genres should be a separate relationship or enum collection rather than one comma-separated string.

## 3. Review

```text
Review
------
id
userId
movieId
rating
comment
createdAt
updatedAt
```

Constraints:

```text
rating = 1..5
```

One user can review a movie only once.

Database unique constraint:

```text
(user_id, movie_id)
```

## 4. Theatre

```text
Theatre
-------
id
name
description
address
city
state
postalCode
status
createdAt
updatedAt
```

Potential:

```text
TheatreStatus:
PENDING
APPROVED
REJECTED
SUSPENDED
```

## 5. Screen

```text
Screen
------
id
theatreId
name
screenNumber
capacity
status
```

## 6. Seat

```text
Seat
----
id
screenId
rowLabel
seatNumber
seatType
priceMultiplier
status
```

For example:

```text
SeatType:
REGULAR
PREMIUM
RECLINER
```

## 7. Show

```text
Show
----
id
movieId
theatreId
screenId
startTime
endTime
basePrice
status
createdAt
updatedAt
```

`Movie`, `Theatre`, and `Screen` should all be references.

## 8. Booking

```text
Booking
-------
id
userId
showId
bookingReference
status
totalAmount
createdAt
expiresAt
cancelledAt
```

Status:

```text
PENDING_PAYMENT
CONFIRMED
CANCELLED
PAYMENT_FAILED
EXPIRED
```

## 9. BookingSeat

Do not put `booking.seatIds` inside Booking. Use a separate entity.

```text
BookingSeat
-----------
id
bookingId
seatId
price
status
```

This is a many-to-many relationship with extra information such as **price at the time of booking**.

```text
Booking 100
 ├── Seat A1
 ├── Seat A2
 └── Seat A3
```

## 10. Payment

```text
Payment
-------
id
bookingId
transactionId
amount
currency
method
status
provider
createdAt
updatedAt
```

Status:

```text
PENDING
PROCESSING
SUCCESS
FAILED
REFUNDED
```

---

# DTOs

Do not expose entities directly from controllers. Use request / response DTOs.

## Authentication DTOs

```text
RegisterRequest
---------------
name
email
password
phoneNumber
```

```text
LoginRequest
------------
email
password
```

```text
AuthResponse
------------
accessToken
refreshToken
expiresIn
user
```

## Movie DTOs

### Create

```text
CreateMovieRequest
------------------
title
description
thumbnailUrl
posterUrl
trailerUrl
durationMinutes
language
releaseDate
genres
```

### Update

```text
UpdateMovieRequest
------------------
title
description
thumbnailUrl
posterUrl
trailerUrl
durationMinutes
language
releaseDate
genres
status
```

### List response

```text
MovieSummaryResponse
--------------------
id
title
thumbnailUrl
rating
reviewCount
releaseDate
```

### Details

```text
MovieDetailsResponse
--------------------
id
title
description
thumbnailUrl
posterUrl
trailerUrl
durationMinutes
language
releaseDate
genres
rating
reviewCount
```

## Review DTOs

### Create

```text
CreateReviewRequest
-------------------
rating
comment
```

### Update

```text
UpdateReviewRequest
-------------------
rating
comment
```

### Response

```text
ReviewResponse
--------------
id
userId
userName
rating
comment
createdAt
updatedAt
```

## Theatre DTOs

### Create

```text
CreateTheatreRequest
--------------------
name
description
address
city
state
postalCode
```

### Update

```text
UpdateTheatreRequest
--------------------
name
description
address
city
state
postalCode
```

### Response

```text
TheatreResponse
---------------
id
name
description
address
city
state
postalCode
status
```

## Screen DTOs

### Create

```text
CreateScreenRequest
-------------------
name
screenNumber
```

### Update

```text
UpdateScreenRequest
-------------------
name
screenNumber
status
```

### Response

```text
ScreenResponse
--------------
id
name
screenNumber
capacity
status
```

## Seat DTOs

### Create

```text
CreateSeatRequest
-----------------
rowLabel
seatNumber
seatType
priceMultiplier
```

### Bulk create

```text
CreateSeatsRequest
------------------
seats[]
```

Example:

```json
{
  "seats": [
    {
      "rowLabel": "A",
      "seatNumber": 1,
      "seatType": "REGULAR"
    },
    {
      "rowLabel": "A",
      "seatNumber": 2,
      "seatType": "REGULAR"
    }
  ]
}
```

### Response

```text
SeatResponse
------------
id
rowLabel
seatNumber
seatType
price
status
```

## Show DTOs

### Create

```text
CreateShowRequest
-----------------
movieId
screenId
startTime
endTime
basePrice
```

`theatreId` is not needed in the request body because it is already in:

```http
POST /theatres/{theatreId}/shows
```

### Update

```text
UpdateShowRequest
-----------------
screenId
startTime
endTime
basePrice
```

### Response

```text
ShowResponse
------------
id
movie
theatre
screen
startTime
endTime
basePrice
status
```

### Show summary

For movie browsing:

```text
ShowSummaryResponse
-------------------
id
theatreId
theatreName
screenId
screenName
startTime
endTime
basePrice
availableSeats
```

## Seat Availability DTO

```text
ShowSeatResponse
----------------
seatId
rowLabel
seatNumber
seatType
price
status
```

Example:

```json
{
  "seatId": 101,
  "rowLabel": "A",
  "seatNumber": 1,
  "seatType": "REGULAR",
  "price": 300,
  "status": "AVAILABLE"
}
```

## Booking DTOs

### Create

```text
CreateBookingRequest
--------------------
showId
seatIds[]
```

### Response

```text
BookingResponse
---------------
id
bookingReference
movie
theatre
screen
show
seats[]
totalAmount
status
createdAt
expiresAt
```

### Booking summary

For booking history:

```text
BookingSummaryResponse
----------------------
id
bookingReference
movieTitle
theatreName
showStartTime
totalAmount
status
createdAt
```

This prevents a giant payload for `GET /bookings`.

## Payment DTOs

### Create payment

```text
CreatePaymentRequest
--------------------
bookingId
paymentMethod
```

### Response

```text
PaymentResponse
---------------
id
bookingId
transactionId
amount
currency
paymentMethod
status
createdAt
```

### Webhook

```text
PaymentWebhookRequest
---------------------
transactionId
bookingReference
status
amount
signature
```

The signature is required so we can verify the webhook actually came from the payment provider.

## Statistics DTOs

### Show statistics

```text
ShowStatisticsResponse
----------------------
showId
totalSeats
availableSeats
bookedSeats
occupancyRate
totalRevenue
```

### Theatre statistics

```text
TheatreStatisticsResponse
-------------------------
theatreId
totalShows
totalBookings
ticketsSold
revenue
occupancyRate
```

### Movie statistics

```text
MovieStatisticsResponse
-----------------------
movieId
totalShows
totalBookings
ticketsSold
revenue
averageRating
reviewCount
```

---

# Final Domain Structure

```text
                       ┌──────────────┐
                       │     User     │
                       └──────┬───────┘
                              │
                 ┌────────────┼────────────┐
                 │                         │
                 ▼                         ▼
             Booking                    Review
                 │                         │
                 │                         ▼
                 │                       Movie
                 │                         │
                 ▼                         │
            BookingSeat                   │
                 │                         │
                 ▼                         │
                Seat                       │
                 │                         │
                 ▼                         │
              Screen ◄──── Theatre         │
                 │                         │
                 └──────────┐              │
                            ▼              │
                           Show ◄──────────┘
                            │
                            ▼
                         Payment
```

---

# Implementation Order

Do not create all 10 entities immediately.

## Entities

```text
1. User
2. Movie
3. Theatre
4. Screen
5. Seat
6. Show
7. Booking
8. BookingSeat
9. Review
10. Payment
```

## APIs

```text
Phase 1
────────────────────
Auth
Movies
Theatres
Screens
Seats
Shows

Phase 2
────────────────────
Bookings
BookingSeat
Seat availability

Phase 3
────────────────────
Concurrent booking
Transactions
Optimistic/Pessimistic locking

Phase 4
────────────────────
Redis
Seat locks
Expiration

Phase 5
────────────────────
Payments
Idempotency
Webhooks

Phase 6
────────────────────
Reviews
Statistics
Admin dashboard

Phase 7
────────────────────
Docker
Kafka
Monitoring
Load testing
AWS
```

## Next design step

Before creating entities, design the **PostgreSQL tables + relationships + primary keys + foreign keys + unique constraints + indexes**. Especially for `Show`, `Seat`, `BookingSeat`, and the constraints that prevent duplicate bookings. That is where the system becomes interesting rather than a set of Spring Data repositories.
