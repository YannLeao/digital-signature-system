package com.example.backend.domain;

public enum AuditAction {
    LOGIN,
    LOGOUT,
    AUTH_FAIL,
    TOKEN_ISSUED,
    DOC_SIGNED,
    DOC_VERIFIED,
    PASSWORD_CHANGED
}