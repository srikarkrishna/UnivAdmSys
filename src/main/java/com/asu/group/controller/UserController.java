package com.asu.group.controller;

import com.asu.group.common.UserConstant;
import com.asu.group.entity.User;
import com.asu.group.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository repository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @PostMapping("/create")
    public String createAccount(@RequestBody User user){
        user.setRoles(UserConstant.DEFAULT_ROLE);
        String encryptedPassword = bCryptPasswordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);
        repository.save(user);
        return "Hello, "+user.getUserName()+". Your university account has been created.";
    }
    //If loggedIn user is ADMIN -> ADMIN OR ADMISSION COMMITTEE ACCESS
    //If LoggedIn user is ADMISSION COMMITTEE -> ADMISSION COMMITTEE

    @GetMapping("access/{userId}/{userRole}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MAC')")
    public String giveAccessToUser(@PathVariable int userId, @PathVariable String userRole, Principal principal){
           User user =  repository.findById(userId).get();
           List<String> activeRoles =   getRolesByLoggedInUser(principal);
           String newRole="";
           if(activeRoles.contains(userRole)){
              newRole = user.getRoles()+","+userRole;
              user.setRoles(newRole);
           }
           repository.save(user);
           return user.getUserName()+" is assigned with the new role.";
    }

    @GetMapping
    @Secured("ROLE_ADMIN")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<User> loadUsers(){
        return repository.findAll();
    }

    @GetMapping("/test")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String testUserAccess(){
        return "user can only access this";
    }

    private List<String> getRolesByLoggedInUser(Principal principal){
        String roles = getLoggedInUser(principal).getRoles();
        List<String> assignRolesList =  Arrays.stream(roles.split(",")).collect(Collectors.toList());
        if(assignRolesList.contains(UserConstant.ADMIN_ROLE)){
            return Arrays.stream(UserConstant.ADMIN_ACCESS).collect(Collectors.toList());
        }
        if(assignRolesList.contains(UserConstant.MAC_ROLE)){
            return Arrays.stream(UserConstant.MAC_ACCESS).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private User getLoggedInUser(Principal principal){
        return repository.findByUserName(principal.getName()).get();
    }

}
