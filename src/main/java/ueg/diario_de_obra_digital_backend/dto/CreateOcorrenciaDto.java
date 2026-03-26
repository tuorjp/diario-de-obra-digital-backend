package ueg.diario_de_obra_digital_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOcorrenciaDto {
    private String tipo;
    private String ocorrencia;
}
