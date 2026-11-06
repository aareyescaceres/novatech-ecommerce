package com.novatech.backoffice.auth;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StaffAuthControllerTest {
    @Test
    void onlyStaffCanAccessTheBackoffice() {
        assertFalse(StaffAuthController.isStaffRole("CUSTOMER"));
        assertFalse(StaffAuthController.isStaffRole(null));
        assertTrue(StaffAuthController.isStaffRole("ADMIN"));
        assertTrue(StaffAuthController.isStaffRole("SALES_STAFF"));
        assertTrue(StaffAuthController.isStaffRole("INVENTORY_MANAGER"));
    }
}
