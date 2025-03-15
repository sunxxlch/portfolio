package release.portfolio.Dao;

import org.springframework.data.jpa.repository.JpaRepository;
import release.portfolio.Model.User;

public interface UserRepo extends JpaRepository<User, String> {
    User findByUsername(String username);
}
