package release.portfolio.Dao;

import org.springframework.data.jpa.repository.JpaRepository;
import release.portfolio.Model.DTO.portfolioRequest;
import release.portfolio.Model.portfolioDataSets;

import java.util.List;
import java.util.Optional;

public interface portfolioDataSetRepo extends JpaRepository<portfolioDataSets,Long> {

    List<portfolioDataSets> findByPortfolioKeyAndProjectName(String pkey, String pname);

    Object findExecutionsById(Long id);

    Optional<portfolioRequest> findByPortfolioKey(String pkey);

    Optional<portfolioDataSets> findBySetName(String pname);

    Optional<portfolioDataSets> findByPortfolioKeyAndProjectNameAndSetName(String portfoliokey, String projectname, String pname);
}
