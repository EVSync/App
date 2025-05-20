package tqs.evsync.backend.model.enums;

public enum ReservationStatus {
	PENDING,	// The reservation is pending confirmation, needs to be payed
	CONFIRMED,	// The reservation is confirmed, the user has payed
	CANCELLED,	// The reservation is cancelled, the user has not payed or has cancelled
	COMPLETED	// The reservation is completed, the user has payed and the reservation is accepted
}
