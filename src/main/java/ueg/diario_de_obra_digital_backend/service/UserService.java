package ueg.diario_de_obra_digital_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ueg.diario_de_obra_digital_backend.dto.EditUserDTO;
import ueg.diario_de_obra_digital_backend.service.exception.DuplicatedTupleException;
import ueg.diario_de_obra_digital_backend.service.exception.UserNotFoundException;
import ueg.diario_de_obra_digital_backend.dto.RegisterDTO;
import ueg.diario_de_obra_digital_backend.model.User;
import ueg.diario_de_obra_digital_backend.repository.UserRepository;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public void newUser(RegisterDTO data) {
    if(this.userRepository.findByLogin(data.getLogin()) != null) {
      throw new DuplicatedTupleException("Usuário já cadastrado.");
    }

    String encryptedPassword = passwordEncoder.encode(data.getPassword());
    User newUser = new User(data.getLogin(), data.getName(), encryptedPassword, data.getRole());

    userRepository.save(newUser);
  }

  public void editUser(EditUserDTO user) {
    Optional<User> existentUserOptional = this.userRepository.findById(user.getId());

    if (existentUserOptional.isEmpty()) {
      throw new UserNotFoundException("Usuário não encontrado");
    }

    User existentUser = existentUserOptional.get();
    String encryptedPassword = passwordEncoder.encode(user.getPassword());
    existentUser.setName(user.getName());
    existentUser.setRole(user.getRole());
    existentUser.setPassword(encryptedPassword);
    existentUser.setLogin(user.getLogin());

    this.userRepository.save(existentUser);
  }
}
