package ueg.diario_de_obra_digital_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ueg.diario_de_obra_digital_backend.dto.CreateObraDTO;
import ueg.diario_de_obra_digital_backend.dto.ObraResponseDTO;
import ueg.diario_de_obra_digital_backend.dto.UpdateObraDTO;
import ueg.diario_de_obra_digital_backend.enums.ObraStatus;
import ueg.diario_de_obra_digital_backend.model.User;
import ueg.diario_de_obra_digital_backend.service.ObraService;

@RestController
@RequestMapping("obra")
@RequiredArgsConstructor
public class ObraController {

    private final ObraService obraService;

    /** POST /obra — Criar nova obra (Admin ou Gestor) */
    @PostMapping
    public ResponseEntity<ObraResponseDTO> create(
            @RequestBody CreateObraDTO dto,
            @AuthenticationPrincipal User currentUser
    ) {
        ObraResponseDTO response = obraService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** PUT /obra/{id} — Editar obra (Admin ou Gestor) */
    @PutMapping("/{id}")
    public ResponseEntity<ObraResponseDTO> update(
            @PathVariable Long id,
            @RequestBody UpdateObraDTO dto,
            @AuthenticationPrincipal User currentUser
    ) {
        ObraResponseDTO response = obraService.update(id, dto);
        return ResponseEntity.ok(response);
    }

    /** DELETE /obra/{id} — Exclusão lógica: status → INATIVA (somente Admin) */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deactivate(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        obraService.deactivate(id);
        return ResponseEntity.ok("Obra desativada com sucesso.");
    }

    /** GET /obra — Listar obras paginadas com filtros opcionais */
    @GetMapping
    public ResponseEntity<Page<ObraResponseDTO>> search(
            @RequestParam(defaultValue = "0")     int page,
            @RequestParam(defaultValue = "10")    int size,
            @RequestParam(defaultValue = "projeto") String sortField,
            @RequestParam(defaultValue = "asc")   String sortDir,
            @RequestParam(required = false)       String term,
            @RequestParam(required = false)       ObraStatus status
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(obraService.search(term, status, pageable));
    }

    /** GET /obra/{id} — Buscar obra por ID */
    @GetMapping("/{id}")
    public ResponseEntity<ObraResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(obraService.findById(id));
    }
}
