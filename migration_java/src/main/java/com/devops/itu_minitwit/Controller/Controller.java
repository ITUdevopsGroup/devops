package com.devops.itu_minitwit.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.devops.itu_minitwit.Database.DatabaseService;
import com.devops.itu_minitwit.Json.PublicDataContainer;
import com.devops.itu_minitwit.Json.UserData;
import com.devops.itu_minitwit.Json.UserDataContainer;

import org.apache.logging.log4j.Logger;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.logging.log4j.LogManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@RestController
public class Controller {

  private SecureRandom random = new SecureRandom();
  private Base64.Encoder enc = Base64.getEncoder();
  private byte[] salt = new byte[16];

  private static final Logger log = LogManager.getLogger(); 
  private DatabaseService databaseService = new DatabaseService();

  @PostConstruct
  private void initialize() {
    // random.nextBytes(salt);
  }

  @GetMapping("/")
  public String index() throws JsonProcessingException {
    log.info("GET: /");
    PublicDataContainer data = databaseService.getPublicData();
    ObjectMapper mapper = new ObjectMapper();
    String result = mapper.writeValueAsString(data);
    return result;
  }

  @RequestMapping(value="user", method = RequestMethod.GET)
  public @ResponseBody String getUserData(@RequestParam("user") int user) throws JsonProcessingException{

    log.info("GET: /user");
    PublicDataContainer data = databaseService.getUserData(user);
    ObjectMapper mapper = new ObjectMapper();
    String result = mapper.writeValueAsString(data);
    log.info(result);
    return result;
  }

    @RequestMapping(value="register", method = RequestMethod.GET)
  public @ResponseBody int registerUser(@RequestParam("user") String userId,@RequestParam("email") String email,@RequestParam("password") String password) throws JsonProcessingException, NoSuchAlgorithmException, InvalidKeySpecException{
    log.info("GET: /register");
    KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
    SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
    byte[] hash = f.generateSecret(spec).getEncoded();
    if(databaseService.registerNewUser(userId,email,enc.encodeToString(hash))) return 0;
    return -1;
  }

    @RequestMapping(value="spec_user", method = RequestMethod.GET)
  public @ResponseBody String getSpecUser(@RequestParam("user") String user,@RequestParam("password") String password) throws JsonProcessingException, NoSuchAlgorithmException, InvalidKeySpecException{

    log.info("GET: /spec_user");
    KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
    SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
    byte[] hash = f.generateSecret(spec).getEncoded();
    UserDataContainer data = databaseService.getSpecificUserData(user,enc.encodeToString(hash));
    ObjectMapper mapper = new ObjectMapper();
    String result = mapper.writeValueAsString(data);
    log.info(result);
    return result;
  }

}