package tqs.evsync.backend.model;

import jakarta.persistence.*;

@Entity
public class Reservation {
        private Long id;
        private String startTime;
        private Double duration;
	private ReservationStatus status;

        @ManyToOne
        @JoinColumn(name = "consumer_id")
        private Consumer consumer;

        public Long getId() {
        	return id;
        }
        public String getStartTime() {
            	return startTime;
        }
        public Double getDuration() {
            	return duration;
        }
        public Consumer getConsumer() {
            	return consumer;
        }

        public void setId(Long id) {
            	this.id = id;
        }
        public void setStartTime(String startTime) {
            	this.startTime = startTime;
        }
        public void setDuration(Double duration) {
            	this.duration = duration;
        }
        public void setConsumer(Consumer consumer) {
            	this.consumer = consumer;
        }
}
