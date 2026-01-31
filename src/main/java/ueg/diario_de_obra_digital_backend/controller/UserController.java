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
import ueg.diario_de_obra_digital_backend.dto.EditUserDTO;
import ueg.diario_de_obra_digital_backend.dto.RegisterDTO;
import ueg.diario_de_obra_digital_backend.dto.UserProfileDTO;
import ueg.diario_de_obra_digital_backend.enums.UserRole;
import ueg.diario_de_obra_digital_backend.model.User;
import ueg.diario_de_obra_digital_backend.service.UserService;
import ueg.diario_de_obra_digital_backend.service.exception.ExistentUserException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("register")
    public ResponseEntity<?> register(@RequestBody RegisterDTO data) {
        try {
            userService.newUser(data);
            return ResponseEntity.status(HttpStatus.CREATED).body("Usuário criado com sucesso.");

        } catch (ExistentUserException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());

            // Tipo genérico para o Frontend tratar tanto Ativos quanto Inativos
            response.put("type", "DATA_CONFLICT");

            // Correção: Agora usamos o método no PLURAL (Lista de Strings)
            response.put("conflictFields", e.getConflictFields());

            response.put("user", new UserProfileDTO(e.getUser()));

            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }

    @GetMapping("search")
    public ResponseEntity<Page<UserProfileDTO>> search(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortField,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String term,
            @RequestParam(required = false) UserRole role,
            @RequestParam(defaultValue = "false") boolean hideInactive,
            @AuthenticationPrincipal User currentUser
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortField).descending() : Sort.by(sortField).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserProfileDTO> result = userService.searchUsers(term, role, hideInactive, currentUser.getId(), pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserProfile(id));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getMe(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getUserProfile(user));
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserProfileDTO>> listAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @PutMapping("edit")
    public ResponseEntity<String> edit(@RequestBody EditUserDTO dto, @AuthenticationPrincipal User currentUser) {
        // Passamos o currentUser para validar as permissões de ADMIN no Service
        userService.editUser(dto, currentUser);
        return ResponseEntity.status(HttpStatus.OK).body("Usuário editado com sucesso");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("Status do usuário alterado com sucesso.");
    }
}