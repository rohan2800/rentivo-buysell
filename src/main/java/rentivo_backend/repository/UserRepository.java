package rentivo_backend.repository; import org.springframework.data.jpa.repository.JpaRepository; import rentivo_backend.entity.User; import java.util.Optional;
public interface UserRepository extends JpaRepository<User,Long>{ Optional<User> findByPhone(String phone); long countByActiveTrue(); }
