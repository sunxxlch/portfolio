package release.portfolio.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import release.portfolio.Dao.UserRepo;
import release.portfolio.Model.User;
import release.portfolio.Model.UserPrincipal;

import java.util.List;

@Service
public class MyUserDetailService implements UserDetailsService {

    @Autowired
    private UserRepo usr;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = usr.findByUsername(username);
        if(user==null){
            throw new UsernameNotFoundException("404 error");
        }
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getUser_roles().name()));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }




}
