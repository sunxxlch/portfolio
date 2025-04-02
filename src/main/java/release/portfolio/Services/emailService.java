package release.portfolio.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import release.portfolio.Dao.adminRepo;
import release.portfolio.Model.AdminData;

import java.util.Optional;

@Service
public class emailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private adminRepo adrepo;

    public void sendEmail(String subject, String projectname, String portfolioKey) {
        System.out.println("Started email");
        Optional<AdminData> op = adrepo.findById(projectname);
        AdminData admData = op.get();
        String to = admData.getMailid();
        String text = "";
        if(subject.equals("Release Sign Off")){
            text = "This is a message for project: " + projectname +
                    " and portfolio key: " + portfolioKey +
                    "Executions are completed with 100% Pass. " +
                    "We are good to release the build. " +
                    "Here are the portfolio for tests executed:"+
                    "http://dnjdn.com";
        } else {
            text = "This is a message for project: " + projectname +
                    " and portfolio key: " + portfolioKey +
                    "Executions are not completed with 100% Pass. " +
                    "But we are not completed our executions before the threshold "+
                    "please request to extend deployment date or to release the build using conditional Sign Off. " +
                    "Here are the portfolio for tests executed:"+
                    "http://dnjdn.com";
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
        System.out.println("sent email");

    }
}