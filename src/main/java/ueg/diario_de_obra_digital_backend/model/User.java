package ueg.diario_de_obra_digital_backend.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ueg.diario_de_obra_digital_backend.enums.UserRole;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Entity(name = "users")
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User implements UserDetails {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String login;

    @Column(nullable = false, length = 40)
    private String name;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "phone_1")
    private String phone1;

    @Column(name = "phone_2")
    private String phone2;

    private String crea;

    @Column(name = "crea_uf", length = 2)
    private String creaUf;

    @Column(unique = true)
    private String cpf;

    @CreationTimestamp
    @Column(name = "creation_date", updatable = false)
    private LocalDate creationDate;

    @Column(nullable = false)
    private boolean enabled = true;

    // ... (address, addressNumber, zipCode, complement, city, state mantidos) ...
    private String address;

    @Column(name = "address_number")
    private String addressNumber;

    @Column(name = "zip_code")
    private String zipCode;

    private String complement;

    private String city;

    @Column(length = 2)
    private String state;

    // Construtor
    public User(String login, String name, String password, UserRole role) {
        this.login = login;
        this.name = name;
        this.password = password;
        this.role = role;
        this.enabled = true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.role == UserRole.ADMIN) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"));
        } else {
            return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
        }
    }

    @Override
    public String getPassword() { return this.password; }

    @Override
    public String getUsername() { return this.login; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return this.enabled; }
}