package ueg.diario_de_obra_digital_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ueg.diario_de_obra_digital_backend.model.Servico;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {
}