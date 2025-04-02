package release.portfolio.Dao;

import jakarta.transaction.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import release.portfolio.Model.hotspot;

import java.util.List;
import java.util.Optional;

public interface HotspotRepo extends JpaRepository<hotspot,String> {
    void deleteByPortfolioKeyAndProjectName(String portfolioKey, String projectName);

    Optional<hotspot> findByProjectNameAndPortfolioKey(String projectName, String portfolioKey);
}
