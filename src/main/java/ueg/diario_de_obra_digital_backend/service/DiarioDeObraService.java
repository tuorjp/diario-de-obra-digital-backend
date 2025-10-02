package ueg.diario_de_obra_digital_backend.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.stereotype.Service;
import ueg.diario_de_obra_digital_backend.model.DiarioDeObra;
import ueg.diario_de_obra_digital_backend.repository.DiarioDeObraRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiarioDeObraService {
  private final DiarioDeObraRepository diarioDeObraRepository;
  private final EntityManager entityManager;

  // Lista todos até os com deletado = true
  public List<DiarioDeObra> listAllWithDeleted() {
    return diarioDeObraRepository.findAll();
  }

  // Lista todos os que tem deletado = false
  public List<DiarioDeObra> listAllActive() {
    // Obtém a sessão do Hibernate a partir do EntityManager
    Session session = entityManager.unwrap(Session.class);

    // Ativa o filtro para esta sessão/transação
    session.enableFilter("deletedDiarioFilter");

    // A chamada ao repositório dentro desta sessão agora terá o filtro aplicado
    List<DiarioDeObra> listaAtivos = diarioDeObraRepository.findAll();

    // Desativa o filtro após realizar a consulta
    session.disableFilter("deletedDiarioFilter");

    return listaAtivos;
  }

  public void logicDelete(Long id) {
    // O @SQLDelete no model do DiarioDeObra faz o delete ser lógico
    diarioDeObraRepository.deleteById(id);
  }
}
