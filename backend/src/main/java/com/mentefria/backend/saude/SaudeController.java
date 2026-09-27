package com.mentefria.backend.saude;

import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SaudeController {

    private final JdbcTemplate jdbc;

    public SaudeController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/saude")
    public Map<String, String> saude() {
        jdbc.queryForObject("SELECT 1", Integer.class);
        return Map.of("status", "ok", "banco", "ok");
    }
}