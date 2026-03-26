package ueg.diario_de_obra_digital_backend.dto;

import lombok.Getter;
import ueg.diario_de_obra_digital_backend.enums.DiarioStatus;
import ueg.diario_de_obra_digital_backend.model.DiarioDeObra;
import ueg.diario_de_obra_digital_backend.model.DiarioEquipamento;
import ueg.diario_de_obra_digital_backend.model.DiarioMaoDeObra;
import ueg.diario_de_obra_digital_backend.model.DiarioServico;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class DiarioResponseDto {

  private final Long id;

  // Dados da Obra (pré-preenchimento automático)
  private final Long obraId;
  private final String contratante;
  private final String contratada;
  private final String projeto;

  // Dados do Diário
  private final LocalDate data;
  private final String condicaoClimatica;
  private final String observacoes;
  private final DiarioStatus status;
  private final List<String> fotos;
  private final List<OcorrenciaResponseDto> ocorrencias;

  // Itens do Diário
  private final List<MaoDeObraItemResponseDTO> maoDeObra;
  private final List<ServicoItemResponseDTO> servicos;
  private final List<EquipamentoItemResponseDTO> equipamentos;

  // Atores
  private final Long autorId;
  private final String autorNome;
  private final Long validadorId;
  private final String validadorNome;
  private final String comentarioValidacao;

  public DiarioResponseDto(DiarioDeObra d) {
    this.id = d.getId();
    this.obraId = d.getObra().getId();
    this.contratante = d.getObra().getContratante();
    this.contratada = d.getObra().getContratada();
    this.projeto = d.getObra().getProjeto();
    this.data = d.getData();
    this.condicaoClimatica = d.getCondicaoClimatica();
    this.observacoes = d.getObservacoes();
    this.status = d.getStatus();
    this.fotos = d.getFotos();
    this.ocorrencias = mapOcorrencias(d.getOcorrencias());
    this.autorId = d.getAutor() != null ? d.getAutor().getId() : null;
    this.autorNome = d.getAutor() != null ? d.getAutor().getName() : null;
    this.validadorId = d.getValidador() != null ? d.getValidador().getId() : null;
    this.validadorNome = d.getValidador() != null ? d.getValidador().getName() : null;
    this.comentarioValidacao = d.getComentarioValidacao();
    this.maoDeObra = mapMaoDeObra(d.getMaoDeObra());
    this.servicos = mapServicos(d.getServicosExecutados());
    this.equipamentos = mapEquipamentos(d.getEquipamentos());
  }

  private List<OcorrenciaResponseDto> mapOcorrencias(List<ueg.diario_de_obra_digital_backend.model.Ocorrencia> items) {
    if (items == null) return List.of();
    return items.stream()
            .map(o -> new OcorrenciaResponseDto(
                    o.getId(),
                    o.getTipo(),
                    o.getOcorrencia(),
                    o.getAutor() != null ? o.getAutor().getId() : null,
                    o.getAutor() != null ? o.getAutor().getName() : null
            ))
            .collect(Collectors.toList());
  }

  private List<MaoDeObraItemResponseDTO> mapMaoDeObra(Set<DiarioMaoDeObra> items) {
    if (items == null) return List.of();
    return items.stream()
            .map(i -> new MaoDeObraItemResponseDTO(i.getMaoDeObra().getId(), i.getMaoDeObra().getNome(), i.getQuantidade()))
            .collect(Collectors.toList());
  }

  private List<ServicoItemResponseDTO> mapServicos(Set<DiarioServico> items) {
    if (items == null) return List.of();
    return items.stream()
            .map(i -> new ServicoItemResponseDTO(i.getServico().getId(), i.getServico().getNome(), i.getServico().getUnidadeMedida(), i.getQuantidade()))
            .collect(Collectors.toList());
  }

  private List<EquipamentoItemResponseDTO> mapEquipamentos(Set<DiarioEquipamento> items) {
    if (items == null) return List.of();
    return items.stream()
            .map(i -> new EquipamentoItemResponseDTO(i.getEquipamento().getId(), i.getEquipamento().getNome(), i.getQuantidade()))
            .collect(Collectors.toList());
  }

  // ── Inner response records ────────────────────────────────────────────────

  @Getter
  public static class MaoDeObraItemResponseDTO {
    private final Long maoDeObraId;
    private final String nome;
    private final Integer quantidade;
    public MaoDeObraItemResponseDTO(Long id, String nome, Integer qtd) {
      this.maoDeObraId = id;
      this.nome = nome;
      this.quantidade = qtd;
    }
  }

  @Getter
  public static class ServicoItemResponseDTO {
    private final Long servicoId;
    private final String nome;
    private final String unidadeMedida;
    private final Double quantidade;
    public ServicoItemResponseDTO(Long id, String nome, String unidade, Double qtd) {
      this.servicoId = id;
      this.nome = nome;
      this.unidadeMedida = unidade;
      this.quantidade = qtd;
    }
  }

  @Getter
  public static class EquipamentoItemResponseDTO {
    private final Long equipamentoId;
    private final String nome;
    private final Integer quantidade;
    public EquipamentoItemResponseDTO(Long id, String nome, Integer qtd) {
      this.equipamentoId = id;
      this.nome = nome;
      this.quantidade = qtd;
    }
  }

  @Getter
  public static class OcorrenciaResponseDto {
    private final Long id;
    private final String tipo;
    private final String ocorrencia;
    private final Long autorId;
    private final String autorNome;
    public OcorrenciaResponseDto(Long id, String tipo, String ocorrencia, Long autorId, String autorNome) {
      this.id = id;
      this.tipo = tipo;
      this.ocorrencia = ocorrencia;
      this.autorId = autorId;
      this.autorNome = autorNome;
    }
  }
}
