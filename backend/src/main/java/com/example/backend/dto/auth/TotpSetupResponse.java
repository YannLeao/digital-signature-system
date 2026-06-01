package com.example.backend.dto.auth;

import java.util.List;

public record TotpSetupResponse(
        String otpauthUrl,
        List<String> backupCodes
) {}