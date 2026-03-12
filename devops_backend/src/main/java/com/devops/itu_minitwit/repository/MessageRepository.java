package com.devops.itu_minitwit.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.devops.itu_minitwit.domain.Message;
import com.devops.itu_minitwit.domain.User;

public interface MessageRepository extends JpaRepository<Message, Integer> {

  long countByFlagged(Integer flagged);

  List<Message> findByFlaggedOrderByPubDateDesc(Integer flagged, Pageable pageable);

  List<Message> findByFlaggedAndAuthorInOrderByPubDateDesc(
      Integer flagged,
      Collection<User> authors,
      Pageable pageable);

  List<Message> findByFlaggedAndAuthorUsernameOrderByPubDateDesc(
      Integer flagged,
      String username,
      Pageable pageable);
}