package org.devbot.bookmymovie.core.security;

import java.util.EnumSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RolePermissions {

    private static final Set<Permission> USER_PERMISSIONS = Set.of(
            Permission.USER_PROFILE_READ,
            Permission.USER_PROFILE_UPDATE,
            Permission.MOVIE_READ,
            Permission.REVIEW_READ,
            Permission.REVIEW_CREATE,
            Permission.REVIEW_UPDATE_OWN,
            Permission.REVIEW_DELETE_OWN,
            Permission.THEATRE_READ,
            Permission.SCREEN_READ,
            Permission.SHOW_READ,
            Permission.SHOW_SEAT_READ,
            Permission.SEAT_LOCK_CREATE,
            Permission.SEAT_LOCK_DELETE,
            Permission.BOOKING_CREATE,
            Permission.BOOKING_READ_OWN,
            Permission.BOOKING_CANCEL_OWN,
            Permission.BOOKING_TICKET_READ,
            Permission.PAYMENT_CREATE,
            Permission.PAYMENT_READ_OWN
    );

    private static final Set<Permission> THEATER_ADMIN_PERMISSIONS = Set.of(
            Permission.SCREEN_CREATE,
            Permission.SCREEN_UPDATE,
            Permission.SCREEN_DELETE,
            Permission.SEAT_CREATE,
            Permission.SEAT_UPDATE,
            Permission.SEAT_DELETE,
            Permission.SHOW_CREATE,
            Permission.SHOW_UPDATE,
            Permission.SHOW_CANCEL,
            Permission.SHOW_READ_THEATRE,
            Permission.THEATRE_STATS_READ,
            Permission.SHOW_STATS_READ,
            Permission.THEATRE_MOVIE_STATS_READ
    );

    private static final Set<Permission> ADMIN_PERMISSIONS = Set.of(
            Permission.MOVIE_CREATE,
            Permission.MOVIE_UPDATE,
            Permission.MOVIE_DELETE,
            Permission.THEATRE_CREATE,
            Permission.THEATRE_UPDATE,
            Permission.THEATRE_DELETE,
            Permission.THEATRE_APPROVE,
            Permission.THEATRE_REJECT,
            Permission.PAYMENT_REFUND,
            Permission.ADMIN_STATS_READ,
            Permission.ADMIN_USER_READ,
            Permission.ADMIN_USER_STATUS_UPDATE,
            Permission.ADMIN_THEATRE_READ
    );

    public static Set<Permission> of(Role role) {
        return switch (role) {
            case USER -> USER_PERMISSIONS;
            case THEATER_ADMIN -> combined(USER_PERMISSIONS, THEATER_ADMIN_PERMISSIONS);
            case ADMIN -> combined(USER_PERMISSIONS, ADMIN_PERMISSIONS);
            case SUPER_ADMIN -> Set.copyOf(EnumSet.allOf(Permission.class));
        };
    }

    private static Set<Permission> combined(Set<Permission> left, Set<Permission> right) {
        EnumSet<Permission> permissions = EnumSet.copyOf(left);
        permissions.addAll(right);
        return Set.copyOf(permissions);
    }
}
