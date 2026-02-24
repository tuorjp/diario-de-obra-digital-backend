package ueg.diario_de_obra_digital_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ueg.diario_de_obra_digital_backend.enums.ObraStatus;
import ueg.diario_de_obra_digital_backend.model.Obra;

import java.util.List;

@Repository
public interface ObraRepository extends JpaRepository<Obra, Long>, JpaSpecificationExecutor<Obra> {
    List<Obra> findAllByStatus(ObraStatus status);
}
