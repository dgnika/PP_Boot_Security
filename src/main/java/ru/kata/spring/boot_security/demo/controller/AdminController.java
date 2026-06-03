package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public AdminController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping()
    public String showUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin";
    }

    @GetMapping("/create")
    public String createUser(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleService.getAllRoles());
        return "create";
    }

    @PostMapping
    public String saveNewUser(@ModelAttribute("user") User user, @RequestParam(required = false) List<Long> roleIds) {
        userService.saveUserWithRoles(user, roleIds);
        return "redirect:/admin";
    }

    @GetMapping("/update")
    public String updateUser(Model model, @RequestParam(name = "id") Long id) {
        User user = userService.getUserById(id).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("roles", roleService.getAllRoles());
        return "update";
    }

    @PostMapping("/update")
    public String saveUser(@ModelAttribute("user") User user, @RequestParam(required = false) List<Long> roleIds) {
        userService.saveUserWithRoles(user, roleIds);
        return "redirect:/admin";
    }

    @GetMapping("/delete")
    public String deleteUser(@RequestParam("id") Long id) {
        userService.deleteUser(id);
        return "redirect:/admin";
    }
}
