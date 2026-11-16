package com.lensflow.api.quote.repository;

import com.lensflow.api.quote.entity.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface QuoteRepository extends JpaRepository<Quote, Long> {
    List<Quote> findAllByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    Optional<Quote> findByIdAndOwnerId(Long id, Long ownerId);
    boolean existsByOwnerIdAndQuoteNumber(Long ownerId, String quoteNumber);
}
