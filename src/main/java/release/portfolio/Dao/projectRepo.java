package release.portfolio.Dao;

import org.springframework.data.jpa.repository.JpaRepository;
import release.portfolio.Model.DTO.loginRequest;
import release.portfolio.Model.projectData;

public interface projectRepo extends JpaRepository<projectData,String> {

}
