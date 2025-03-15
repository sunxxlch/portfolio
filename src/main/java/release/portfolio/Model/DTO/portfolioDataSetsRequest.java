package release.portfolio.Model.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class portfolioDataSetsRequest {

    private String setName;
    private String projectId;
    private String versionId;
    private String cycleId;
}
