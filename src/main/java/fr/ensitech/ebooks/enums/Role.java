package fr.ensitech.ebooks.enums;

public enum Role {
    CLIENT("client"),
    LIBRARIAN("librarian"),  // Bibliothécaire
    ADMIN("admin");           // Administrateur

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Role fromString(String text) {
        for (Role role : Role.values()) {
            if (role.value.equalsIgnoreCase(text)) {
                return role;
            }
        }
        return CLIENT; // Par défaut
    }
}

