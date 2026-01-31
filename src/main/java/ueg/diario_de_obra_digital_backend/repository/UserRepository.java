package ueg.diario_de_obra_digital_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // IMPORTANTE
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;
import ueg.diario_de_obra_digital_backend.model.User;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    UserDetails findByLogin(String login);

    Optional<User> findUserByLogin(String login);
    Optional<User> findUserByCpf(String cpf);

    User findByCrea(String crea);
}