package tqs.evsync.backend.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue("CONSUMER")
public class Consumer extends User {
	private double wallet = 0;
	


	@JsonIgnore
	@OneToMany(mappedBy = "consumer")
	private List<Reservation> reservations;

	public double getWallet() {
		return wallet;
	}
	public List<Reservation> getReservations() {
		return reservations;
	}

	public void setWallet(double wallet) {
		this.wallet = wallet;
	}
	public void setReservations(List<Reservation> reservations) {
		this.reservations = reservations;
	}

	
}
