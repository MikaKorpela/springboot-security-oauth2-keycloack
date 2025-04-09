package com.pikecape.springboot.security.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DucksController {
  @GetMapping("/api/duey")
  public String getDuey() {
    return "Duey";
  }

  @GetMapping("/api/huey")
  public String getHuey() {
    return "Huey";
  }

  @GetMapping("/api/luey")
  public String getLuey() {
    return "Luey";
  }
}
