package ueg.diario_de_obra_digital_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ueg.diario_de_obra_digital_backend.dto.EditUserDTO;
import ueg.diario_de_obra_digital_backend.dto.RegisterDTO;
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

  @PutMapping("edit")
  public ResponseEntity<String> edit(@RequestBody EditUserDTO dto) {
    userService.editUser(dto);

    return ResponseEntity.status(HttpStatus.OK).body("Usuário editado com sucesso");
  }
}
