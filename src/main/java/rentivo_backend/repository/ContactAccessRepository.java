package rentivo_backend.repository; import org.springframework.data.jpa.repository.JpaRepository; import rentivo_backend.entity.ContactAccess; import java.util.*;
public interface ContactAccessRepository extends JpaRepository<ContactAccess,Long>{ Optional<ContactAccess> findByUserIdAndListingId(Long userId,Long listingId); long countByUserId(Long userId); }
