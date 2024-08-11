package com.nano.soft.kol.user.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.nano.soft.kol.bloger.entity.Bloger;
import com.nano.soft.kol.bloger.repo.BlogerRepository;
import com.nano.soft.kol.user.entity.User;
import com.nano.soft.kol.user.repo.UserRepository;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BlogerRepository blogerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);
        if (user == null)
        {
            Bloger bloger = blogerRepository.findByEmail(username);
            if (bloger == null)
            {
                throw new UsernameNotFoundException("User not found");
            }
            return bloger;
        }
        return user;
    }
}
