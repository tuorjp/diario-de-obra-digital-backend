package ueg.diario_de_obra_digital_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ueg.diario_de_obra_digital_backend.enums.DiarioStatus;
import ueg.diario_de_obra_digital_backend.enums.ObraStatus;
import ueg.diario_de_obra_digital_backend.enums.UserRole;
import ueg.diario_de_obra_digital_backend.model.*;
import ueg.diario_de_obra_digital_backend.repository.*;
import ueg.diario_de_obra_digital_backend.service.exception.*;
import ueg.diario_de_obra_digital_backend.dto.CreateDiarioDto;
import ueg.diario_de_obra_digital_backend.dto.UpdateDiarioDto;
import ueg.diario_de_obra_digital_backend.dto.DiarioResponseDto;
import ueg.diario_de_obra_digital_backend.dto.MaoDeObraItemDto;
import ueg.diario_de_obra_digital_backend.dto.ServicoItemDto;
import ueg.diario_de_obra_digital_backend.dto.EquipamentoItemDto;

import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.springframework.data.jpa.domain.Specification;

import static org.springframework.data.jpa.domain.Specification.where;
import static ueg.diario_de_obra_digital_backend.specification.DiarioDeObraSpecification.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class DiarioDeObraService {

  private final DiarioDeObraRepository diarioDeObraRepository;
  private final ObraRepository obraRepository;
  private final MaoDeObraRepository maoDeObraRepository;
  private final ServicoRepository servicoRepository;
  private final EquipamentoRepository equipamentoRepository;
  private final FileStorageService fileStorageService;
  private final EntityManager entityManager;

  // ─── CREATE ──────────────────────────────────────────────────────────────────

  @Transactional
  public DiarioResponseDto create(Long obraId, CreateDiarioDto dto, List<MultipartFile> fotos, User currentUser) {
    Obra obra = findObraOrThrow(obraId);

    // Pré-condição: obra deve estar ativa
    if (obra.getStatus() != ObraStatus.ATIVA) {
      throw new IllegalArgumentException("Não é possível criar diários em uma obra inativa.");
    }

    // Pré-condição: engenheiro deve estar vinculado à obra (admin pode criar em qualquer obra)
    if (currentUser.getRole() == UserRole.ENGENHEIRO) {
      boolean vinculado = obra.getEngenheiros().stream()
          .anyMatch(e -> e.getId().equals(currentUser.getId()));
      if (!vinculado) {
        throw new DiarioEditForbiddenEx("Você não está vinculado a esta obra.");
      }
    }

    // RN: unicidade de diário por obra e data
    if (dto.getData() == null) {
      throw new IllegalArgumentException("O campo 'data' é obrigatório.");
    }
    diarioDeObraRepository.findByObraAndData(obra, dto.getData()).ifPresent(d -> {
      throw new DiarioDuplicadoEx(
          "Já existe um diário para esta obra na data " + dto.getData() + ".");
    });

    // Valida campos obrigatórios
    if (!StringUtils.hasText(dto.getCondicaoClimatica())) {
      throw new IllegalArgumentException("O campo 'condição climática' é obrigatório.");
    }
    if (dto.getMaoDeObra() == null || dto.getMaoDeObra().isEmpty()) {
      throw new IllegalArgumentException("Informe pelo menos um quantitativo de mão de obra.");
    }
    boolean temServicoOuEquipamento =
        (dto.getServicos() != null && !dto.getServicos().isEmpty()) ||
        (dto.getEquipamentos() != null && !dto.getEquipamentos().isEmpty());
    if (!temServicoOuEquipamento) {
      throw new IllegalArgumentException("Informe pelo menos um serviço executado ou equipamento utilizado.");
    }
    if (fotos == null || fotos.isEmpty() || fotos.stream().allMatch(f -> f == null || f.isEmpty())) {
      throw new IllegalArgumentException("Adicione pelo menos uma foto que evidencie os serviços.");
    }

    DiarioDeObra diario = new DiarioDeObra();
    diario.setObra(obra);
    diario.setAutor(currentUser);
    diario.setData(dto.getData());
    diario.setCondicaoClimatica(dto.getCondicaoClimatica());
    diario.setObservacoes(dto.getObservacoes());
    diario.setStatus(DiarioStatus.AGUARDANDO_AVALIACAO);
    if (dto.getVisitas() != null) diario.getVisitas().addAll(dto.getVisitas());

    // Salva as fotos no storage e armazena os nomes únicos no diário
    for (MultipartFile foto : fotos) {
      if (foto != null && !foto.isEmpty()) {
        String fileName = fileStorageService.storeFile(foto);
        diario.getFotos().add(fileName);
      }
    }

    // Mão de obra
    for (MaoDeObraItemDto item : dto.getMaoDeObra()) {
      MaoDeObra mdo = maoDeObraRepository.findById(item.getMaoDeObraId())
          .orElseThrow(() -> new IllegalArgumentException("Mão de obra não encontrada: id=" + item.getMaoDeObraId()));
      DiarioMaoDeObra dmd = new DiarioMaoDeObra();
      dmd.setDiario(diario);
      dmd.setMaoDeObra(mdo);
      dmd.setQuantidade(item.getQuantidade());
      diario.getMaoDeObra().add(dmd);
    }

    // Serviços
    if (dto.getServicos() != null) {
      for (ServicoItemDto item : dto.getServicos()) {
        Servico servico = servicoRepository.findById(item.getServicoId())
            .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado: id=" + item.getServicoId()));
        DiarioServico ds = new DiarioServico();
        ds.setDiario(diario);
        ds.setServico(servico);
        ds.setQuantidade(item.getQuantidade());
        diario.getServicosExecutados().add(ds);
      }
    }

    // Equipamentos
    if (dto.getEquipamentos() != null) {
      for (EquipamentoItemDto item : dto.getEquipamentos()) {
        Equipamento equip = equipamentoRepository.findById(item.getEquipamentoId())
            .orElseThrow(() -> new IllegalArgumentException("Equipamento não encontrado: id=" + item.getEquipamentoId()));
        DiarioEquipamento de = new DiarioEquipamento();
        de.setDiario(diario);
        de.setEquipamento(equip);
        de.setQuantidade(item.getQuantidade());
        diario.getEquipamentos().add(de);
      }
    }

    return new DiarioResponseDto(diarioDeObraRepository.save(diario));
  }

  // ─── UPDATE ──────────────────────────────────────────────────────────────────

  @Transactional
  public DiarioResponseDto update(Long id, UpdateDiarioDto dto, List<MultipartFile> novasFotos, User currentUser) {
    DiarioDeObra diario = findDiarioOrThrow(id);

    // Verifica se é o autor ou admin
    checkEditPermission(diario, currentUser);

    // Atualiza campos simples se fornecidos
    if (dto.getData() != null) {
      // Verifica unicidade nova data (ignora se data não mudou)
      if (!dto.getData().equals(diario.getData())) {
        diarioDeObraRepository.findByObraAndData(diario.getObra(), dto.getData()).ifPresent(d -> {
          throw new DiarioDuplicadoEx(
              "Já existe um diário para esta obra na data " + dto.getData() + ".");
        });
      }
      diario.setData(dto.getData());
    }
    if (StringUtils.hasText(dto.getCondicaoClimatica())) {
      diario.setCondicaoClimatica(dto.getCondicaoClimatica());
    }
    if (dto.getObservacoes() != null) {
      diario.setObservacoes(dto.getObservacoes());
    }
    if (dto.getVisitas() != null) {
      diario.getVisitas().clear();
      diario.getVisitas().addAll(dto.getVisitas());
    }

    // Substitui itens de mão de obra se fornecidos
    if (dto.getMaoDeObra() != null) {
      diario.getMaoDeObra().clear();
      for (MaoDeObraItemDto item : dto.getMaoDeObra()) {
        MaoDeObra mdo = maoDeObraRepository.findById(item.getMaoDeObraId())
            .orElseThrow(() -> new IllegalArgumentException("Mão de obra não encontrada: id=" + item.getMaoDeObraId()));
        DiarioMaoDeObra dmd = new DiarioMaoDeObra();
        dmd.setDiario(diario);
        dmd.setMaoDeObra(mdo);
        dmd.setQuantidade(item.getQuantidade());
        diario.getMaoDeObra().add(dmd);
      }
    }

    // Substitui serviços se fornecidos
    if (dto.getServicos() != null) {
      diario.getServicosExecutados().clear();
      for (ServicoItemDto item : dto.getServicos()) {
        Servico servico = servicoRepository.findById(item.getServicoId())
            .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado: id=" + item.getServicoId()));
        DiarioServico ds = new DiarioServico();
        ds.setDiario(diario);
        ds.setServico(servico);
        ds.setQuantidade(item.getQuantidade());
        diario.getServicosExecutados().add(ds);
      }
    }

    // Substitui equipamentos se fornecidos
    if (dto.getEquipamentos() != null) {
      diario.getEquipamentos().clear();
      for (EquipamentoItemDto item : dto.getEquipamentos()) {
        Equipamento equip = equipamentoRepository.findById(item.getEquipamentoId())
            .orElseThrow(() -> new IllegalArgumentException("Equipamento não encontrado: id=" + item.getEquipamentoId()));
        DiarioEquipamento de = new DiarioEquipamento();
        de.setDiario(diario);
        de.setEquipamento(equip);
        de.setQuantidade(item.getQuantidade());
        diario.getEquipamentos().add(de);
      }
    }

    // Adiciona novas fotos se fornecidas (não remove as antigas)
    if (novasFotos != null) {
      for (MultipartFile foto : novasFotos) {
        if (foto != null && !foto.isEmpty()) {
          String fileName = fileStorageService.storeFile(foto);
          diario.getFotos().add(fileName);
        }
      }
    }

    return new DiarioResponseDto(diarioDeObraRepository.save(diario));
  }

  // ─── APROVAR / REPROVAR ──────────────────────────────────────────────────────

  @Transactional
  public DiarioResponseDto aprovar(Long id, User validador, String comentario) {
    return avaliar(id, validador, comentario, DiarioStatus.VALIDO);
  }

  @Transactional
  public DiarioResponseDto reprovar(Long id, User validador, String comentario) {
    return avaliar(id, validador, comentario, DiarioStatus.INVALIDO);
  }

  private DiarioResponseDto avaliar(Long id, User validador, String comentario, DiarioStatus novoStatus) {
    if (validador.getRole() != UserRole.FISCAL && validador.getRole() != UserRole.GESTOR
        && validador.getRole() != UserRole.ADMIN) {
      throw new DiarioEditForbiddenEx("Apenas Fiscal, Gestor ou Administrador podem avaliar diários.");
    }

    DiarioDeObra diario = findDiarioOrThrow(id);

    if (diario.getStatus() != DiarioStatus.AGUARDANDO_AVALIACAO) {
      throw new DiarioStatusEx(
          "Só é possível avaliar diários com status 'Aguardando Avaliação'. Status atual: " + diario.getStatus());
    }

    diario.setStatus(novoStatus);
    diario.setValidador(validador);
    diario.setComentarioValidacao(comentario);

    return new DiarioResponseDto(diarioDeObraRepository.save(diario));
  }

  // ─── DELETE ──────────────────────────────────────────────────────────────────

  @Transactional
  public void delete(Long id, User currentUser) {
    DiarioDeObra diario = findDiarioOrThrow(id);
    checkEditPermission(diario, currentUser);
    // @SQLDelete no model faz delete lógico automaticamente
    diarioDeObraRepository.deleteById(id);
  }

  // ─── READS ───────────────────────────────────────────────────────────────────

  public DiarioResponseDto findById(Long id) {
    DiarioDeObra diario = findDiarioOrThrow(id);
    return new DiarioResponseDto(diario);
  }

  public Page<DiarioResponseDto> list(Long obraId, LocalDate data, Long autorId, Pageable pageable) {
    Specification<DiarioDeObra> spec = isNotDeleted();

    if (obraId != null) {
      spec = spec.and(obraIdSelected(obraId));
    }
    if (data != null) {
      spec = spec.and(dataSelected(data));
    }
    if (autorId != null) {
      spec = spec.and(autorIdSelected(autorId));
    }

    Page<DiarioDeObra> diarios = diarioDeObraRepository.findAll(spec, pageable);
    return diarios.map(DiarioResponseDto::new);
  }

  // Lista todos até os com deletado = true
  public List<DiarioDeObra> listAllWithDeleted() {
    return diarioDeObraRepository.findAll();
  }

  // Lista todos os que tem deletado = false
  public List<DiarioDeObra> listAllActive() {
    Session session = entityManager.unwrap(Session.class);
    session.enableFilter("deletedDiarioFilter");
    List<DiarioDeObra> listaAtivos = diarioDeObraRepository.findAll();
    session.disableFilter("deletedDiarioFilter");
    return listaAtivos;
  }

  public void logicDelete(Long id) {
    diarioDeObraRepository.deleteById(id);
  }

  // ─── PRIVATE HELPERS ─────────────────────────────────────────────────────────

  private DiarioDeObra findDiarioOrThrow(Long id) {
    return diarioDeObraRepository.findById(id)
        .orElseThrow(() -> new DiarioNotFoundEx("Diário de obra não encontrado com id: " + id));
  }

  private Obra findObraOrThrow(Long obraId) {
    return obraRepository.findById(obraId)
        .orElseThrow(() -> new ObraNotFoundException("Obra não encontrada com id: " + obraId));
  }

  /**
   * Verifica se o usuário tem permissão para editar ou excluir o diário:
   * - Admin pode sempre.
   * - Engenheiro/outros: apenas se for o autor E o diário tiver no máximo 5 dias.
   */
  private void checkEditPermission(DiarioDeObra diario, User currentUser) {
    if (currentUser.getRole() == UserRole.ADMIN) return;

    if (!diario.getAutor().getId().equals(currentUser.getId())) {
      throw new DiarioEditForbiddenEx("Apenas o autor do diário ou um Administrador pode editá-lo.");
    }

    long diasDesde = java.time.temporal.ChronoUnit.DAYS.between(diario.getData(), LocalDate.now());
    if (diasDesde > 5) {
      throw new DiarioEditForbiddenEx(
          "Engenheiros só podem editar diários criados há no máximo 5 dias. Este diário foi criado há " + diasDesde + " dias.");
    }
  }
}
