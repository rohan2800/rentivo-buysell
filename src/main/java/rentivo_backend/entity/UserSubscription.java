package rentivo_backend.entity;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="user_subscriptions")
public class UserSubscription {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @ManyToOne(fetch=FetchType.EAGER,optional=false) @JoinColumn(name="user_id") private User user; @ManyToOne(fetch=FetchType.EAGER,optional=false) @JoinColumn(name="plan_id") private SubscriptionPlan plan; @Column(nullable=false) private LocalDateTime startAt; @Column(nullable=false) private LocalDateTime endAt; @Column(nullable=false) private Integer contactsUsed=0; @Enumerated(EnumType.STRING) @Column(nullable=false) private Status status=Status.ACTIVE;
 public enum Status { ACTIVE, EXPIRED, CANCELLED }
 public Long getId(){return id;} public void setId(Long v){id=v;} public User getUser(){return user;} public void setUser(User v){user=v;} public SubscriptionPlan getPlan(){return plan;} public void setPlan(SubscriptionPlan v){plan=v;} public LocalDateTime getStartAt(){return startAt;} public void setStartAt(LocalDateTime v){startAt=v;} public LocalDateTime getEndAt(){return endAt;} public void setEndAt(LocalDateTime v){endAt=v;} public Integer getContactsUsed(){return contactsUsed;} public void setContactsUsed(Integer v){contactsUsed=v;} public Status getStatus(){return status;} public void setStatus(Status v){status=v;}
}
