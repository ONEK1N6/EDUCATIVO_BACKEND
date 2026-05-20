package com.educativo.api.repository;

import com.educativo.api.entity.CreditCard;
import com.educativo.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {
    Optional<CreditCard> findByUser(User user);
    Optional<CreditCard> findByUserId(Long userId);
}
