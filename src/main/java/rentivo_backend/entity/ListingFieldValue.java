package rentivo_backend.entity;
import jakarta.persistence.*;
@Entity @Table(name="listing_field_values", uniqueConstraints=@UniqueConstraint(name="uk_listing_field",columnNames={"listing_id","field_id"}))
public class ListingFieldValue {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="listing_id") private Listing listing;
 @ManyToOne(fetch=FetchType.EAGER,optional=false) @JoinColumn(name="field_id") private CategoryField field;
 @Column(nullable=false,length=4000) private String value;
 public Long getId(){return id;} public Listing getListing(){return listing;} public CategoryField getField(){return field;} public String getValue(){return value;} public void setId(Long v){id=v;} public void setListing(Listing v){listing=v;} public void setField(CategoryField v){field=v;} public void setValue(String v){value=v;}
}
