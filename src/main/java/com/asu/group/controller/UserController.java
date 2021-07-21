package com.asu.group.controller;

import com.asu.group.entity.User;
import com.asu.group.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final String DEFAULT_ROLE = "ROLE_USER";
    private static final String ADMIN_ROLE = "ROLE_ADMIN";
    private static final String MAC_ROLE = "ROLE_MAC";
    private static final String[] ADMIN_ACCESS = {ADMIN_ROLE, MAC_ROLE};
    private static final String[] MAC_ACCESS = {MAC_ROLE};

    @Autowired
    private IUserRepository repository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @PostMapping("/create")
    public String createAccount(@RequestBody User user){
        user.setRoles(DEFAULT_ROLE);
        String encryptedPassword = bCryptPasswordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);
        repository.save(user);
        return "Hello, "+user.getUsername()+". Your university account has been created.";
    }
    //If loggedIn user is ADMIN -> ADMIN OR ADMISSION COMMITTEE ACCESS
    //If LoggedIn user is ADMISSION COMMITTEE -> ADMISSION COMMITTEE
    @GetMapping("access/{userId}/{userRole}")
    public String giveAccessToUser(@PathVariable int userId, @PathVariable String userRole, Principal principal){
           User user =  repository.findById(userId).get();
           List<String> activeRoles =   getRolesByLoggedInUser(principal);
           String newRole="";
           if(activeRoles.contains(userRole)){
              newRole = user.getRoles()+","+userRole;
              user.setRoles(newRole);
           }
           repository.save(user);
           return user.getUsername()+" is assigned with the new role.";
    }

    private List<String> getRolesByLoggedInUser(Principal principal){
        String roles = getLoggedInUser(principal).getRoles();
        List<String> assignRolesList =  Arrays.stream(roles.split(",")).collect(Collectors.toList());
        if(assignRolesList.contains(ADMIN_ROLE)){
            return Arrays.stream(ADMIN_ACCESS).collect(Collectors.toList());
        }
        if(assignRolesList.contains(MAC_ROLE)){
            return Arrays.stream(MAC_ACCESS).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private User getLoggedInUser(Principal principal){
        return repository.findByUserName(principal.getName()).get();
    }

}
