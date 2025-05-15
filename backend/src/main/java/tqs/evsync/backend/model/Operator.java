package tqs.evsync.backend.model;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("OPERATOR")
public class Operator extends User {

	@JsonIgnore
	@OneToMany(mappedBy = "operator")
	private List<ChargingStation> chargingStation;

	public List<ChargingStation> getChargingStation() {
		return chargingStation;
	}

	public void setChargingStation(List<ChargingStation> chargingStation) {
		this.chargingStation = chargingStation;
	}
	
	public void addChargingStation(ChargingStation chargingStation) {
		this.chargingStation.add(chargingStation);
		chargingStation.setOperator(this);
	}
}
