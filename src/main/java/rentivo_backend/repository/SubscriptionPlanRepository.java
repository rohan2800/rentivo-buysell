package rentivo_backend.repository; import org.springframework.data.jpa.repository.JpaRepository; import rentivo_backend.entity.SubscriptionPlan; import java.util.*;
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan,Long>{ List<SubscriptionPlan> findByActiveTrueOrderByPriceAsc(); }
