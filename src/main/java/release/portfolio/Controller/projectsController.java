package release.portfolio.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.bind.annotation.*;
import release.portfolio.Model.DTO.RefreshTokenRequest;
import release.portfolio.Model.DTO.Role;
import release.portfolio.Model.User;
import release.portfolio.Model.portfolioData;
import release.portfolio.Model.projectData;
import release.portfolio.Services.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("http://localhost:3000/")
public class projectsController {

    @Autowired
    private projectsService pservice;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private jwtService jwtserv;

    @Autowired
    private jwtRefreshService jwtrefserv;

    @Autowired
    private userservice usd;

    @GetMapping("/")
    public List<projectData> getProjects(){
        return pservice.fetchallprojects();
    }

    @GetMapping("/allProject")
    public List<String> getallprojects(){
        List<projectData> lstprojects = pservice.fetchallprojects();

        return lstprojects.stream()
                .map(projectData::getName)
                .collect(Collectors.toList());
    }

    @PostMapping("/addProject")
    public List<projectData> addProject(@RequestBody projectData pdata){
        pservice.newproject(pdata);
        return pservice.fetchallprojects();
    }

    @GetMapping("/browse/{projectname}")
    public List<portfolioData> getportfolios(@PathVariable String projectname){
        return pservice.fetchallportfolios(projectname);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        if (authentication.isAuthenticated()) {
            String accessToken = jwtserv.generateToken(user.getUsername(), 1 * 60 * 1000);
            String refreshToken = jwtrefserv.generateRefreshToken(user.getUsername(), 60 * 60 * 1000);
            String role = pservice.getroleofUser(user.getUsername());
            System.out.println(Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken,
                    "Role",role
            ));
            return ResponseEntity.ok(Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken,
                    "Role",role
            ));
        }else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed authentication");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {

        String refreshToken = request.getRefreshToken();
        if (jwtrefserv.validateRefreshToken(refreshToken)) {
            String newAccessToken = jwtserv.generateToken(jwtrefserv.RefreshextractUsername(refreshToken),1 * 60 * 1000);
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid refresh token");
    }


    @PostMapping("/create")
    public User createuser(@RequestBody User user){
        return  usd.savedata(user);
    }

}
