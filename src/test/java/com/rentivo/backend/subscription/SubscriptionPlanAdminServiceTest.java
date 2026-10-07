package com.rentivo.backend.subscription;

import com.rentivo.backend.common.exception.ConflictException;
import com.rentivo.backend.common.exception.NotFoundException;
import com.rentivo.backend.subscription.dto.SubscriptionDtos.PlanRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubscriptionPlanAdminServiceTest {

    private static final long ID = 1L;

    @Mock SubscriptionPlanRepository plans;

    private final SubscriptionPlanAdminService service = new SubscriptionPlanAdminService(plans);

    private PlanRequest request(String name) {
        return new PlanRequest(name, new BigDecimal("499.00"), 30, 10, "A plan", true);
    }

    @Test
    void createRejectsADuplicateName() {
        SubscriptionPlan existing = new SubscriptionPlan();
        existing.setName("Basic");
        when(plans.findByNameIgnoreCase("Basic")).thenReturn(Optional.of(existing));

        assertThrows(ConflictException.class, () -> service.create(request("Basic")));
    }

    @Test
    void updateAllowsKeepingItsOwnName() {
        SubscriptionPlan existing = new SubscriptionPlan();
        ReflectionTestUtils.setField(existing, "id", ID);
        existing.setName("Basic");
        when(plans.findById(ID)).thenReturn(Optional.of(existing));
        when(plans.findByNameIgnoreCase("Basic")).thenReturn(Optional.of(existing));

        var res = service.update(ID, request("Basic"));
        assertEquals("Basic", res.name());
    }

    @Test
    void updateRejectsRenamingToAnotherPlansName() {
        SubscriptionPlan existing = new SubscriptionPlan();
        ReflectionTestUtils.setField(existing, "id", ID);
        existing.setName("Basic");
        SubscriptionPlan other = new SubscriptionPlan();
        ReflectionTestUtils.setField(other, "id", 2L);
        other.setName("Pro");
        when(plans.findById(ID)).thenReturn(Optional.of(existing));
        when(plans.findByNameIgnoreCase("Pro")).thenReturn(Optional.of(other));

        assertThrows(ConflictException.class, () -> service.update(ID, request("Pro")));
    }

    @Test
    void deactivatingAMissingPlanFails() {
        when(plans.findById(ID)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.setActive(ID, false));
    }

    @Test
    void deactivateFlipsTheFlag() {
        SubscriptionPlan existing = new SubscriptionPlan();
        ReflectionTestUtils.setField(existing, "id", ID);
        existing.setActive(true);
        when(plans.findById(ID)).thenReturn(Optional.of(existing));

        var res = service.setActive(ID, false);
        assertFalse(res.active());
    }
}
