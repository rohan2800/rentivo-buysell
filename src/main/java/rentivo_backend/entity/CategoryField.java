package rentivo_backend.entity;
import jakarta.persistence.*;
@Entity @Table(name="category_fields")
public class CategoryField {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="category_id") private Category category;
 @Column(nullable=false) private String name;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private FieldType type=FieldType.TEXT;
 @Column(nullable=false) private boolean required=false;
 @Column(length=2000) private String optionsCsv;
 @Column(nullable=false) private int sortOrder=0;
 public enum FieldType { TEXT, NUMBER, DECIMAL, BOOLEAN, DATE, SELECT, MULTI_SELECT, TEXTAREA }
 public Long getId(){return id;} public Category getCategory(){return category;} public String getName(){return name;} public FieldType getType(){return type;} public boolean isRequired(){return required;} public String getOptionsCsv(){return optionsCsv;} public int getSortOrder(){return sortOrder;}
 public void setId(Long v){id=v;} public void setCategory(Category v){category=v;} public void setName(String v){name=v;} public void setType(FieldType v){type=v;} public void setRequired(boolean v){required=v;} public void setOptionsCsv(String v){optionsCsv=v;} public void setSortOrder(int v){sortOrder=v;}
}
