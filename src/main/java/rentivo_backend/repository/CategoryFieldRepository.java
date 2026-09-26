package rentivo_backend.repository; import org.springframework.data.jpa.repository.JpaRepository; import rentivo_backend.entity.CategoryField; import java.util.*;
public interface CategoryFieldRepository extends JpaRepository<CategoryField,Long>{ List<CategoryField> findByCategoryIdOrderBySortOrderAsc(Long categoryId); }
