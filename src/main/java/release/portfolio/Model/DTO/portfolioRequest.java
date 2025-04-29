package release.portfolio.Model.DTO;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class portfolioRequest {
    private String portfolioKey;
    private String projectName;
    private List<portfolioDataSetsRequest> dataSets;


}
