package com.sadna_market.market.InfrastructureLayer.Adapters;

import com.sadna_market.market.DomainLayer.IMessageRepository;
import com.sadna_market.market.DomainLayer.Message;
import com.sadna_market.market.InfrastructureLayer.JpaRepos.MessageJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MessageJpaAdapter implements IMessageRepository {

    @Autowired
    private MessageJpaRepository messageJpaRepository;

    @Override
    public Message save(Message message) {
        return messageJpaRepository.save(message);
    }

    @Override
    public Optional<Message> findById(UUID messageId) {
        return messageJpaRepository.findById(messageId);
    }

    @Override
    public List<Message> findBySender(String username) {
        return messageJpaRepository.findBySenderUsername(username);
    }

    @Override
    public List<Message> findByStore(UUID storeId) {
        return messageJpaRepository.findByStoreId(storeId);
    }

    @Override
    public List<Message> findByUserAndStore(String username, UUID storeId) {
        return messageJpaRepository.findBySenderUsernameAndStoreId(username, storeId);
    }

    @Override
    public void deleteById(UUID messageId) {
        messageJpaRepository.deleteById(messageId);
    }

    @Override
    @Transactional
    public boolean addReply(UUID messageId, String replyAuthor, String replyText) {
        Optional<Message> optionalMessage = messageJpaRepository.findById(messageId);
        if (optionalMessage.isPresent()) {
            Message message = optionalMessage.get();
            message.addReply(replyAuthor, replyText);
            messageJpaRepository.save(message);
            return true;
        }
        return false;
    }

    @Override
    public List<Message> getUnreadMessagesForStore(UUID storeId) {
        return messageJpaRepository.findByStoreIdAndIsReadFalse(storeId);
    }

    @Override
    public List<Message> getUnansweredMessagesForStore(UUID storeId) {
        return messageJpaRepository.findUnansweredMessagesByStore(storeId);
    }

    @Override
    @Transactional
    public boolean markMessageAsRead(UUID messageId) {
        Optional<Message> optionalMessage = messageJpaRepository.findById(messageId);
        if (optionalMessage.isPresent()) {
            Message message = optionalMessage.get();
            message.markAsRead();
            messageJpaRepository.save(message);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public int markAllStoreMessagesAsRead(UUID storeId) {
        return messageJpaRepository.markAllStoreMessagesAsRead(storeId);
    }

    @Override
    public void clear() {
        messageJpaRepository.deleteAll();
    }

    @Override
    public List<Message> findAll() {
        return messageJpaRepository.findAll();
    }
}