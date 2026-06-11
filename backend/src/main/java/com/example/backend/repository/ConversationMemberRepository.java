package com.example.backend.repository;

import com.example.backend.entity.ConversationMember;
import com.example.backend.entity.ConversationMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, ConversationMemberId> {
    List<ConversationMember> findByUserId(Long userId);

    List<ConversationMember> findByConversationId(Long conversationId);

    List<ConversationMember> findByConversationIdIn(Collection<Long> conversationIds);

    boolean existsByConversationIdAndUserId(Long conversationId, Long userId);
}
