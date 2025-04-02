package release.portfolio.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import release.portfolio.Dao.UserRepo;
import release.portfolio.Dao.adminRepo;
import release.portfolio.Dao.portfolioRepo;
import release.portfolio.Dao.projectRepo;
import release.portfolio.Model.AdminData;
import release.portfolio.Model.DTO.adminUserCredential;
import release.portfolio.Model.User;
import release.portfolio.Model.portfolioData;
import release.portfolio.Model.projectData;
import java.util.Base64;

import java.util.List;

@Service
public class projectsService {

    @Autowired
    private projectRepo prepo;

    @Autowired
    private portfolioRepo portrepo;

    @Autowired
    private UserRepo urepo;

    @Autowired
    private adminRepo adrepo;

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

    public void addAdminData(adminUserCredential ad) {
        AdminData adm = new AdminData();
        adm.setProject_name(ad.getProject_name());
        adm.setCredentials(Base64.getEncoder().encodeToString((ad.getUserId() + ":" + ad.getPassword()).getBytes()));
        adm.setMailid(ad.getMailId());
        adrepo.save(adm);
    }




}
