package com.devops.itu_minitwit.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.devops.itu_minitwit.domain.Follower;
import com.devops.itu_minitwit.domain.FollowerId;

public interface FollowerRepository extends JpaRepository<Follower, FollowerId> {
  boolean existsById(FollowerId id);
  List<Follower> findByWhoUsernameOrderByWhomUsernameAsc(String username);

  @Query("SELECT f.whom.username, COUNT(f) FROM Follower f GROUP BY f.whom.id, f.whom.username ORDER BY COUNT(f) DESC LIMIT 10")
  List<Object[]> findTop10ByFollowerCount();
}