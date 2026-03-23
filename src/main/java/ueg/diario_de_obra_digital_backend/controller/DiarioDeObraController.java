package ueg.diario_de_obra_digital_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ueg.diario_de_obra_digital_backend.dto.CreateDiarioDto;
import ueg.diario_de_obra_digital_backend.dto.UpdateDiarioDto;
import ueg.diario_de_obra_digital_backend.dto.DiarioResponseDto;
import ueg.diario_de_obra_digital_backend.dto.AvaliacaoDiarioDto;
import ueg.diario_de_obra_digital_backend.model.User;
import ueg.diario_de_obra_digital_backend.service.DiarioDeObraService;
import ueg.diario_de_obra_digital_backend.service.FileStorageService;

import java.util.List;

@RestController
@RequestMapping("diario")
@RequiredArgsConstructor
public class DiarioDeObraController {

  private final DiarioDeObraService diarioDeObraService;
  private final FileStorageService fileStorageService;

  /** POST /diario/{obraId} — Criar novo diário (ENGENHEIRO, ADMIN) */
  @PostMapping(value = "/{obraId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<DiarioResponseDto> create(
          @PathVariable Long obraId,
          @RequestPart("dados") CreateDiarioDto dto,
          @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos,
          @AuthenticationPrincipal User currentUser
  ) {
    DiarioResponseDto response = diarioDeObraService.create(obraId, dto, fotos, currentUser);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /** PUT /diario/{id} — Editar diário (ENGENHEIRO autor ≤ 5 dias, ADMIN) */
  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<DiarioResponseDto> update(
          @PathVariable Long id,
          @RequestPart("dados") UpdateDiarioDto dto,
          @RequestPart(value = "fotos", required = false) List<MultipartFile> novasFotos,
          @AuthenticationPrincipal User currentUser
  ) {
    DiarioResponseDto response = diarioDeObraService.update(id, dto, novasFotos, currentUser);
    return ResponseEntity.ok(response);
  }

  /** PATCH /diario/{id}/aprovar — Aprovar diário (FISCAL, GESTOR, ADMIN) */
  @PatchMapping("/{id}/aprovar")
  public ResponseEntity<DiarioResponseDto> aprovar(
          @PathVariable Long id,
          @RequestBody(required = false) AvaliacaoDiarioDto dto,
          @AuthenticationPrincipal User currentUser
  ) {
    String comentario = dto != null ? dto.getComentario() : null;
    DiarioResponseDto response = diarioDeObraService.aprovar(id, currentUser, comentario);
    return ResponseEntity.ok(response);
  }

  /** PATCH /diario/{id}/reprovar — Reprovar diário (FISCAL, GESTOR, ADMIN) */
  @PatchMapping("/{id}/reprovar")
  public ResponseEntity<DiarioResponseDto> reprovar(
          @PathVariable Long id,
          @RequestBody(required = false) AvaliacaoDiarioDto dto,
          @AuthenticationPrincipal User currentUser
  ) {
    String comentario = dto != null ? dto.getComentario() : null;
    DiarioResponseDto response = diarioDeObraService.reprovar(id, currentUser, comentario);
    return ResponseEntity.ok(response);
  }

  /** DELETE /diario/{id} — Exclusão lógica (ENGENHEIRO autor ≤ 5 dias, ADMIN) */
  @DeleteMapping("/{id}")
  public ResponseEntity<String> delete(
          @PathVariable Long id,
          @AuthenticationPrincipal User currentUser
  ) {
    diarioDeObraService.delete(id, currentUser);
    return ResponseEntity.ok("Diário de obra excluído com sucesso.");
  }

  /** GET /diario/{id} — Buscar diário por ID */
  @GetMapping("/{id}")
  public ResponseEntity<DiarioResponseDto> findById(@PathVariable Long id) {
    return ResponseEntity.ok(diarioDeObraService.findById(id));
  }

  /** GET /diario/obra/{obraId} — Listar diários ativos de uma obra */
  @GetMapping("/obra/{obraId}")
  public ResponseEntity<List<DiarioResponseDto>> listByObra(@PathVariable Long obraId) {
    return ResponseEntity.ok(diarioDeObraService.listByObra(obraId));
  }

  /** GET /diario/fotos/{filename:.+} — Servir imagem armazenada */
  @GetMapping("/fotos/{filename:.+}")
  public ResponseEntity<Resource> servirFoto(@PathVariable String filename) {
    Resource resource = fileStorageService.loadFileAsResource(filename);
    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
  }
}
