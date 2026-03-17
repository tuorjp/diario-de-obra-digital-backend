package ueg.diario_de_obra_digital_backend.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ueg.diario_de_obra_digital_backend.dto.CreateObraDTO;
import ueg.diario_de_obra_digital_backend.dto.ObraResponseDTO;
import ueg.diario_de_obra_digital_backend.dto.UpdateObraDTO;
import ueg.diario_de_obra_digital_backend.enums.ObraStatus;
import ueg.diario_de_obra_digital_backend.model.Obra;
import ueg.diario_de_obra_digital_backend.model.EnderecoObra;
import ueg.diario_de_obra_digital_backend.model.User;
import ueg.diario_de_obra_digital_backend.repository.ObraRepository;
import ueg.diario_de_obra_digital_backend.repository.UserRepository;
import ueg.diario_de_obra_digital_backend.service.exception.DuplicateRoleAssignmentException;
import ueg.diario_de_obra_digital_backend.service.exception.ObraNotFoundException;
import ueg.diario_de_obra_digital_backend.service.exception.UserNotFoundException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ObraService {

    private final ObraRepository obraRepository;
    private final UserRepository userRepository;

    // ─── CREATE ───────────────────────────────────────────────────────────────

    @Transactional
    public ObraResponseDTO create(CreateObraDTO dto, User currentUser) {
        validateRequiredFields(dto.getContratante(), dto.getContratada(), dto.getProjeto());

        Obra obra = new Obra();
        obra.setContratante(dto.getContratante());
        obra.setContratada(dto.getContratada());
        obra.setProjeto(dto.getProjeto());
        obra.setNumeroContrato(dto.getNumeroContrato());
        obra.setStatus(ObraStatus.ATIVA);
        obra.setDataInicio(dto.getDataInicio());
        obra.setDataPrevistaFim(dto.getDataPrevistaFim());
        obra.setObservacao(dto.getObservacao());
        obra.setCriador(currentUser);

        if (dto.getEndereco() != null) {
            EnderecoObra endereco = new EnderecoObra();
            endereco.setEndereco(dto.getEndereco().getEndereco());
            endereco.setComplemento(dto.getEndereco().getComplemento());
            endereco.setNumero(dto.getEndereco().getNumero());
            endereco.setCidade(dto.getEndereco().getCidade());
            endereco.setCep(dto.getEndereco().getCep());
            endereco.setUf(dto.getEndereco().getUf());
            endereco.setObra(obra);
            obra.setEndereco(endereco);
        }

        applyFiscalAndEngenheiros(obra, dto.getFiscalId(), dto.getEngenheiroIds());

        return new ObraResponseDTO(obraRepository.save(obra));
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    @Transactional
    public ObraResponseDTO update(Long id, UpdateObraDTO dto, User currentUser) {
        Obra obra = findObraOrThrow(id);

        if (obra.getCriador() != null && !obra.getCriador().getId().equals(currentUser.getId()) && currentUser.getRole() != ueg.diario_de_obra_digital_backend.enums.UserRole.ADMIN) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Somente o criador pode alterar esta obra");
        }

        if (StringUtils.hasText(dto.getContratante()))
            obra.setContratante(dto.getContratante());
        if (StringUtils.hasText(dto.getContratada()))
            obra.setContratada(dto.getContratada());
        if (StringUtils.hasText(dto.getProjeto()))
            obra.setProjeto(dto.getProjeto());
        if (dto.getNumeroContrato() != null)
            obra.setNumeroContrato(dto.getNumeroContrato());
        if (dto.getDataInicio() != null)
            obra.setDataInicio(dto.getDataInicio());
        if (dto.getDataPrevistaFim() != null)
            obra.setDataPrevistaFim(dto.getDataPrevistaFim());

        if (dto.getObservacao() != null) {
            obra.setObservacao(dto.getObservacao());
        }

        if (dto.getEndereco() != null) {
            EnderecoObra endereco = obra.getEndereco();
            if (endereco == null) {
                endereco = new EnderecoObra();
                endereco.setObra(obra);
            }
            if (StringUtils.hasText(dto.getEndereco().getEndereco()))
                endereco.setEndereco(dto.getEndereco().getEndereco());
            if (dto.getEndereco().getComplemento() != null)
                endereco.setComplemento(dto.getEndereco().getComplemento());
            if (dto.getEndereco().getNumero() != null)
                endereco.setNumero(dto.getEndereco().getNumero());
            if (StringUtils.hasText(dto.getEndereco().getCidade()))
                endereco.setCidade(dto.getEndereco().getCidade());
            if (StringUtils.hasText(dto.getEndereco().getCep()))
                endereco.setCep(dto.getEndereco().getCep());
            if (StringUtils.hasText(dto.getEndereco().getUf()))
                endereco.setUf(dto.getEndereco().getUf());
            obra.setEndereco(endereco);
        }

        applyFiscalAndEngenheiros(obra, dto.getFiscalId(), dto.getEngenheiroIds());

        return new ObraResponseDTO(obraRepository.save(obra));
    }

    // ─── LOGICAL DELETE (Admin only — enforced at controller/security level) ──

    @Transactional
    public void deactivate(Long id) {
        Obra obra = findObraOrThrow(id);
        obra.setStatus(ObraStatus.INATIVA);
        obraRepository.save(obra);
    }

    // ─── READS ────────────────────────────────────────────────────────────────

    public ObraResponseDTO findById(Long id, User currentUser) {
        Obra obra = findObraOrThrow(id);

        if (currentUser.getRole() == ueg.diario_de_obra_digital_backend.enums.UserRole.GESTOR) {
            if (obra.getCriador() != null && !currentUser.getId().equals(obra.getCriador().getId())) {
                throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Acesso negado");
            }
        } else if (currentUser.getRole() == ueg.diario_de_obra_digital_backend.enums.UserRole.FISCAL || 
                   currentUser.getRole() == ueg.diario_de_obra_digital_backend.enums.UserRole.ENGENHEIRO) {
            boolean isFiscal = obra.getFiscal() != null && obra.getFiscal().getId().equals(currentUser.getId());
            boolean isEngenheiro = obra.getEngenheiros() != null && obra.getEngenheiros().stream().anyMatch(e -> e.getId().equals(currentUser.getId()));
            if (!isFiscal && !isEngenheiro) {
                throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Acesso negado");
            }
        }

        return new ObraResponseDTO(obra);
    }

    public Page<ObraResponseDTO> search(String term, String searchField, ObraStatus status, Pageable pageable, User currentUser) {
        Specification<Obra> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (currentUser.getRole() == ueg.diario_de_obra_digital_backend.enums.UserRole.GESTOR) {
                predicates.add(cb.equal(root.get("criador"), currentUser));
            } else if (currentUser.getRole() == ueg.diario_de_obra_digital_backend.enums.UserRole.FISCAL || 
                       currentUser.getRole() == ueg.diario_de_obra_digital_backend.enums.UserRole.ENGENHEIRO) {
                predicates.add(cb.or(
                        cb.equal(root.get("fiscal"), currentUser),
                        cb.isMember(currentUser, root.get("engenheiros"))
                ));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (StringUtils.hasText(term) && StringUtils.hasText(searchField)) {
                if (searchField.equals("id")) {
                    try {
                        Long idTerm = Long.parseLong(term.trim());
                        predicates.add(cb.equal(root.get("id"), idTerm));
                    } catch (NumberFormatException e) {
                        // If searching by ID but term isn't a number, force no results
                        predicates.add(cb.disjunction());
                    }
                } else if (searchField.equals("status")) {
                    try {
                        ObraStatus statusTerm = ObraStatus.valueOf(term.trim().toUpperCase());
                        predicates.add(cb.equal(root.get("status"), statusTerm));
                    } catch (IllegalArgumentException e) {
                        // If searching by status but term isn't a valid status, force no results
                        predicates.add(cb.disjunction());
                    }
                } else {
                    String like = "%" + term.toLowerCase() + "%";
                    predicates.add(cb.like(cb.lower(root.get(searchField)), like));
                }
            }

            return predicates.isEmpty() ? cb.conjunction()
                    : cb.and(predicates.toArray(new Predicate[0]));
        };

        return obraRepository.findAll(spec, pageable).map(ObraResponseDTO::new);
    }

    // ─── PRIVATE HELPERS ──────────────────────────────────────────────────────

    private Obra findObraOrThrow(Long id) {
        return obraRepository.findById(id)
                .orElseThrow(() -> new ObraNotFoundException("Obra não encontrada com id: " + id));
    }

    /**
     * Resolve e atribui o fiscal e os engenheiros à obra, aplicando a RN 07:
     * o mesmo usuário não pode ser fiscal e engenheiro simultaneamente.
     */
    private void applyFiscalAndEngenheiros(Obra obra, Long fiscalId, Set<Long> engenheiroIds) {
        // Resolve fiscal
        User fiscal = null;
        if (fiscalId != null) {
            fiscal = userRepository.findById(fiscalId)
                    .orElseThrow(() -> new UserNotFoundException("Fiscal não encontrado com id: " + fiscalId));
            if (!fiscal.isEnabled()) {
                throw new IllegalArgumentException("Não é possível atribuir um usuário inativo como fiscal");
            }
        }

        // Resolve engenheiros
        Set<User> engenheiros = new HashSet<>();
        if (engenheiroIds != null && !engenheiroIds.isEmpty()) {
            for (Long engenheiroId : engenheiroIds) {
                User engenheiro = userRepository.findById(engenheiroId)
                        .orElseThrow(
                                () -> new UserNotFoundException("Engenheiro não encontrado com id: " + engenheiroId));
                if (!engenheiro.isEnabled()) {
                    throw new IllegalArgumentException("Não é possível atribuir um usuário inativo como engenheiro");
                }
                engenheiros.add(engenheiro);
            }
        }

        // RN 07 – fiscal não pode ser também engenheiro na mesma obra
        if (fiscal != null && !engenheiros.isEmpty()) {
            final Long fiscalIdFinal = fiscal.getId();
            boolean conflict = engenheiros.stream()
                    .anyMatch(e -> e.getId().equals(fiscalIdFinal));
            if (conflict) {
                throw new DuplicateRoleAssignmentException(
                        "O mesmo usuário não pode ser atribuído como Fiscal e Engenheiro na mesma obra (RN 07).");
            }
        }

        obra.setFiscal(fiscal);
        obra.setEngenheiros(engenheiros);
    }

    private void validateRequiredFields(String contratante, String contratada, String projeto) {
        List<String> missing = new ArrayList<>();
        if (!StringUtils.hasText(contratante))
            missing.add("contratante");
        if (!StringUtils.hasText(contratada))
            missing.add("contratada");
        if (!StringUtils.hasText(projeto))
            missing.add("projeto");

        if (!missing.isEmpty()) {
            throw new IllegalArgumentException(
                    "Campos obrigatórios não preenchidos: " + String.join(", ", missing));
        }
    }
}
