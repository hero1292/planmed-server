package ru.alexanyan.planmed.common.error;

public record ProblemDetails(String type, String title, int status, String detail) {}
