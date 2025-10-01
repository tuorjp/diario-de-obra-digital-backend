package ueg.diario_de_obra_digital_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "diario_servicos")
public class DiarioServico {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "diario_id")
  private DiarioDeObra diario;

  @ManyToOne(optional = false)
  @JoinColumn(name = "servico_id")
  private Servico servico;

  @Column(nullable = false)
  private Double quantidade;
}