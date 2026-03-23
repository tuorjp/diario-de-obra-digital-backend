package ueg.diario_de_obra_digital_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CreateDiarioDto {
  private LocalDate data;
  private String condicaoClimatica;
  private String observacoes;
  private List<MaoDeObraItemDto> maoDeObra = new ArrayList<>();
  private List<ServicoItemDto> servicos = new ArrayList<>();
  private List<EquipamentoItemDto> equipamentos = new ArrayList<>();
  private List<String> visitas = new ArrayList<>();
}
