package release.portfolio.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import release.portfolio.Dao.UserRepo;
import release.portfolio.Dao.portfolioRepo;
import release.portfolio.Dao.projectRepo;
import release.portfolio.Model.DTO.loginRequest;
import release.portfolio.Model.User;
import release.portfolio.Model.portfolioData;
import release.portfolio.Model.projectData;

import java.util.List;

@Service
public class projectsService {

    @Autowired
    private projectRepo prepo;

    @Autowired
    private portfolioRepo portrepo;

    @Autowired
    private UserRepo urepo;



    public List<projectData> fetchallprojects() {
        return prepo.findAll();
    }

    public List<portfolioData> fetchallportfolios(String projectName) {
        List<portfolioData> portfolios = portrepo.findByProjectName(projectName);
        if (portfolios.isEmpty()) {
            throw new RuntimeException("No portfolios found for project: " + projectName);
        }
        return portfolios;
    }

    public void newproject(projectData pdata) {
        prepo.save(pdata);
    }


    public String getroleofUser(String username) {
        User lr = urepo.findByUsername(username);
        return String.valueOf(lr.getUser_roles());
    }
}
