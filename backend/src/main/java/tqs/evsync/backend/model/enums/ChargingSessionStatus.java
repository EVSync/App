package tqs.evsync.backend.model.enums;

public enum ChargingSessionStatus {
    ACTIVE,     // Currently charging
    COMPLETED,  // Finished normally
    CANCELLED,  // Cancelled by user
    INTERRUPTED // Stopped unexpectedly (power outage for example)
}