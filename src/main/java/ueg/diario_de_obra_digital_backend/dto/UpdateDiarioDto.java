package ueg.diario_de_obra_digital_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class UpdateDiarioDto {
  private LocalDate data;
  private String condicaoClimatica;
  private String observacoes;
  private List<MaoDeObraItemDto> maoDeObra;
  private List<ServicoItemDto> servicos;
  private List<EquipamentoItemDto> equipamentos;
  private List<String> visitas;
}
