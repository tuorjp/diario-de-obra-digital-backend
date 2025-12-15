package ueg.diario_de_obra_digital_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ueg.diario_de_obra_digital_backend.dto.EditUserDTO;
import ueg.diario_de_obra_digital_backend.dto.UserProfileDTO;
import ueg.diario_de_obra_digital_backend.service.exception.DuplicatedTupleException;
import ueg.diario_de_obra_digital_backend.service.exception.UserNotFoundException;
import ueg.diario_de_obra_digital_backend.dto.RegisterDTO;
import ueg.diario_de_obra_digital_backend.model.User;
import ueg.diario_de_obra_digital_backend.repository.UserRepository;

import java.util.List;
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

    // (busca por ID) - pode manter ou remover se não for usar admin vendo outros
    public UserProfileDTO getUserProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));
        return new UserProfileDTO(user);
    }

    // Recebe o usuário já autenticado
    public UserProfileDTO getUserProfile(User user) {
        return new UserProfileDTO(user);
    }

    public void editUser(EditUserDTO dto) {
        Optional<User> existentUserOptional = this.userRepository.findById(dto.getId());

        if (existentUserOptional.isEmpty()) {
            throw new UserNotFoundException("Usuário não encontrado");
        }

        User existentUser = existentUserOptional.get();

        if (dto.getCrea() != null && !dto.getCrea().isBlank()) {
            User userWithCrea = userRepository.findByCrea(dto.getCrea());
            // Se achou alguém com esse CREA e esse alguém NÃO É o usuário que estamos editando
            if (userWithCrea != null && !userWithCrea.getId().equals(existentUser.getId())) {
                throw new DuplicatedTupleException("Este registro CREA já está cadastrado para outro usuário.");
            }
        }

        existentUser.setName(dto.getName());
        existentUser.setRole(dto.getRole());
        existentUser.setLogin(dto.getLogin());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            String encryptedPassword = passwordEncoder.encode(dto.getPassword());
            existentUser.setPassword(encryptedPassword);
        }

        existentUser.setPhone1(dto.getPhone1());
        existentUser.setPhone2(dto.getPhone2());
        existentUser.setCrea(dto.getCrea());
        existentUser.setCreaUf(dto.getCreaUf());
        existentUser.setCpf(dto.getCpf());


        existentUser.setAddress(dto.getAddress());
        existentUser.setAddressNumber(dto.getAddressNumber());
        existentUser.setZipCode(dto.getZipCode());
        existentUser.setComplement(dto.getComplement());
        existentUser.setCity(dto.getCity());
        existentUser.setState(dto.getState());

        this.userRepository.save(existentUser);
    }
}
