package com.taha.medchatai.repository;

import com.taha.medchatai.entity.ConversationHistory;
import com.taha.medchatai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConversationHistoryRepository extends JpaRepository<ConversationHistory, Long> {

    // Belirli bir kullanıcının belirli oturumdaki son N mesajını getir
    @Query("SELECT ch FROM ConversationHistory ch WHERE ch.user = :user AND ch.sessionId = :sessionId ORDER BY ch.createdAt DESC")
    List<ConversationHistory> findByUserAndSessionIdOrderByCreatedAtDesc(@Param("user") User user, @Param("sessionId") String sessionId);

    // Son N mesajı getir (pagination için)
    @Query("SELECT ch FROM ConversationHistory ch WHERE ch.user = :user AND ch.sessionId = :sessionId ORDER BY ch.createdAt DESC LIMIT :limit")
    List<ConversationHistory> findLastNMessages(@Param("user") User user, @Param("sessionId") String sessionId, @Param("limit") int limit);

    // Belirli tarihten sonraki mesajları getir
    @Query("SELECT ch FROM ConversationHistory ch WHERE ch.user = :user AND ch.sessionId = :sessionId AND ch.createdAt > :since ORDER BY ch.createdAt ASC")
    List<ConversationHistory> findByUserAndSessionIdAndCreatedAtAfter(@Param("user") User user, @Param("sessionId") String sessionId, @Param("since") LocalDateTime since);

    // Kullanıcının tüm oturumlarını getir
    @Query("SELECT DISTINCT ch.sessionId FROM ConversationHistory ch WHERE ch.user = :user ORDER BY MAX(ch.createdAt) DESC")
    List<String> findDistinctSessionIdsByUser(@Param("user") User user);
}
