package ueg.diario_de_obra_digital_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ueg.diario_de_obra_digital_backend.service.exception.UserNotFoundException;
import ueg.diario_de_obra_digital_backend.dto.RegisterDTO;
import ueg.diario_de_obra_digital_backend.model.User;
import ueg.diario_de_obra_digital_backend.repository.UserRepository;

@RequiredArgsConstructor
@Service
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public void newUser(RegisterDTO data) {
    if(this.userRepository.findByLogin(data.getLogin()) != null) {
      throw new UserNotFoundException("Usuário já existe.");
    }

    String encryptedPassword = passwordEncoder.encode(data.getPassword());
    User newUser = new User(data.getLogin(), data.getName(), encryptedPassword, data.getRole());

    userRepository.save(newUser);
  }
}
