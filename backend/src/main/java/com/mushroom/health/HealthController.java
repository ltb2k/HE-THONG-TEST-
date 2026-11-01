package com.mushroom.health;

import com.mushroom.common.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @GetMapping
    public ApiResponse<Map<String, Object>> getHealth() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", Instant.now().toString());
        status.put("system", "Hệ thống quản lý nuôi trồng nấm thông minh");
        status.put("version", "1.0.0-MVP");

        // Kiểm tra database
        boolean dbHealthy = false;
        try {
            if (jdbcTemplate != null) {
                Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
                dbHealthy = (result != null && result == 1);
            }
        } catch (Exception e) {
            dbHealthy = false;
        }
        status.put("database", dbHealthy ? "CONNECTED" : "DISCONNECTED");

        return ApiResponse.success("Hệ thống đang hoạt động bình thường", status);
    }
}
