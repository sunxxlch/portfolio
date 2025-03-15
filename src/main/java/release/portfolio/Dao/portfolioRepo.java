package release.portfolio.Dao;

import org.springframework.data.jpa.repository.JpaRepository;
import release.portfolio.Model.portfolioData;

import java.util.List;

public interface portfolioRepo extends JpaRepository<portfolioData,Integer> {

    List<portfolioData> findByProjectName(String projectName);

    boolean existsByPortfolioKeyAndProjectName(String key, String projectName);

    portfolioData findById(long pid);

    void deleteById(long pid);
}
