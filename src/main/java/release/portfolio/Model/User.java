package release.portfolio.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import release.portfolio.Model.DTO.Role;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="usercredentials")
public class User {

    @Id
    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role user_roles;

}