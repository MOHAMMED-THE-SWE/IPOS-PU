package model;

public enum UserRole {
    ADMIN,
    MEMBER
    // Enum means: the role can ONLY be these values.
    // This avoids bugs from strings like "admin", "Admin", "admn", etc.
}
