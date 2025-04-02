package release.portfolio.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "hotspot_portfolios",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_name", "portfolio_key"}))
public class hotspot {
    @Id
    @Column(name = "project_name", nullable = false)
    private String projectName;

    @Column(name = "portfolio_key", nullable = false)
    private String portfolioKey;

    @Column(name = "date_of_release", nullable = false)
    private Date dor;
}
