package release.portfolio.Dao;

import org.springframework.data.jpa.repository.JpaRepository;
import release.portfolio.Model.AdminData;

public interface adminRepo extends JpaRepository<AdminData, String> {
}
