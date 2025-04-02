package release.portfolio.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "project_admin_data")
public class AdminData {
    @Id
    @Column(name = "project_name", nullable = false)
    private String project_name;
    @Column(name = "credentials", nullable = false)
    private String credentials;
    @Column(name = "mail_id", nullable = false)
    private String mailid;
}
