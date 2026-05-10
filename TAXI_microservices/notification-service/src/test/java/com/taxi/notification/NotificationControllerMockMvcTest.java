package com.taxi.notification;

import com.taxi.notification.controller.NotificationController;
import com.taxi.notification.dto.NotificationResponseDto;
import com.taxi.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class)
class NotificationControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Test
    void postCreatesReturns201() throws Exception {
        NotificationResponseDto dto = new NotificationResponseDto(
                1L, 9L, "DRIVER", 2L, "m", "PENDING", 0, null, LocalDateTime.now());
        when(notificationService.createNotification(any())).thenReturn(dto);

        mockMvc.perform(post("/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tripId\":9,\"recipientType\":\"DRIVER\",\"recipientId\":2,\"message\":\"m\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void postValidationFailsWhenMessageMissing() throws Exception {
        mockMvc.perform(post("/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tripId\":9,\"recipientType\":\"DRIVER\",\"recipientId\":2}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postValidationFailsWhenTripIdNull() throws Exception {
        mockMvc.perform(post("/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipientType\":\"DRIVER\",\"recipientId\":2,\"message\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByTripReturnsOk() throws Exception {
        when(notificationService.getNotificationsByTrip(3L)).thenReturn(List.of());
        mockMvc.perform(get("/notifications").param("trip_id", "3"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getStatsReturnsOk() throws Exception {
        when(notificationService.getQueueStats()).thenReturn(Map.of(
                "PENDING", 1L, "PROCESSING", 0L, "SENT", 2L, "FAILED", 0L));
        mockMvc.perform(get("/notifications/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.PENDING").value(1));
    }
}
