package ueg.diario_de_obra_digital_backend.dto;

import lombok.Data;
import ueg.diario_de_obra_digital_backend.enums.ObraStatus;
import ueg.diario_de_obra_digital_backend.model.Obra;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class ObraResponseDTO {
    private Long id;
    private String contratante;
    private String contratada;
    private String projeto;
    private ObraStatus status;
    private LocalDate dataInicio;
    private UserProfileDTO fiscal;
    private Set<UserProfileDTO> engenheiros;

    public ObraResponseDTO(Obra obra) {
        this.id = obra.getId();
        this.contratante = obra.getContratante();
        this.contratada = obra.getContratada();
        this.projeto = obra.getProjeto();
        this.status = obra.getStatus();
        this.dataInicio = obra.getDataInicio();
        this.fiscal = obra.getFiscal() != null ? new UserProfileDTO(obra.getFiscal()) : null;
        this.engenheiros = obra.getEngenheiros()
                .stream()
                .map(UserProfileDTO::new)
                .collect(Collectors.toSet());
    }
}
