package com.rentivo.backend.user;

import com.rentivo.backend.common.exception.ConflictException;
import com.rentivo.backend.common.exception.NotFoundException;
import com.rentivo.backend.common.web.PageResponse;
import com.rentivo.backend.common.web.Paging;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class UserAdminService {

    public record UserView(Long id, String name, String phone, Role role, boolean active,
                           boolean phoneVerified, Instant createdAt) {
        static UserView from(User u) {
            return new UserView(u.getId(), u.getName(), u.getPhone(), u.getRole(), u.isActive(),
                    u.isPhoneVerified(), u.getCreatedAt());
        }
    }

    private final UserRepository users;

    public UserAdminService(UserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserView> list(int page, int size) {
        return PageResponse.of(users.findAll(Paging.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))),
                UserView::from);
    }

    @Transactional
    public UserView setActive(Long actingAdminId, Long userId, boolean active) {
        User target = users.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        if (!active) {
            if (target.getId().equals(actingAdminId)) {
                throw new ConflictException("You cannot block your own account");
            }
            if (target.getRole() == Role.ADMIN) {
                throw new ConflictException("Admin accounts cannot be blocked");
            }
        }
        target.setActive(active);
        return UserView.from(target);
    }
}
