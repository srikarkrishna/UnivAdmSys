package com.asu.group.service;

import com.asu.group.entity.User;
import com.asu.group.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

public class GroupUserDetailsService implements UserDetailsService {
    @Autowired
    private IUserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = repository.findByUserName(username);
        return user.map(GroupUserDetails::new)
                .orElseThrow(()-> new UsernameNotFoundException(username+" doesn't exist in the system"));
    }
}
