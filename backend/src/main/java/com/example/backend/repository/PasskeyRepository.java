package com.example.backend.repository;

import com.example.backend.domain.Passkey;
import com.example.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasskeyRepository extends JpaRepository<Passkey, Long> {
    List<Passkey> findByUserAndActiveTrue(User user);
    Optional<Passkey> findByCredentialIdAndActiveTrue(String credentialId);
    Optional<Passkey> findByCredentialId(String credentialId);
    List<Passkey> findByUserId(UUID userId);
}
