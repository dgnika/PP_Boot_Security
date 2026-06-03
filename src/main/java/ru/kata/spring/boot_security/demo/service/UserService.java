package ru.kata.spring.boot_security.demo.service;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.kata.spring.boot_security.demo.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getAllUsers();

    void saveUser(User user);

    Optional<User> getUserById(long id);

    void updateUser(User user);

    void deleteUser(long id);

    void saveUserWithRoles(User user, List<Long> roleIds);

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    User findByUsername(String username);
}
