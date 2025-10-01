package ueg.diario_de_obra_digital_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "servicos")
public class Servico {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String nome; // Ex: "Escavação"

  @Column(nullable = false)
  private String unidadeMedida; // Ex: "m³"
}
