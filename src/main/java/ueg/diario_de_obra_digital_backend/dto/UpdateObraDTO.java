package ueg.diario_de_obra_digital_backend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UpdateObraDTO {
    private String contratante;
    private String contratada;
    private String projeto;
    private LocalDate dataInicio;
    private Long fiscalId;
    private Set<Long> engenheiroIds;
}
