package ru.kata.spring.boot_security.demo.service;

import org.hibernate.Hibernate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.dao.RoleDao;
import ru.kata.spring.boot_security.demo.dao.UserDao;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserDao userDao;
    private final RoleDao roleDao;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserDao userDao, RoleDao roleDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    @Override
    public void saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userDao.saveUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserById(long id) {
        return userDao.getUserById(id);
    }

    @Override
    public void updateUser(User user) {
        User dbUser = getUserById(user.getId()).orElseThrow();
        dbUser.setUsername(user.getUsername());
        dbUser.setFirstName(user.getFirstName());
        dbUser.setLastName(user.getLastName());
        dbUser.setAge(user.getAge());

        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            if (!passwordEncoder.matches(user.getPassword(), dbUser.getPassword())) {
                dbUser.setPassword(passwordEncoder.encode(user.getPassword()));
            }
        }
        userDao.updateUser(dbUser);
    }

    @Override
    public void deleteUser(long id) {
        userDao.deleteUser(id);
    }

    @Override
    public void saveUserWithRoles(User user, List<Long> roleIds) {
        Set<Role> roles = roleIds.stream()
                .map(id -> roleDao.getRoleById(id)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + id)))
                .collect(Collectors.toSet());
        user.setRoles(roles);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getId() == null) {
            userDao.saveUser(user);
        } else {
            userDao.updateUser(user);
        }
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails userDetails = userDao.getUserByUsername(username);
        if (userDetails == null) {
            throw new UsernameNotFoundException(String.format("User with username %s not found", username));
        }
        Hibernate.initialize(userDetails.getAuthorities());
        return userDetails;
    }

    @Override
    public User findByUsername(String username) {
        return userDao.getUserByUsername(username);
    }
}
