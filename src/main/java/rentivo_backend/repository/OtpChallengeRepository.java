package rentivo_backend.repository; import org.springframework.data.jpa.repository.JpaRepository; import rentivo_backend.entity.OtpChallenge; import java.util.*;
public interface OtpChallengeRepository extends JpaRepository<OtpChallenge,Long>{ Optional<OtpChallenge> findTopByPhoneAndUsedFalseOrderByIdDesc(String phone); }
