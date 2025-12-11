package ueg.diario_de_obra_digital_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ueg.diario_de_obra_digital_backend.dto.EditUserDTO;
import ueg.diario_de_obra_digital_backend.dto.RegisterDTO;
import ueg.diario_de_obra_digital_backend.model.User;
import ueg.diario_de_obra_digital_backend.service.UserService;

import java.util.List;

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
  public ResponseEntity<EditUserDTO> edit(@RequestBody EditUserDTO dto) {
    userService.editUser(dto);

    dto.setPassword(null);

    return ResponseEntity.status(HttpStatus.OK).body(dto);
  }

  @GetMapping("find-by-login/{login}")
  public ResponseEntity<User> findByLogin(@PathVariable String login) {
    User user = userService.findByLogin(login);
    return ResponseEntity.ok().body(user);
  }

  @GetMapping("find-by-id/{id}")
  public ResponseEntity<User> findById(@PathVariable Long id) {
    User user = userService.findById(id);
    return ResponseEntity.ok().body(user);
  }

  @GetMapping("list")
  public ResponseEntity<List<User>> listAll() {
    List<User> users = userService.findAll();
    return ResponseEntity.ok().body(users);
  }
}
