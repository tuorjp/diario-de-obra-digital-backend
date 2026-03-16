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
    private String numeroContrato;
    private ObraStatus status;
    private LocalDate dataInicio;
    private LocalDate dataPrevistaFim;
    private String observacao;
    private UserProfileDTO fiscal;
    private Set<UserProfileDTO> engenheiros;
    private UserProfileDTO criador;
    private EnderecoObraDTO endereco;

    public ObraResponseDTO(Obra obra) {
        this.id = obra.getId();
        this.contratante = obra.getContratante();
        this.contratada = obra.getContratada();
        this.projeto = obra.getProjeto();
        this.numeroContrato = obra.getNumeroContrato();
        this.status = obra.getStatus();
        this.dataInicio = obra.getDataInicio();
        this.dataPrevistaFim = obra.getDataPrevistaFim();
        this.observacao = obra.getObservacao();
        this.fiscal = obra.getFiscal() != null ? new UserProfileDTO(obra.getFiscal()) : null;
        this.engenheiros = obra.getEngenheiros()
                .stream()
                .map(UserProfileDTO::new)
                .collect(Collectors.toSet());
        this.criador = obra.getCriador() != null ? new UserProfileDTO(obra.getCriador()) : null;
        this.endereco = obra.getEndereco() != null ? new EnderecoObraDTO(obra.getEndereco()) : null;
    }
}
