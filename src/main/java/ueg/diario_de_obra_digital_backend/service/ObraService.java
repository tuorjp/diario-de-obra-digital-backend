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
    public ObraResponseDTO create(CreateObraDTO dto) {
        validateRequiredFields(dto.getContratante(), dto.getContratada(), dto.getProjeto());

        Obra obra = new Obra();
        obra.setContratante(dto.getContratante());
        obra.setContratada(dto.getContratada());
        obra.setProjeto(dto.getProjeto());
        obra.setStatus(ObraStatus.ATIVA);

        applyFiscalAndEngenheiros(obra, dto.getFiscalId(), dto.getEngenheiroIds());

        return new ObraResponseDTO(obraRepository.save(obra));
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    @Transactional
    public ObraResponseDTO update(Long id, UpdateObraDTO dto) {
        Obra obra = findObraOrThrow(id);

        if (StringUtils.hasText(dto.getContratante())) obra.setContratante(dto.getContratante());
        if (StringUtils.hasText(dto.getContratada()))  obra.setContratada(dto.getContratada());
        if (StringUtils.hasText(dto.getProjeto()))     obra.setProjeto(dto.getProjeto());

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

    public ObraResponseDTO findById(Long id) {
        return new ObraResponseDTO(findObraOrThrow(id));
    }

    public Page<ObraResponseDTO> search(String term, ObraStatus status, Pageable pageable) {
        Specification<Obra> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (StringUtils.hasText(term)) {
                String like = "%" + term.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("contratante")), like),
                        cb.like(cb.lower(root.get("contratada")),  like),
                        cb.like(cb.lower(root.get("projeto")),     like)
                ));
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
        }

        // Resolve engenheiros
        Set<User> engenheiros = new HashSet<>();
        if (engenheiroIds != null && !engenheiroIds.isEmpty()) {
            for (Long engenheiroId : engenheiroIds) {
                User engenheiro = userRepository.findById(engenheiroId)
                        .orElseThrow(() -> new UserNotFoundException("Engenheiro não encontrado com id: " + engenheiroId));
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
        if (!StringUtils.hasText(contratante)) missing.add("contratante");
        if (!StringUtils.hasText(contratada))  missing.add("contratada");
        if (!StringUtils.hasText(projeto))     missing.add("projeto");

        if (!missing.isEmpty()) {
            throw new IllegalArgumentException(
                    "Campos obrigatórios não preenchidos: " + String.join(", ", missing));
        }
    }
}
