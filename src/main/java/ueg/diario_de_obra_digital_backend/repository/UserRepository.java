package ueg.diario_de_obra_digital_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;
import ueg.diario_de_obra_digital_backend.model.User;

@Repository
public interface UserRepository extends JpaRepository<Long, User> {
  UserDetails findByLogin(String login);
}
