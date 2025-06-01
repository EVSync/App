package tqs.evsync.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import tqs.evsync.backend.model.enums.OutletStatus;
import jakarta.persistence.EnumType;

@Entity
public class ChargingOutlet {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double costPerHour;
    private int maxPower;
	private boolean isAvailable = true;

	@Enumerated(EnumType.STRING)
	private OutletStatus status;


    @ManyToOne
    @JoinColumn(name = "charging_station_id")
    private ChargingStation chargingStation;


	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}


	public double getCostPerHour() {
	    	return costPerHour;
	}
	public void setCostPerHour(double costPerHour) {
	    	this.costPerHour = costPerHour;
	}
	public int getMaxPower() {
	    	return maxPower;
	}
	public void setMaxPower(int maxPower) {
	    	this.maxPower = maxPower;
	}

	public ChargingStation getChargingStation() {
        return chargingStation;
    }

    public void setChargingStation(ChargingStation chargingStation) {
        this.chargingStation = chargingStation;
    }

	public boolean isAvailable() {
		return isAvailable;
	}
	public void setAvailable(boolean available) {
		this.isAvailable = available;

	public OutletStatus getStatus() {
		return status;
	}
	public void setStatus(OutletStatus status) {
		this.status = status;

	}
}