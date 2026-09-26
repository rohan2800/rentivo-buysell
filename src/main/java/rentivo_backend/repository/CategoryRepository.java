package rentivo_backend.repository; import org.springframework.data.jpa.repository.JpaRepository; import rentivo_backend.entity.Category; import java.util.*;
public interface CategoryRepository extends JpaRepository<Category,Long>{ Optional<Category> findByNameIgnoreCase(String name); List<Category> findByActiveTrueOrderByNameAsc(); }
