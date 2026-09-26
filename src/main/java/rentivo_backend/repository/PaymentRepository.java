package rentivo_backend.repository; import org.springframework.data.jpa.repository.JpaRepository; import rentivo_backend.entity.Payment; import java.math.BigDecimal;
public interface PaymentRepository extends JpaRepository<Payment,Long>{ long countByStatus(String status); BigDecimal sumAmountByStatus(String status); }
