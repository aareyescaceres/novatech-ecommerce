package com.novatech.reports;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ReportControllerTest {
    @Test
    void rejectsMissingOrInvalidInternalToken() {
        ReportController controller = new ReportController(mock(ReportService.class), "local-secret");
        assertThrows(ResponseStatusException.class, () -> controller.summary(null));
        assertThrows(ResponseStatusException.class, () -> controller.summary("wrong-secret"));
    }
}
