package ueg.diario_de_obra_digital_backend.specification;

import org.springframework.data.jpa.domain.Specification;
import ueg.diario_de_obra_digital_backend.model.DiarioDeObra;

import java.time.LocalDate;

public class DiarioDeObraSpecification {

    public static Specification<DiarioDeObra> obraIdSelected(Long obraId) {
        return (root, query, cb) -> cb.equal(root.get("obra").get("id"), obraId);
    }

    public static Specification<DiarioDeObra> dataSelected(LocalDate data) {
        return (root, query, cb) -> cb.equal(root.get("data"), data);
    }

    public static Specification<DiarioDeObra> autorIdSelected(Long autorId) {
        return (root, query, cb) -> cb.equal(root.get("autor").get("id"), autorId);
    }

    public static Specification<DiarioDeObra> isNotDeleted() {
        return (root, query, cb) -> cb.equal(root.get("deletado"), false);
    }
}
