package com.devops.itu_minitwit.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.devops.itu_minitwit.Database.DatabaseService;
import com.devops.itu_minitwit.Json.PublicDataContainer;
import com.devops.itu_minitwit.Json.ResultContainer;
import com.devops.itu_minitwit.Json.UserDataContainer;
import org.apache.logging.log4j.Logger;
import java.security.NoSuchAlgorithmException;
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

  // private SecureRandom random = new SecureRandom();
  private Base64.Encoder enc = Base64.getEncoder();
  private byte[] salt = new byte[16];

  private static final Logger log = LogManager.getLogger(); 
  private DatabaseService databaseService = new DatabaseService();
  private ObjectMapper mapper = new ObjectMapper();

  @PostConstruct
  private void initialize() {
  }

  @GetMapping("/")
  public String index() throws JsonProcessingException {
    log.info("GET: /");
    PublicDataContainer data = databaseService.getPublicData();
    String result = mapper.writeValueAsString(data);
    return result;
  }

  @RequestMapping(value="user", method = RequestMethod.GET)
  public @ResponseBody String getUserData(@RequestParam("user") String sessionUser,@RequestParam("profile") String profile) throws JsonProcessingException{

    log.info("GET: /user");
    int sessionUserProcessed = -1;
    if(!sessionUser.equals("null")) sessionUserProcessed = Integer.parseInt(sessionUser); 
    PublicDataContainer data = databaseService.getUserData(sessionUserProcessed,profile);
    String result = mapper.writeValueAsString(data);
    log.info(result);
    return result;
  }

    @RequestMapping(value="register", method = RequestMethod.GET)
  public @ResponseBody ResultContainer registerUser(@RequestParam("user") String userId,@RequestParam("email") String email,@RequestParam("password") String password) throws JsonProcessingException, NoSuchAlgorithmException, InvalidKeySpecException{
    log.info("GET: /register");
    KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
    SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
    byte[] hash = f.generateSecret(spec).getEncoded();
    return databaseService.registerNewUser(userId,email,enc.encodeToString(hash));
  }

    @RequestMapping(value="spec_user", method = RequestMethod.GET)
  public @ResponseBody String getSpecUser(@RequestParam("user") String profile,@RequestParam("password") String password) throws JsonProcessingException, NoSuchAlgorithmException, InvalidKeySpecException{

    log.info("GET: /spec_user");
    KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
    SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
    byte[] hash = f.generateSecret(spec).getEncoded();
    UserDataContainer data = databaseService.getSpecificUserData(profile,enc.encodeToString(hash));
    String result = mapper.writeValueAsString(data);
    log.info(result);
    return result;
  }

  @RequestMapping(value="is_followed", method = RequestMethod.GET)
  public @ResponseBody String isFollowed(@RequestParam("user") int userId,@RequestParam("profile") String profile) throws JsonProcessingException{
    log.info("GET: /is_followed");
    ResultContainer data =  databaseService.isFollowed(userId,profile);
    String result = mapper.writeValueAsString(data);
    log.info(result);
    return result;
  }

  @RequestMapping(value="follow", method = RequestMethod.GET)
  public @ResponseBody String follow(@RequestParam("user") String userId,@RequestParam("profile") String profile) throws JsonProcessingException{
    log.info("GET: /follow");
    ResultContainer data = databaseService.follow(userId,profile);
    String result = mapper.writeValueAsString(data);
    log.info(result);
    return result;
  }

  @RequestMapping(value="unfollow", method = RequestMethod.GET)
  public @ResponseBody String unfollow(@RequestParam("user") String userId,@RequestParam("profile") String profile) throws JsonProcessingException{
    log.info("GET: /unfollow");
    ResultContainer data =  databaseService.unFollow(userId,profile);
    String result = mapper.writeValueAsString(data);
    log.info(result);
    return result;
  }

  @RequestMapping(value="add_message", method = RequestMethod.GET)
  public @ResponseBody String addMessag(@RequestParam("user") String userId,@RequestParam("text") String text,@RequestParam("pubDate") String pubDate,@RequestParam("flagged") String flagged) throws JsonProcessingException{
    log.info("GET: /add_message");
    // int dateFormatted = Integer.parseInt(pubDate);
    ResultContainer data =  databaseService.addMessage(userId,text,pubDate,flagged);
    String result = mapper.writeValueAsString(data);
    log.info(result);
    return result;
  }

}