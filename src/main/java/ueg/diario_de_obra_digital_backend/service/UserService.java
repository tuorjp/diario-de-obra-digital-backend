package ueg.diario_de_obra_digital_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ueg.diario_de_obra_digital_backend.dto.EditUserDTO;
import ueg.diario_de_obra_digital_backend.dto.RegisterDTO;
import ueg.diario_de_obra_digital_backend.dto.UserProfileDTO;
import ueg.diario_de_obra_digital_backend.enums.UserRole;
import ueg.diario_de_obra_digital_backend.model.User;
import ueg.diario_de_obra_digital_backend.repository.UserRepository;
import ueg.diario_de_obra_digital_backend.service.exception.DuplicatedTupleException;
import ueg.diario_de_obra_digital_backend.service.exception.ExistentUserException;
import ueg.diario_de_obra_digital_backend.service.exception.UserNotFoundException;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void newUser(RegisterDTO data) {
        List<String> conflicts = new ArrayList<>();
        User foundUser = null;

        // 1. VERIFICAÇÃO DE LOGIN (E-MAIL)
        Optional<User> existingByLogin = userRepository.findUserByLogin(data.getLogin());
        if (existingByLogin.isPresent()) {
            foundUser = existingByLogin.get();
            conflicts.add("Login");
        }

        // 2. VERIFICAÇÃO DE CPF
        if (data.getCpf() != null && !data.getCpf().isBlank()) {
            Optional<User> existingByCpf = userRepository.findUserByCpf(data.getCpf());
            if (existingByCpf.isPresent()) {
                // Se ainda não achamos usuário pelo login, definimos este como o encontrado
                if (foundUser == null) {
                    foundUser = existingByCpf.get();
                }
                conflicts.add("CPF");
            }
        }

        // 3. VERIFICAÇÃO DE CREA
        if (data.getCrea() != null && !data.getCrea().isBlank()) {
            User existingByCrea = userRepository.findByCrea(data.getCrea());
            if (existingByCrea != null) {
                if (foundUser == null) {
                    foundUser = existingByCrea;
                }
                conflicts.add("CREA");
            }
        }

        // SE HOUVE ALGUM CONFLITO (Seja em usuário ATIVO ou INATIVO)
        if (!conflicts.isEmpty() && foundUser != null) {
            // Lança a exceção passando a LISTA de campos conflitantes e o usuário encontrado.
            // O Frontend decidirá a mensagem baseada no status (enabled) do usuário retornado.
            throw new ExistentUserException("Dados já existentes no sistema.", foundUser, conflicts);
        }

        //  SE PASSOU POR TUDO SEM CONFLITOS, CRIA O NOVO 
        User userToSave = new User();
        userToSave.setName(data.getName());
        userToSave.setLogin(data.getLogin());
        userToSave.setPassword(passwordEncoder.encode(data.getPassword()));
        userToSave.setRole(data.getRole());
        userToSave.setEnabled(true);
        userToSave.setPhone1(data.getPhone1());
        userToSave.setPhone2(data.getPhone2());
        userToSave.setCrea(data.getCrea());
        userToSave.setCreaUf(data.getCreaUf());
        userToSave.setCpf(data.getCpf());
        userToSave.setAddress(data.getAddress());
        userToSave.setAddressNumber(data.getAddressNumber());
        userToSave.setZipCode(data.getZipCode());
        userToSave.setComplement(data.getComplement());
        userToSave.setCity(data.getCity());
        userToSave.setState(data.getState());

        userRepository.save(userToSave);
    }

    public Page<UserProfileDTO> searchUsers(String searchTerm, UserRole role, boolean hideInactive, Long currentUserId, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (currentUserId != null) predicates.add(cb.notEqual(root.get("id"), currentUserId));
            if (hideInactive) predicates.add(cb.isTrue(root.get("enabled")));
            if (role != null) predicates.add(cb.equal(root.get("role"), role));
            if (StringUtils.hasText(searchTerm)) {
                String likeTerm = "%" + searchTerm.toLowerCase() + "%";
                predicates.add(cb.or(cb.like(cb.lower(root.get("name")), likeTerm), cb.like(cb.lower(root.get("login")), likeTerm)));
            }
            if (predicates.isEmpty()) return cb.conjunction();
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return userRepository.findAll(spec, pageable).map(UserProfileDTO::new);
    }

    public UserProfileDTO getUserProfile(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));
        return new UserProfileDTO(user);
    }

    public UserProfileDTO getUserProfile(User user) {
        return new UserProfileDTO(user);
    }

    public List<UserProfileDTO> findAll() {
        return userRepository.findAll().stream().map(UserProfileDTO::new).collect(Collectors.toList());
    }

    public void editUser(EditUserDTO dto, User currentUser) {
        User existentUser = userRepository.findById(dto.getId())
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;

        //  CAMPOS EDITÁVEIS POR TODOS (Próprio usuário ou Admin) 
        existentUser.setName(dto.getName());

        // Senha
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            existentUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // Telefones
        existentUser.setPhone1(dto.getPhone1());
        existentUser.setPhone2(dto.getPhone2());

        // Endereço
        existentUser.setAddress(dto.getAddress());
        existentUser.setAddressNumber(dto.getAddressNumber());
        existentUser.setZipCode(dto.getZipCode());
        existentUser.setComplement(dto.getComplement());
        existentUser.setCity(dto.getCity());
        existentUser.setState(dto.getState());

        // Validação de CREA (com checagem de unicidade)
        if (dto.getCrea() != null && !dto.getCrea().equals(existentUser.getCrea())) {
            User userWithCrea = userRepository.findByCrea(dto.getCrea());
            if (userWithCrea != null && !userWithCrea.getId().equals(existentUser.getId())) {
                throw new DuplicatedTupleException("Este registro CREA já está cadastrado para outro usuário.");
            }
            existentUser.setCrea(dto.getCrea());
        }
        existentUser.setCreaUf(dto.getCreaUf());

        //  SEGURANÇA: CAMPOS RESTRITOS A ADMIN 
        if (isAdmin) {

            // 1. Alteração de Role
            if (dto.getRole() != null) {
                existentUser.setRole(dto.getRole());
            }

            // 2. Alteração de Status (Enabled)
            // Usa Boolean wrapper no DTO para aceitar null e não sobrescrever erradamente
            if (dto.getEnabled() != null) {
                existentUser.setEnabled(dto.getEnabled());
            }

            // 3. Alteração de Login (Com validação de duplicidade)
            if (dto.getLogin() != null && !dto.getLogin().equals(existentUser.getLogin())) {
                Optional<User> userWithLogin = userRepository.findUserByLogin(dto.getLogin());
                if (userWithLogin.isPresent() && !userWithLogin.get().getId().equals(existentUser.getId())) {
                    throw new DuplicatedTupleException("Este Login (E-mail) já está em uso.");
                }
                existentUser.setLogin(dto.getLogin());
            }

            // 4. Alteração de CPF (Com validação de duplicidade)
            if (dto.getCpf() != null && !dto.getCpf().equals(existentUser.getCpf())) {
                Optional<User> userWithCpf = userRepository.findUserByCpf(dto.getCpf());
                if (userWithCpf.isPresent() && !userWithCpf.get().getId().equals(existentUser.getId())) {
                    throw new DuplicatedTupleException("Este CPF já está em uso.");
                }
                existentUser.setCpf(dto.getCpf());
            }
        }

        userRepository.save(existentUser);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }
}