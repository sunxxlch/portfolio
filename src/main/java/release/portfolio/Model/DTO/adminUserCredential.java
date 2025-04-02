package release.portfolio.Model.DTO;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class adminUserCredential {
    private String project_name;
    private String userId;
    private String password;
    private String mailId;
}
