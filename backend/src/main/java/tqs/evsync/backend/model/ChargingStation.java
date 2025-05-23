package tqs.evsync.backend.model;

import java.util.ArrayList;
import java.util.List;


import tqs.evsync.backend.model.enums.ChargingStationStatus;

import jakarta.persistence.*;

@Entity
public class ChargingStation {


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Double latitude;
	private Double longitude;
	private ChargingStationStatus status;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id") 
    private List<ChargingOutlet> outlets = new ArrayList<>();

	@ManyToOne()
	@JoinColumn(name = "operator_id")
	private Operator operator;
	

	@OneToMany(mappedBy = "chargingStation", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ChargingOutlet> chargingOutlets= new ArrayList<>();


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
	public ChargingStationStatus getStatus() {
		return status;
	}
    public void setStatus(ChargingStationStatus status) {
        this.status = status;
    }
	public List<ChargingOutlet> getChargingOutlets() {
		return outlets;
	}
	public void setChargingOutlets(List<ChargingOutlet> chargingOutlets) {
		this.outlets = chargingOutlets;
	}
	public void addChargingOutlet(ChargingOutlet chargingOutlet) {
		this.outlets.add(chargingOutlet);
	}

	public void removeChargingOutlet(ChargingOutlet chargingOutlet) {
		this.outlets.remove(chargingOutlet);
	}
}
