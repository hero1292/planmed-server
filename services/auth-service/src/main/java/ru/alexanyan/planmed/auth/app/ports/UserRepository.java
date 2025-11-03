package ru.alexanyan.planmed.auth.app.ports;

import java.util.Optional;

public interface UserRepository {
    Optional<UserView> findByLogin(String login);
    UserId save(UserAggregate user);
    record UserId(String value) {}
    record UserView(String id, String login, String status, String roleCode) {}
    class UserAggregate {}
}
