package com.devops.itu_minitwit.Database;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.devops.itu_minitwit.Json.PublicDataContainer;
import com.devops.itu_minitwit.Json.PublicDataRecord;
import com.devops.itu_minitwit.Json.Result;
import com.devops.itu_minitwit.Json.ResultContainer;
import com.devops.itu_minitwit.Json.UserData;
import com.devops.itu_minitwit.Json.UserDataContainer;
import com.devops.itu_minitwit.domain.Follower;
import com.devops.itu_minitwit.domain.FollowerId;
import com.devops.itu_minitwit.domain.Message;
import com.devops.itu_minitwit.domain.User;
import com.devops.itu_minitwit.repository.FollowerRepository;
import com.devops.itu_minitwit.repository.MessageRepository;
import com.devops.itu_minitwit.repository.UserRepository;

@Service
public class DatabaseService {

    private static final Logger log = LogManager.getLogger();

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final FollowerRepository followerRepository;

    public DatabaseService(MessageRepository messageRepository,
                           UserRepository userRepository,
                           FollowerRepository followerRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.followerRepository = followerRepository;
    }

    private String formatPubDate(Long seconds) {
        if (seconds == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new Date(seconds * 1000L));
    }

    public int getTotalMessageCount() {
        try {
            return (int) messageRepository.countByFlagged(0);
        } catch (Exception e) {
            log.error("Count messages failed: " + e.getMessage());
            return 0;
        }
    }

    public int getTotalUserCount() {
        try {
            return (int) userRepository.count();
        } catch (Exception e) {
            log.error("Count users failed: " + e.getMessage());
            return 0;
        }
    }

    public PublicDataContainer getPublicData() {
        log.info("Querying public data records");
        try {
            List<Message> all = messageRepository.findByFlaggedOrderByPubDateDesc(0);
            int limit = Math.min(30, all.size());
            List<Message> messages = all.subList(0, limit);
            ArrayList<PublicDataRecord> data = new ArrayList<>();

            for (Message m : messages) {
                User u = m.getAuthor();
                PublicDataRecord record = new PublicDataRecord(
                        m.getId(),
                        u.getId() != null ? String.valueOf(u.getId()) : null,
                        m.getText(),
                        formatPubDate(m.getPubDate()),
                        m.getFlagged() != null ? m.getFlagged() : 0,
                        u.getId() != null ? u.getId() : 0,
                        u.getUsername(),
                        u.getEmail(),
                        u.getPwHash()
                );
                data.add(record);
            }
            PublicDataContainer result = new PublicDataContainer(data, false);
            log.info("Querying public data succeeded");
            return result;
        } catch (Exception e) {
            log.error("Querying public data failed " + e.getMessage());
            return null;
        }
    }

    public PublicDataContainer getUserData(int sessionUser, String profileUser) {

        log.info("Querying user data records for user: " + sessionUser);
        ResultContainer followed = isFollowed(sessionUser, profileUser != null ? profileUser : "");

        UserDataContainer userdata = getUserId(profileUser);

        if (userdata != null && userdata.getUserData().getUserId() == 0) {
            log.error("User doesnt exist: " + profileUser);
            return new PublicDataContainer();
        } else if (userdata == null) {
            return new PublicDataContainer();
        }

        try {
            int profileUserId = userdata.getUserData().getUserId();

            Optional<User> profileOpt = userRepository.findById(profileUserId);
            if (profileOpt.isEmpty()) {
                return new PublicDataContainer();
            }
            User profile = profileOpt.get();

            List<Follower> edges = followerRepository.findByWhoUsernameOrderByWhomUsernameAsc(profile.getUsername());
            List<User> authors = new ArrayList<>();
            authors.add(profile);
            for (Follower f : edges) {
                authors.add(f.getWhom());
            }

            List<Message> all = messageRepository
                    .findByFlaggedAndAuthorInOrderByPubDateDesc(0, authors);
            int limit = Math.min(30, all.size());
            List<Message> messages = all.subList(0, limit);

            ArrayList<PublicDataRecord> data = new ArrayList<>();
            for (Message m : messages) {
                User u = m.getAuthor();
                PublicDataRecord record = new PublicDataRecord(
                        m.getId(),
                        u.getId() != null ? String.valueOf(u.getId()) : null,
                        m.getText(),
                        formatPubDate(m.getPubDate()),
                        m.getFlagged() != null ? m.getFlagged() : 0,
                        u.getId() != null ? u.getId() : 0,
                        u.getUsername(),
                        u.getEmail(),
                        u.getPwHash()
                );
                data.add(record);
            }

            PublicDataContainer result = new PublicDataContainer(
                    data,
                    followed.getUserData().isResult()
            );
            log.info(String.format("Querying user data of  user: %s succeeded", sessionUser));
            return result;
        } catch (Exception e) {
            log.error(String.format("Querying user data of  user: %s failed %s", sessionUser, e.getMessage()));
            return null;
        }
    }

    public UserDataContainer getSpecificUserData(String userId, String pwdHash) {

        log.info("Querying user data records for user: " + userId);
        try {
            Optional<User> userOpt = userRepository.findByUsername(userId);
            UserData userData = new UserData();

            if (userOpt.isPresent()) {
                User u = userOpt.get();
                userData.setUsername(u.getUsername());
                if (u.getId() != null) {
                    userData.setUserId(u.getId());
                }
                if (pwdHash.equals(u.getPwHash())) {
                    userData.setPwOK(true);
                }
            }
            return new UserDataContainer(userData);
        } catch (Exception e) {
            log.error(String.format("Querying specific user data of  user: %s failed %s", userId, e.getMessage()));
            return null;
        }
    }

    public UserDataContainer getUserId(String username) {

        log.info("Get use id: " + username);
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);
            UserData userData = new UserData();
            if (userOpt.isPresent() && userOpt.get().getId() != null) {
                userData.setUserId(userOpt.get().getId());
            }
            return new UserDataContainer(userData);
        } catch (Exception e) {
            log.error(String.format("Get use id: %s failed %s", username, e.getMessage()));
            return null;
        }
    }

    public ResultContainer registerNewUser(String username, String email, String pwdHash) {
        log.info("Registring new user: " + username);
        UserDataContainer userdata = getUserId(username);
        if (userdata != null && userdata.getUserData().getUserId() != 0) {
            log.error("User already exists: " + username);
            return new ResultContainer(new Result("The username is already taken", true, false));
        }

        try {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPwHash(pwdHash);
            userRepository.save(user);
            log.info(String.format("Succesfully registred new user: %s, email: %s", username, email));
        } catch (Exception e) {
            log.error(String.format("Registration of new user: %s, email: %s failed. %s", username, email, e.getMessage()));
            return new ResultContainer(new Result("DB_ERROR", true, false));
        }
        return new ResultContainer(new Result("OK", false, true));

    }

    public ResultContainer isFollowed(int sessionUser, String profileUser) {
        log.info("Checkking follow status for user: " + profileUser);
        UserDataContainer profileUserId = getUserId(profileUser);

        boolean result = false;
        try {
            int whomId = profileUserId.getUserData().getUserId();
            if (whomId != 0 && sessionUser != 0) {
                FollowerId id = new FollowerId(sessionUser, whomId);
                result = followerRepository.existsById(id);
            }
            log.info(String.format("Succesfully checked follow status: %s, profileUser: %s", sessionUser, profileUser));
        } catch (Exception e) {
            log.error(String.format("Check of follow status: %s, profileUser: %s failed %s",
                    sessionUser, profileUser, e.getMessage()));
            return new ResultContainer(new Result("DB_ERROR", true, false));
        }
        return new ResultContainer(new Result("OK", false, result));
    }

    public ResultContainer follow(String userId, String whoUsername) {
        log.info("Follow user: " + userId);
        int whom;
        UserDataContainer userdata = getUserId(whoUsername);
        if (userdata != null && userdata.getUserData().getUserId() != 0) {
            whom = userdata.getUserData().getUserId();
        } else {
            log.error("User doesnt exists: " + whoUsername);
            return new ResultContainer(new Result("NOT_EXISTS", true, false));
        }

        try {
            int who = Integer.parseInt(userId);
            FollowerId id = new FollowerId(who, whom);
            if (!followerRepository.existsById(id)) {
                Optional<User> whoUserOpt = userRepository.findById(who);
                Optional<User> whomUserOpt = userRepository.findById(whom);
                if (whoUserOpt.isEmpty() || whomUserOpt.isEmpty()) {
                    return new ResultContainer(new Result("NOT_EXISTS", true, false));
                }
                Follower follower = new Follower();
                follower.setId(id);
                follower.setWho(whoUserOpt.get());
                follower.setWhom(whomUserOpt.get());
                followerRepository.save(follower);
            }
            log.info(String.format("Succesfully followed: %s, profileUser: %s", userId, whom));
        } catch (Exception e) {
            log.error(String.format("Follow %s, profileUser: %s failed %s", userId, whom, e.getMessage()));
            return new ResultContainer(new Result("DB_ERROR", true, false));
        }
        return new ResultContainer(new Result("OK", false, true));
    }

    public ResultContainer unFollow(String userId, String whoUsername) {
        log.info("Unfollow user: " + userId);
        int whom;
        UserDataContainer userdata = getUserId(whoUsername);
        if (userdata != null && userdata.getUserData().getUserId() != 0) {
            whom = userdata.getUserData().getUserId();
        } else {
            log.error("User doesnt exist: " + whoUsername);
            return new ResultContainer(new Result("NOT_EXISTS", true, false));
        }

        try {
            int who = Integer.parseInt(userId);
            FollowerId id = new FollowerId(who, whom);
            if (followerRepository.existsById(id)) {
                followerRepository.deleteById(id);
            }
            log.info(String.format("Succesfully unfollowed: %s, profileUser: %s", userId, whom));
        } catch (Exception e) {
            log.error(String.format("Unfollow %s, profileUser: %s failed %s", userId, whom, e.getMessage()));
            return new ResultContainer(new Result("NOT_EXISTS", true, false));
        }
        return new ResultContainer(new Result("OK", false, true));
    }

    public ResultContainer addMessage(String userId, String text, String pubDate, String flagged) {
        log.info("Add message for user: " + userId);

        try {
            int uid = Integer.parseInt(userId);
            Optional<User> userOpt = userRepository.findById(uid);
            if (userOpt.isEmpty()) {
                return new ResultContainer(new Result("NOT_EXISTS", true, false));
            }
            Message m = new Message();
            m.setAuthor(userOpt.get());
            m.setText(text);
            try {
                m.setPubDate(Long.parseLong(pubDate));
            } catch (NumberFormatException ex) {
                m.setPubDate(0L);
            }
            m.setFlagged(0);
            messageRepository.save(m);
            log.info(String.format("Succesfully added message for: %s", userId));
        } catch (Exception e) {
            log.error(String.format("Add message failed for: %s %s", userId, e.getMessage()));
            return new ResultContainer(new Result("NOT_EXISTS", true, false));
        }
        return new ResultContainer(new Result("OK", false, true));
    }
}
