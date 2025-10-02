package ueg.diario_de_obra_digital_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ueg.diario_de_obra_digital_backend.model.DiarioDeObra;
import ueg.diario_de_obra_digital_backend.model.Obra;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DiarioDeObraRepository extends JpaRepository<DiarioDeObra, Long> {
  Optional<DiarioDeObra> findByObraAndData(Obra obra, LocalDate data);
  @Query(value = "SELECT * FROM diarios_de_obra WHERE id = :id", nativeQuery = true)
  Optional<DiarioDeObra> findByIdEvenIfDeleted(@Param("id") Long id);
}
