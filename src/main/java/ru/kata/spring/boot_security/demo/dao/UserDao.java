package ru.kata.spring.boot_security.demo.dao;

import ru.kata.spring.boot_security.demo.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    User getUserByUsername(String username);

    List<User> getAllUsers();

    void saveUser(User user);

    Optional<User> getUserById(long id);

    void updateUser(User user);

    void deleteUser(long id);
}
