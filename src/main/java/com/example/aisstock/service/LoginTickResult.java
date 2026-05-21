package com.example.aisstock.service;

import com.example.aisstock.model.User;

import java.time.OffsetDateTime;

public record LoginTickResult(User user, OffsetDateTime previousLoginAt) {
}
