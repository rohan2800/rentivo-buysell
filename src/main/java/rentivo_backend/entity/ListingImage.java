package rentivo_backend.entity;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="listing_images")
public class ListingImage {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="listing_id") private Listing listing;
 @Column(nullable=false) private String fileName; @Column(nullable=false,length=1000) private String fileUrl; private int sortOrder; @Column(nullable=false) private LocalDateTime uploadedAt=LocalDateTime.now();
 public Long getId(){return id;} public Listing getListing(){return listing;} public String getFileName(){return fileName;} public String getFileUrl(){return fileUrl;} public int getSortOrder(){return sortOrder;} public void setId(Long v){id=v;} public void setListing(Listing v){listing=v;} public void setFileName(String v){fileName=v;} public void setFileUrl(String v){fileUrl=v;} public void setSortOrder(int v){sortOrder=v;} public LocalDateTime getUploadedAt(){return uploadedAt;}
}
