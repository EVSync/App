package tqs.evsync.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "app_user")
@Inheritance(strategy = InheritanceType.JOINED) 
@DiscriminatorColumn(name = "user_type")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String password;
	private String email;
    
	public Long getId() {
		return id;
	}
	public String getPassword() {
		return password;
	}
	public String getEmail() {
		return email;
	}

	public void setId(Long id) {
		this.id = id;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
