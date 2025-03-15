package release.portfolio.Model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "portfolio_data_sets")
public class portfolioDataSets {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "portfolio_key", nullable = false)
    private String portfolioKey;

    @Column(name = "set_name", nullable = false)
    private String setName;

    @Column(name = "project_name", nullable = false)
    private String projectName;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "version_id", nullable = false)
    private String versionId;

    @Column(name = "cycle_id", nullable = false)
    private String cycleId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "executions", columnDefinition = "jsonb")
    private JsonNode executedData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "release_defects", columnDefinition = "jsonb")
    private JsonNode defects;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "identified_defects", columnDefinition = "jsonb")
    private JsonNode bugs;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "execution_reports", columnDefinition = "jsonb")
    private JsonNode reports;


}
