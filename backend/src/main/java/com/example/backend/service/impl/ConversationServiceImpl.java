package com.example.backend.service.impl;

import com.example.backend.dto.request.ConversationCreateRequest;
import com.example.backend.dto.request.MessageCreateRequest;
import com.example.backend.dto.response.ConversationResponse;
import com.example.backend.dto.response.MessageResponse;
import com.example.backend.entity.Conversation;
import com.example.backend.entity.ConversationMember;
import com.example.backend.entity.Message;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.repository.ConversationMemberRepository;
import com.example.backend.repository.ConversationRepository;
import com.example.backend.repository.MessageRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.ConversationService;
import com.example.backend.service.NotificationService;
import com.example.backend.utils.Enums;
import com.example.backend.utils.TokenUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ConversationServiceImpl implements ConversationService {
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ConversationServiceImpl(ConversationRepository conversationRepository,
                                   ConversationMemberRepository conversationMemberRepository,
                                   MessageRepository messageRepository,
                                   UserRepository userRepository,
                                   NotificationService notificationService) {
        this.conversationRepository = conversationRepository;
        this.conversationMemberRepository = conversationMemberRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    public List<ConversationResponse> getMyConversations() {
        Long currentUserId = TokenUtils.getCurrentUserId();
        List<Long> conversationIds = conversationMemberRepository.findByUserId(currentUserId)
                .stream()
                .map(ConversationMember::getConversationId)
                .toList();
        if (conversationIds.isEmpty()) {
            return List.of();
        }

        List<Conversation> conversations = conversationRepository.findByIdInOrderByCreatedAtDesc(conversationIds);
        return toConversationResponses(conversations);
    }

    @Override
    public ConversationResponse getConversation(Long id) {
        ensureConversationMember(id);
        return toConversationResponse(findConversation(id));
    }

    @Override
    @Transactional
    public ConversationResponse createDirectConversation(ConversationCreateRequest request) {
        Long currentUserId = TokenUtils.getCurrentUserId();
        Long recipientId = request.getRecipientId();
        if (currentUserId.equals(recipientId)) {
            throw new AppException(400, "Cannot create a conversation with yourself");
        }

        userRepository.findById(recipientId)
                .orElseThrow(() -> new AppException(404, "Recipient not found"));

        Optional<Conversation> existingConversation = findExistingDirectConversation(currentUserId, recipientId);
        if (existingConversation.isPresent()) {
            return toConversationResponse(existingConversation.get());
        }

        return createConversation(Enums.ConversationType.DIRECT, List.of(currentUserId, recipientId));
    }

    @Override
    @Transactional
    public ConversationResponse createMatchConversation(Long postOwnerId, Long requesterId) {
        if (postOwnerId == null || requesterId == null || postOwnerId.equals(requesterId)) {
            throw new AppException(400, "Invalid match conversation members");
        }

        userRepository.findById(postOwnerId)
                .orElseThrow(() -> new AppException(404, "Post owner not found"));
        userRepository.findById(requesterId)
                .orElseThrow(() -> new AppException(404, "Requester not found"));

        return createConversation(Enums.ConversationType.MATCH_GROUP, List.of(postOwnerId, requesterId));
    }

    @Override
    public List<MessageResponse> getMessages(Long conversationId) {
        ensureConversationMember(conversationId);
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        return toMessageResponses(messages);
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(Long conversationId, MessageCreateRequest request) {
        Long currentUserId = TokenUtils.getCurrentUserId();
        ensureConversationMember(conversationId, currentUserId);

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(currentUserId);
        message.setContent(cleanRequired(request.getContent(), "Message content is required"));
        message.setCreatedAt(LocalDateTime.now());

        Message savedMessage = messageRepository.save(message);
        notifyConversationMembers(savedMessage);
        return toMessageResponse(savedMessage);
    }

    private ConversationResponse createConversation(Enums.ConversationType type, List<Long> memberIds) {
        Conversation conversation = new Conversation();
        conversation.setType(type);
        conversation.setCreatedAt(LocalDateTime.now());
        Conversation savedConversation = conversationRepository.save(conversation);

        List<ConversationMember> members = memberIds.stream()
                .distinct()
                .map(userId -> {
                    ConversationMember member = new ConversationMember();
                    member.setConversationId(savedConversation.getId());
                    member.setUserId(userId);
                    return member;
                })
                .toList();
        conversationMemberRepository.saveAll(members);

        return toConversationResponse(savedConversation);
    }

    private Optional<Conversation> findExistingDirectConversation(Long currentUserId, Long recipientId) {
        List<Long> myConversationIds = conversationMemberRepository.findByUserId(currentUserId)
                .stream()
                .map(ConversationMember::getConversationId)
                .toList();
        if (myConversationIds.isEmpty()) {
            return Optional.empty();
        }

        return conversationRepository.findByIdInAndType(myConversationIds, Enums.ConversationType.DIRECT)
                .stream()
                .filter(conversation -> {
                    List<Long> memberIds = conversationMemberRepository.findByConversationId(conversation.getId())
                            .stream()
                            .map(ConversationMember::getUserId)
                            .toList();
                    return memberIds.size() == 2
                            && memberIds.contains(currentUserId)
                            && memberIds.contains(recipientId);
                })
                .findFirst();
    }

    private Conversation findConversation(Long id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new AppException(404, "Conversation not found"));
    }

    private void ensureConversationMember(Long conversationId) {
        ensureConversationMember(conversationId, TokenUtils.getCurrentUserId());
    }

    private void ensureConversationMember(Long conversationId, Long userId) {
        if (!conversationMemberRepository.existsByConversationIdAndUserId(conversationId, userId)
                && !TokenUtils.hasRole("ADMIN")) {
            throw new AppException(403, "Access denied");
        }
    }

    private void notifyConversationMembers(Message message) {
        List<ConversationMember> members = conversationMemberRepository.findByConversationId(message.getConversationId());
        for (ConversationMember member : members) {
            if (member.getUserId().equals(message.getSenderId())) {
                continue;
            }

            notificationService.createNotification(
                    member.getUserId(),
                    "New message",
                    "You have a new message in conversation #" + message.getConversationId(),
                    Enums.NotificationType.NEW_MESSAGE
            );
        }
    }

    private ConversationResponse toConversationResponse(Conversation conversation) {
        return toConversationResponses(List.of(conversation)).getFirst();
    }

    private List<ConversationResponse> toConversationResponses(List<Conversation> conversations) {
        if (conversations.isEmpty()) {
            return List.of();
        }

        List<Long> conversationIds = conversations.stream().map(Conversation::getId).toList();
        Map<Long, List<ConversationMember>> membersByConversationId = conversationMemberRepository
                .findByConversationIdIn(conversationIds)
                .stream()
                .collect(Collectors.groupingBy(ConversationMember::getConversationId));
        Map<Long, User> usersById = mapUsers(membersByConversationId.values()
                .stream()
                .flatMap(Collection::stream)
                .map(ConversationMember::getUserId)
                .collect(Collectors.toSet()));
        Map<Long, Message> lastMessagesByConversationId = lastMessagesByConversationId(conversationIds);

        return conversations.stream()
                .sorted(conversationComparator(lastMessagesByConversationId))
                .map(conversation -> toConversationResponse(
                        conversation,
                        membersByConversationId.getOrDefault(conversation.getId(), List.of()),
                        usersById,
                        lastMessagesByConversationId.get(conversation.getId())
                ))
                .toList();
    }

    private ConversationResponse toConversationResponse(Conversation conversation,
                                                        List<ConversationMember> members,
                                                        Map<Long, User> usersById,
                                                        Message lastMessage) {
        ConversationResponse response = new ConversationResponse();
        response.setId(conversation.getId());
        response.setType(conversation.getType());
        response.setName(conversation.getName());
        response.setMemberIds(members.stream().map(ConversationMember::getUserId).toList());
        response.setMemberNames(members.stream()
                .map(member -> usersById.get(member.getUserId()))
                .filter(Objects::nonNull)
                .map(User::getFullName)
                .toList());
        if (lastMessage != null) {
            response.setLastMessageId(lastMessage.getId());
            response.setLastMessageSenderId(lastMessage.getSenderId());
            response.setLastMessageContent(lastMessage.getContent());
            response.setLastMessageCreatedAt(lastMessage.getCreatedAt());
        }
        response.setCreatedAt(conversation.getCreatedAt());
        return response;
    }

    private List<MessageResponse> toMessageResponses(List<Message> messages) {
        if (messages.isEmpty()) {
            return List.of();
        }

        Map<Long, User> usersById = mapUsers(messages.stream()
                .map(Message::getSenderId)
                .collect(Collectors.toSet()));

        return messages.stream()
                .map(message -> toMessageResponse(message, usersById))
                .toList();
    }

    private MessageResponse toMessageResponse(Message message) {
        Map<Long, User> usersById = mapUsers(Set.of(message.getSenderId()));
        return toMessageResponse(message, usersById);
    }

    private MessageResponse toMessageResponse(Message message, Map<Long, User> usersById) {
        User sender = usersById.get(message.getSenderId());

        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setConversationId(message.getConversationId());
        response.setSenderId(message.getSenderId());
        response.setSenderName(sender != null ? sender.getFullName() : null);
        response.setContent(message.getContent());
        response.setCreatedAt(message.getCreatedAt());
        return response;
    }

    private Map<Long, User> mapUsers(Collection<Long> userIds) {
        List<Long> cleanUserIds = userIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (cleanUserIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return userRepository.findAllById(cleanUserIds)
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private Map<Long, Message> lastMessagesByConversationId(Collection<Long> conversationIds) {
        if (conversationIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, Message> result = new LinkedHashMap<>();
        messageRepository.findByConversationIdInOrderByCreatedAtDesc(conversationIds)
                .forEach(message -> result.putIfAbsent(message.getConversationId(), message));
        return result;
    }

    private Comparator<Conversation> conversationComparator(Map<Long, Message> lastMessagesByConversationId) {
        return (left, right) -> {
            LocalDateTime leftTime = Optional.ofNullable(lastMessagesByConversationId.get(left.getId()))
                    .map(Message::getCreatedAt)
                    .orElse(left.getCreatedAt());
            LocalDateTime rightTime = Optional.ofNullable(lastMessagesByConversationId.get(right.getId()))
                    .map(Message::getCreatedAt)
                    .orElse(right.getCreatedAt());
            return rightTime.compareTo(leftTime);
        };
    }

    private String cleanRequired(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new AppException(400, message);
        }

        return value.trim();
    }
}
