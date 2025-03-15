package release.portfolio.Model.DTO;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class jiradata {

    private JsonNode executedData;

//    private JsonNode defects;
//
//    private JsonNode bugs;

}
