package tqs.evsync.backend.model;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tqs.evsync.backend.model.enums.OperatorType;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("OPERATOR")
public class Operator extends User {

	private OperatorType operatorType;
	

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

	public OperatorType getOperatorType() {
		return operatorType;
	}
	public void setOperatorType(OperatorType operatorType) {
		this.operatorType = operatorType;
	}
}
