# v1 Authorities

Derived from [v1-api-contract-and-domain.md](v1-api-contract-and-domain.md). The contract stores `User.role` (`USER`, `THEATER_ADMIN`, `ADMIN`, `SUPER_ADMIN`). These names are the permission bits you would use with `hasAuthority("SHOW_CREATE")` if roles are expanded into a permission table.

`ADMIN` is the platform operator (movies, theatres, users, refunds). `SUPER_ADMIN` has every permission. `THEATER_ADMIN` and `ADMIN` also get the USER set in code.

Own-resource suffixes (`_OWN`) are not extra role bits: the caller must have the authority **and** own the resource.

## Public (no user authority)

| Authority | API |
|-----------|-----|
| *(permitAll)* | `POST /api/v1/{web\|app}/auth/register`, `/signup`, `/login`, `/refresh` |
| *(permitAll + signature)* | `POST /api/v1/payments/webhook` |

## USER

| Authority | API |
|-----------|-----|
| `USER_PROFILE_READ` | `GET /api/v1/users/me` |
| `USER_PROFILE_UPDATE` | `PUT /api/v1/users/me` |
| `MOVIE_READ` | `GET /api/v1/movies`, `/movies/search`, `/movies/{id}`, `/movies/now-showing`, `/movies/upcoming` |
| `REVIEW_READ` | `GET /api/v1/movies/{id}/reviews`, `GET /api/v1/users/me/reviews` |
| `REVIEW_CREATE` | `POST /api/v1/movies/{id}/reviews` |
| `REVIEW_UPDATE_OWN` | `PUT /api/v1/reviews/{id}` |
| `REVIEW_DELETE_OWN` | `DELETE /api/v1/reviews/{id}` |
| `THEATRE_READ` | `GET /api/v1/theatres`, `GET /api/v1/theatres/{id}` |
| `SCREEN_READ` | `GET /api/v1/theatres/{id}/screens`, `GET /api/v1/screens/{id}` |
| `SHOW_READ` | `GET /api/v1/movies/{id}/shows`, `GET /api/v1/shows/{id}`, `GET /api/v1/theatres/{id}/shows` |
| `SHOW_SEAT_READ` | `GET /api/v1/shows/{id}/seats` |
| `SEAT_LOCK_CREATE` | `POST /api/v1/shows/{id}/seat-locks` (may stay internal to booking) |
| `SEAT_LOCK_DELETE` | `DELETE /api/v1/shows/{id}/seat-locks/{lockId}` |
| `BOOKING_CREATE` | `POST /api/v1/bookings` |
| `BOOKING_READ_OWN` | `GET /api/v1/bookings`, `GET /api/v1/bookings/{id}`, `GET /api/v1/users/me/bookings` |
| `BOOKING_CANCEL_OWN` | `POST /api/v1/bookings/{id}/cancel` |
| `BOOKING_TICKET_READ` | `GET /api/v1/bookings/{id}/ticket` |
| `PAYMENT_CREATE` | `POST /api/v1/payments` |
| `PAYMENT_READ_OWN` | `GET /api/v1/payments/{id}` |

## THEATER_ADMIN

| Authority | API |
|-----------|-----|
| `SCREEN_CREATE` | `POST /api/v1/theatres/{id}/screens` |
| `SCREEN_UPDATE` | `PUT /api/v1/screens/{id}` |
| `SCREEN_DELETE` | `DELETE /api/v1/screens/{id}` |
| `SEAT_CREATE` | `POST /api/v1/screens/{id}/seats`, `/seats/bulk` |
| `SEAT_UPDATE` | `PUT /api/v1/seats/{id}` |
| `SEAT_DELETE` | `DELETE /api/v1/seats/{id}` |
| `SHOW_CREATE` | `POST /api/v1/theatres/{id}/shows` |
| `SHOW_UPDATE` | `PUT /api/v1/shows/{id}` |
| `SHOW_CANCEL` | `POST /api/v1/shows/{id}/cancel` |
| `SHOW_READ_THEATRE` | `GET /api/v1/theatres/{id}/shows` (admin filters: `status`) |
| `THEATRE_STATS_READ` | `GET /api/v1/theatres/{id}/statistics` |
| `SHOW_STATS_READ` | `GET /api/v1/shows/{id}/statistics` |
| `THEATRE_MOVIE_STATS_READ` | `GET /api/v1/theatres/{id}/movies/{id}/statistics` |

## ADMIN

| Authority | API |
|-----------|-----|
| `MOVIE_CREATE` | `POST /api/v1/movies` |
| `MOVIE_UPDATE` | `PUT /api/v1/movies/{id}` |
| `MOVIE_DELETE` | `DELETE /api/v1/movies/{id}` |
| `THEATRE_CREATE` | `POST /api/v1/theatres` |
| `THEATRE_UPDATE` | `PUT /api/v1/theatres/{id}` |
| `THEATRE_DELETE` | `DELETE /api/v1/theatres/{id}` |
| `THEATRE_APPROVE` | `POST /api/v1/theatres/{id}/approve` |
| `THEATRE_REJECT` | `POST /api/v1/theatres/{id}/reject` |
| `PAYMENT_REFUND` | `POST /api/v1/payments/{id}/refund` |
| `ADMIN_STATS_READ` | `GET /api/v1/admin/statistics` |
| `ADMIN_USER_READ` | `GET /api/v1/admin/users`, `GET /api/v1/admin/users/{id}` |
| `ADMIN_USER_STATUS_UPDATE` | `PUT /api/v1/admin/users/{id}/status` |
| `ADMIN_SESSION_REVOKE` | `POST /api/v1/*/auth/sessions/revoke`, `/sessions/revoke-all` |
| `ADMIN_THEATRE_READ` | `GET /api/v1/admin/theatres`, `GET /api/v1/admin/theatres/pending` |

## Role → authority set

```text
USER
  USER_PROFILE_READ, USER_PROFILE_UPDATE
  MOVIE_READ
  REVIEW_READ, REVIEW_CREATE, REVIEW_UPDATE_OWN, REVIEW_DELETE_OWN
  THEATRE_READ, SCREEN_READ, SHOW_READ, SHOW_SEAT_READ
  SEAT_LOCK_CREATE, SEAT_LOCK_DELETE
  BOOKING_CREATE, BOOKING_READ_OWN, BOOKING_CANCEL_OWN, BOOKING_TICKET_READ
  PAYMENT_CREATE, PAYMENT_READ_OWN

THEATER_ADMIN
  SCREEN_CREATE, SCREEN_UPDATE, SCREEN_DELETE
  SEAT_CREATE, SEAT_UPDATE, SEAT_DELETE
  SHOW_CREATE, SHOW_UPDATE, SHOW_CANCEL, SHOW_READ_THEATRE
  THEATRE_STATS_READ, SHOW_STATS_READ, THEATRE_MOVIE_STATS_READ

ADMIN
  MOVIE_CREATE, MOVIE_UPDATE, MOVIE_DELETE
  THEATRE_CREATE, THEATRE_UPDATE, THEATRE_DELETE, THEATRE_APPROVE, THEATRE_REJECT
  PAYMENT_REFUND
  ADMIN_STATS_READ, ADMIN_USER_READ, ADMIN_USER_STATUS_UPDATE, ADMIN_SESSION_REVOKE, ADMIN_THEATRE_READ

SUPER_ADMIN
  all Permission values
```
