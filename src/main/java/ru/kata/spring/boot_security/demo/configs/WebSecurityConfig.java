package ru.kata.spring.boot_security.demo.configs;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserServiceImpl;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final SuccessUserHandler successUserHandler;
    private final UserDetailsService userDetailsService;
    private final EntityManager entityManager;
    private final PasswordEncoder passwordEncoder;


    public WebSecurityConfig(SuccessUserHandler successUserHandler, UserDetailsService userDetailsService, EntityManager entityManager, PasswordEncoder passwordEncoder) {
        this.successUserHandler = successUserHandler;
        this.userDetailsService = userDetailsService;
        this.entityManager = entityManager;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .successHandler(successUserHandler)
                .permitAll()
                .and()
                .logout()
                .permitAll();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider());
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