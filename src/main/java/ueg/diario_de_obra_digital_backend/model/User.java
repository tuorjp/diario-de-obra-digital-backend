package ueg.diario_de_obra_digital_backend.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ueg.diario_de_obra_digital_backend.enums.UserRole;

import java.util.Collection;
import java.util.List;

//Esse model precisa implementar o UserDetails, para ter as ROLES nativas do SPRING
@Entity(name = "users")
@Table(name = "users")
@Getter
@Setter
public class User implements UserDetails {
  @EqualsAndHashCode.Include
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String login;
  private String name;
  private String password;
  @Enumerated(EnumType.STRING)
  private UserRole role;

  public User(String login, String password, UserRole role) {
    this.login =  login;
    this.password = password;
    this.role = role;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    if(this.role == UserRole.ADMIN) {
      return List.of(
          new SimpleGrantedAuthority("ROLE_ADMIN"),
          new SimpleGrantedAuthority("ROLE_USER")
      );
    } else {
      return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
  }

  @Override
  public String getPassword() {
    return "";
  }

  @Override
  public String getUsername() {
    return "";
  }
}
