package com.chatbot.sumapi;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.HashMap;

@RestController
@CrossOrigin(origins = "http://genairagchatbot-frontend.s3.us-east-2.amazonaws.com")
public class SumController {

    private final SumService sumService;

    public SumController(SumService sumService) {
        this.sumService = sumService;
    }

    @GetMapping("/sum")
    public Map<String, Integer> sum(@RequestParam int a, @RequestParam int b) {
        int result = sumService.sum(a, b);
        Map<String, Integer> response = new HashMap<>();
        response.put("sum", result);
        return response;
    }
}
