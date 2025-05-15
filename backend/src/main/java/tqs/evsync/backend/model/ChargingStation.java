package tqs.evsync.backend.model;

import java.util.List;

import jakarta.persistence.*;

public class ChargingStation {
	private Long id;
	private Double latitude;
	private Double longitude;

	@ManyToOne()
	@JoinColumn(name = "operator_id")
	private Operator operator;
	
	@OneToMany()
	private List<ChargingOutlet> chargingOutlets;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	public Operator getOperator() {
		return operator;
	}
	public void setOperator(Operator operator) {
		this.operator = operator;
	}
}
