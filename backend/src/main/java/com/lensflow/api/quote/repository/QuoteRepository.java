
package com.lensflow.api.quote.repository;

import com.lensflow.api.quote.entity.Quote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteRepository extends JpaRepository<Quote, Long> {
}
