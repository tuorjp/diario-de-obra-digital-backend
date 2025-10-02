package ueg.diario_de_obra_digital_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ueg.diario_de_obra_digital_backend.model.MaoDeObra;

@Repository
public interface MaoDeObraRepository extends JpaRepository<MaoDeObra, Long> {
}
