package release.portfolio.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import release.portfolio.Dao.UserRepo;
import release.portfolio.Model.DTO.Role;
import release.portfolio.Model.User;

@Service
public class userservice {

    @Autowired
    private UserRepo repo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    //private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public User savedata(User user) {
        // user.setPassword(encoder.encode(user.getPassword()));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setUser_roles(Role.valueOf(user.getUser_roles().toString()));
        return repo.save(user);
    }


}
