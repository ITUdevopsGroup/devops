package com.devops.itu_minitwit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devops.itu_minitwit.domain.Follower;
import com.devops.itu_minitwit.domain.FollowerId;

public interface FollowerRepository extends JpaRepository<Follower, FollowerId> {

  boolean existsById(FollowerId id);

  List<Follower> findByWhoUsernameOrderByWhomUsernameAsc(String username);
}

