package ru.kata.spring.boot_security.demo.configs;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;

@Component
public class InitUsers {

    @PersistenceContext
    private EntityManager entityManager;
    private final PasswordEncoder passwordEncoder;
    public InitUsers(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initTestUsers() {
        Role roleAdmin = entityManager.createQuery(
                        "select r from Role r where r.role = :role", Role.class)
                .setParameter("role", "ROLE_ADMIN")
                .getResultStream()
                .findFirst()
                .orElseGet(() -> {
                    Role r = new Role("ROLE_ADMIN");
                    entityManager.persist(r);
                    return r;
                });

        Role roleUser = entityManager.createQuery(
                        "select r from Role r where r.role = :role", Role.class)
                .setParameter("role", "ROLE_USER")
                .getResultStream()
                .findFirst()
                .orElseGet(() -> {
                    Role r = new Role("ROLE_USER");
                    entityManager.persist(r);
                    return r;
                });

        User user1 = new User("admin", "Veronika", "Darbinyan", 22L, "admin");
        user1.setPassword(passwordEncoder.encode("admin"));
        user1.setRoles(new HashSet<>(Set.of(roleAdmin, roleUser)));

        User user2 = new User("user", "Ivan", "Ivanov", 20L, "user");
        user2.setPassword(passwordEncoder.encode("user"));
        user2.setRoles(new HashSet<>(Set.of(roleUser)));

        entityManager.persist(user1);
        entityManager.persist(user2);
    }
}
