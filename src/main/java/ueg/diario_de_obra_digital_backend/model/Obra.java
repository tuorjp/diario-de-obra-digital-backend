package ueg.diario_de_obra_digital_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ueg.diario_de_obra_digital_backend.enums.ObraStatus;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "obras")
public class Obra implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String contratante;

  @Column(nullable = false)
  private String contratada;

  @Column(nullable = false)
  private String projeto;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ObraStatus status;

  @ManyToOne
  @JoinColumn(name = "fiscal_id")
  private User fiscal;

  @ManyToMany
  @JoinTable(
      name = "obras_engenheiros",
      joinColumns = @JoinColumn(name = "obra_id"),
      inverseJoinColumns = @JoinColumn(name = "engenheiro_id")
  )
  private Set<User> engenheiros = new HashSet<>();

  @OneToMany(mappedBy = "obra", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<DiarioDeObra> diarios;
}