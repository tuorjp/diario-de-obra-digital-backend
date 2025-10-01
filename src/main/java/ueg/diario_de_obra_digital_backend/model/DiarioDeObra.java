package ueg.diario_de_obra_digital_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ueg.diario_de_obra_digital_backend.enums.DiarioStatus;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "diarios_de_obra", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"obra_id", "data"}) //RN 03 - Unicidade de diário
})
public class DiarioDeObra implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "obra_id", nullable = false)
  private Obra obra;

  @Column(nullable = false)
  private LocalDate data;

  @Column(nullable = false)
  private String condicaoClimatica;

  @OneToMany(mappedBy = "diario", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<DiarioEquipamento> equipamentos;

  @OneToMany(mappedBy = "diario", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<DiarioMaoDeObra> maoDeObra;

  @OneToMany(mappedBy = "diario", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<DiarioServico> servicosExecutados;

  @ElementCollection //Para armazenar a lista de nomes/caminhos dos arquivos de fotos
  @CollectionTable(name = "diario_fotos", joinColumns = @JoinColumn(name = "diario_id"))
  @Column(name = "foto_url")
  private List<String> fotos;

  @ElementCollection
  @CollectionTable(name = "diario_visitas", joinColumns = @JoinColumn(name = "diario_id"))
  @Column(name = "visita_registro")
  private List<String> visitas;

  @Lob //Para textos mais longos
  @Column(columnDefinition = "TEXT")
  private String observacoes;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DiarioStatus status;

  @Column(nullable = false)
  private boolean deletado = false; //Para exclusão lógica (RN 01)

  @ManyToOne
  @JoinColumn(name = "autor_id", nullable = false)
  private User autor; //Engenheiro que criou

  @ManyToOne
  @JoinColumn(name = "validador_id")
  private User validador; //Fiscal ou Gestor que validou/invalidou

  private String comentarioValidacao;
}
