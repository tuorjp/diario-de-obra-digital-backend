package ueg.diario_de_obra_digital_backend.dto;

import lombok.Data;

import java.util.Set;

@Data
public class CreateObraDTO {
    private String contratante;
    private String contratada;
    private String projeto;
    private Long fiscalId;
    private Set<Long> engenheiroIds;
}
