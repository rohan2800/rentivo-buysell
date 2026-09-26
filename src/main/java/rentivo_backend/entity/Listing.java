package rentivo_backend.entity;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.LocalDateTime; import java.util.*;
@Entity @Table(name="listings")
public class Listing {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private String title;
 @ManyToOne(fetch=FetchType.EAGER,optional=false) @JoinColumn(name="category_id") private Category category;
 @Column(nullable=false,length=20) private String listingType;
 @Column(nullable=false,precision=14,scale=2) private BigDecimal price;
 @Column(nullable=false,length=20) private String priceUnit="TOTAL";
 @Column(nullable=false,length=3000) private String description;
 @Column(nullable=false,length=100) private String state;
 @Column(nullable=false,length=100) private String city;
 @Column(nullable=false,length=150) private String locality;
 private String pincode; private String address; private Double latitude; private Double longitude;
 @Column(nullable=false,length=15) private String contactPhone;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private Status status=Status.PENDING;
 @ManyToOne(fetch=FetchType.EAGER,optional=false) @JoinColumn(name="owner_id") private User owner;
 @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now(); private LocalDateTime updatedAt;
 @OneToMany(mappedBy="listing",cascade=CascadeType.ALL,orphanRemoval=true) private List<ListingImage> images=new ArrayList<>();
 @OneToMany(mappedBy="listing",cascade=CascadeType.ALL,orphanRemoval=true) private List<ListingFieldValue> fieldValues=new ArrayList<>();
 public enum Status { PENDING, APPROVED, REJECTED, DELETED }
 public Long getId(){return id;} public void setId(Long v){id=v;} public String getTitle(){return title;} public void setTitle(String v){title=v;} public Category getCategory(){return category;} public void setCategory(Category v){category=v;} public String getListingType(){return listingType;} public void setListingType(String v){listingType=v;} public BigDecimal getPrice(){return price;} public void setPrice(BigDecimal v){price=v;} public String getPriceUnit(){return priceUnit;} public void setPriceUnit(String v){priceUnit=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;} public String getState(){return state;} public void setState(String v){state=v;} public String getCity(){return city;} public void setCity(String v){city=v;} public String getLocality(){return locality;} public void setLocality(String v){locality=v;} public String getPincode(){return pincode;} public void setPincode(String v){pincode=v;} public String getAddress(){return address;} public void setAddress(String v){address=v;} public Double getLatitude(){return latitude;} public void setLatitude(Double v){latitude=v;} public Double getLongitude(){return longitude;} public void setLongitude(Double v){longitude=v;} public String getContactPhone(){return contactPhone;} public void setContactPhone(String v){contactPhone=v;} public Status getStatus(){return status;} public void setStatus(Status v){status=v;} public User getOwner(){return owner;} public void setOwner(User v){owner=v;} public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;} public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime v){updatedAt=v;} public List<ListingImage> getImages(){return images;} public List<ListingFieldValue> getFieldValues(){return fieldValues;}
}
