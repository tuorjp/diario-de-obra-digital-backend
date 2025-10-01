package ueg.diario_de_obra_digital_backend.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ueg.diario_de_obra_digital_backend.enums.UserRole;

import java.util.Collection;
import java.util.List;

@Entity(name = "users")
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor // Adiciona o construtor vazio necessário para o JPA
public class User implements UserDetails {
  @EqualsAndHashCode.Include
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String login;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  private UserRole role;

  public User(String login, String name, String password, UserRole role) {
    this.login = login;
    this.name = name;
    this.password = password;
    this.role = role;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    if (this.role == UserRole.ADMIN) {
      // Um admin também é um usuário
      return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"));
    } else {
      // Mapeia as outras roles diretamente. Ex: GESTOR -> ROLE_GESTOR
      // Isso dá mais flexibilidade para as permissões.
      return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }
  }

  @Override
  public String getPassword() {
    // CORREÇÃO CRÍTICA: Deve retornar a senha real do usuário
    return this.password;
  }

  @Override
  public String getUsername() {
    // CORREÇÃO CRÍTICA: Deve retornar o campo usado para login
    return this.login;
  }
}