package rentivo_backend.dto;

public record ContactAccessResponse(
        boolean unlocked,
        String ownerName,
        String ownerPhone,
        int contactsUsed,
        int contactsRemaining,
        String message
) {}
