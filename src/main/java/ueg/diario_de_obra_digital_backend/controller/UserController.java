package ueg.diario_de_obra_digital_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ueg.diario_de_obra_digital_backend.dto.EditUserDTO;
import ueg.diario_de_obra_digital_backend.dto.RegisterDTO;
import ueg.diario_de_obra_digital_backend.dto.UserProfileDTO;
import ueg.diario_de_obra_digital_backend.model.User;
import ueg.diario_de_obra_digital_backend.service.UserService;

@RestController
@RequestMapping("user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("register")
    public ResponseEntity<String> register(@RequestBody RegisterDTO data) {
        userService.newUser(data);
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuário criado com sucesso.");
    }

    // Novo Endpoint para buscar os dados e preencher a tela
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable Long id) {
        UserProfileDTO userProfile = userService.getUserProfile(id);
        return ResponseEntity.ok(userProfile);
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getMe(@AuthenticationPrincipal User user) {
        // O parâmetro 'user' é injetado automaticamente pelo Spring Security
        // com base no token enviado no header Authorization.
        UserProfileDTO userProfile = userService.getUserProfile(user);
        return ResponseEntity.ok(userProfile);
    }

    @PutMapping("edit")
    public ResponseEntity<String> edit(@RequestBody EditUserDTO dto) {
        userService.editUser(dto);
        return ResponseEntity.status(HttpStatus.OK).body("Usuário editado com sucesso");
    }
}