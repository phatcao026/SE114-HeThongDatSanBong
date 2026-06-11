package com.example.backend.repository;

import com.example.backend.entity.Conversation;
import com.example.backend.utils.Enums;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByIdInOrderByCreatedAtDesc(Collection<Long> ids);

    List<Conversation> findByIdInAndType(Collection<Long> ids, Enums.ConversationType type);
}
