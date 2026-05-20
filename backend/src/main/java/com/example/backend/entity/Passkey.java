package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

import com.example.backend.domain.User;

@Entity
@Table(name = "passkeys")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Passkey {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // Relacionamento com sua entidade User existente
    
    @Column(nullable = false, unique = true, length = 512)
    private String credentialId;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String publicKey;
    
    @Column(nullable = false)
    private Long counter;
    
    @Column(nullable = false, length = 36)
    private String aaguid;
    
    @Column(name = "device_name", length = 255)
    private String deviceName;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "last_used")
    private LocalDateTime lastUsed;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUsed = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastUsed = LocalDateTime.now();
    }
}