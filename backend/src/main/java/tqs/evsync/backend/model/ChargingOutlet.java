package tqs.evsync.backend.model;

import jakarta.persistence.Entity;

@Entity
public class ChargingOutlet {
        private Long id;
        private double costPerHour;
	private int maxPower;

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
}