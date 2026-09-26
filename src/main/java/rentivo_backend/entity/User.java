package rentivo_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="users")
public class User {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false, unique=true, length=15) private String phone;
 @Column(nullable=false) private String name;
 @Column(nullable=false) private boolean phoneVerified;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private Role role=Role.USER;
 @Column(nullable=false) private boolean active=true;
 @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
 public enum Role { USER, PROVIDER, ADMIN }
 public Long getId(){return id;} public void setId(Long v){id=v;}
 public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
 public String getName(){return name;} public void setName(String v){name=v;}
 public boolean isPhoneVerified(){return phoneVerified;} public void setPhoneVerified(boolean v){phoneVerified=v;}
 public Role getRole(){return role;} public void setRole(Role v){role=v;}
 public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
 public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
