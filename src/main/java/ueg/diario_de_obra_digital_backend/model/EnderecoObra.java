package ueg.diario_de_obra_digital_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "enderecos_obras")
public class EnderecoObra implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String endereco;

    private String complemento;

    private String numero;

    @Column(nullable = false)
    private String cidade;

    @Column(nullable = false)
    private String cep;

    @Column(nullable = false)
    private String uf;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "obra_id", nullable = false)
    private Obra obra;
}
