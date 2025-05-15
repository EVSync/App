package tqs.evsync.backend.model;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED) 
@Table(name = "users")
public class User {
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    protected String password;
    protected String email;
    
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
