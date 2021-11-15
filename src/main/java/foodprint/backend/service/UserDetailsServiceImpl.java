package foodprint.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import foodprint.backend.model.UserRepo;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    UserRepo repo;

    @Autowired
    private UserDetailsServiceImpl(UserRepo repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return repo.findByEmail(username).orElseThrow(
            () -> new UsernameNotFoundException("User not found")
        );
    }
    
}
