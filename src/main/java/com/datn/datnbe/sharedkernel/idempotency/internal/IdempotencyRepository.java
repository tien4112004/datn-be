package com.datn.datnbe.sharedkernel.idempotency.internal;

import com.datn.datnbe.sharedkernel.idempotency.api.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotencyRepository extends JpaRepository<IdempotencyKey, String> {

}
