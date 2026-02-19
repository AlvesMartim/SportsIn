package org.SportsIn.repository;

import org.SportsIn.model.chat.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByEquipe_IdOrderByEnvoyeAAsc(Long equipeId);
}
