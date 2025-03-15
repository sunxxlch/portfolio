package release.portfolio.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "portfolios", uniqueConstraints = @UniqueConstraint(columnNames = {"project_name", "portfolio_key"}))
public class portfolioData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_name", nullable = false)
    private String projectName;

    @Column(name = "portfolio_key", nullable = false)
    private String portfolioKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();


}

