package fr.ensitech.ebooks.securingweb;

import fr.ensitech.ebooks.entity.User;
import fr.ensitech.ebooks.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private IUserService userService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userService.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Email non trouvé : " + email));
        return new CustomUserDetails(user);
    }
}

