package com.novatech.store.admin;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

class InternalRequestAuthTest {
    @Test
    void validatesInternalSecret() {
        InternalRequestAuth verifier = new InternalRequestAuth("test-internal-key");
        assertDoesNotThrow(() -> verifier.check("test-internal-key"));
        assertThrows(ResponseStatusException.class, () -> verifier.check(null));
        assertThrows(ResponseStatusException.class, () -> verifier.check("wrong-internal-key"));
    }
}
