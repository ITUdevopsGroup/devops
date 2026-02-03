package com.devops.itu_minitwit.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devops.itu_minitwit.Database.DatabaseService;
import com.devops.itu_minitwit.Json.PublicDataContainer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class Controller {

  private static final Logger log = LogManager.getLogger(); 
  private DatabaseService databaseService = new DatabaseService();

  @GetMapping("/")
  public String index() throws JsonProcessingException {
    log.info("GET: /");
    PublicDataContainer data = databaseService.getPublicData();
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(data);
  }

}