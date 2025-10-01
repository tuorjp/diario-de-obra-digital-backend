package ueg.diario_de_obra_digital_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "diario_maos_de_obra")
public class DiarioMaoDeObra {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "diario_id")
  private DiarioDeObra diario;

  @ManyToOne(optional = false)
  @JoinColumn(name = "mao_de_obra_id")
  private MaoDeObra maoDeObra;

  @Column(nullable = false)
  private Integer quantidade;
}
