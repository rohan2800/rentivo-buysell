package rentivo_backend.entity;
import jakarta.persistence.*; import java.util.*;
@Entity @Table(name="categories")
public class Category {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true) private String name;
 @Column(nullable=false) private boolean active=true;
 @Column(nullable=false) private boolean systemCategory=false;
 @OneToMany(mappedBy="category",cascade=CascadeType.ALL,orphanRemoval=true) private List<CategoryField> fields=new ArrayList<>();
 public Long getId(){return id;} public String getName(){return name;} public boolean isActive(){return active;} public boolean isSystemCategory(){return systemCategory;} public List<CategoryField> getFields(){return fields;}
 public void setId(Long v){id=v;} public void setName(String v){name=v;} public void setActive(boolean v){active=v;} public void setSystemCategory(boolean v){systemCategory=v;} public void setFields(List<CategoryField> v){fields=v;}
}
