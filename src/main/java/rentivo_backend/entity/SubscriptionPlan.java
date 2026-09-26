package rentivo_backend.entity;
import jakarta.persistence.*; import java.math.BigDecimal;
@Entity @Table(name="subscription_plans")
public class SubscriptionPlan {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(nullable=false,unique=true) private String name; @Column(nullable=false,precision=12,scale=2) private BigDecimal price; @Column(nullable=false) private Integer validityDays; @Column(nullable=false) private Integer contactLimit; @Column(length=1000) private String description; @Column(nullable=false) private boolean active=true;
 public Long getId(){return id;} public void setId(Long v){id=v;} public String getName(){return name;} public void setName(String v){name=v;} public BigDecimal getPrice(){return price;} public void setPrice(BigDecimal v){price=v;} public Integer getValidityDays(){return validityDays;} public void setValidityDays(Integer v){validityDays=v;} public Integer getContactLimit(){return contactLimit;} public void setContactLimit(Integer v){contactLimit=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;} public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
}
