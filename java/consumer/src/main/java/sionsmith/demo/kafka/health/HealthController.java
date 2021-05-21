package sionsmith.demo.kafka.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class HealthController {

    @RequestMapping(value = "/health")
    private String heathCheck() {
        return "{\"status\":\"UP\"}";
    }
}
