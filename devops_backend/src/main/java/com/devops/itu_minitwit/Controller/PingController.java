package com.devops.itu_minitwit.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class PingController {
    
     @GetMapping("/ping")
    public String ping() {
    return "pong";
  }
}
