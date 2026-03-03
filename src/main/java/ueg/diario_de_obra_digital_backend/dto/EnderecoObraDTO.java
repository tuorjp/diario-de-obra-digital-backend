package ueg.diario_de_obra_digital_backend.dto;

import lombok.Data;
import ueg.diario_de_obra_digital_backend.model.EnderecoObra;

@Data
public class EnderecoObraDTO {
    private String endereco;
    private String complemento;
    private String numero;
    private String cidade;
    private String cep;
    private String uf;

    public EnderecoObraDTO() {
    }

    public EnderecoObraDTO(EnderecoObra enderecoObra) {
        if (enderecoObra != null) {
            this.endereco = enderecoObra.getEndereco();
            this.complemento = enderecoObra.getComplemento();
            this.numero = enderecoObra.getNumero();
            this.cidade = enderecoObra.getCidade();
            this.cep = enderecoObra.getCep();
            this.uf = enderecoObra.getUf();
        }
    }
}
