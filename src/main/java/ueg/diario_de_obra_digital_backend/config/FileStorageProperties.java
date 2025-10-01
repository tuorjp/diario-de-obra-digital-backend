package ueg.diario_de_obra_digital_backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter

@ConfigurationProperties(prefix = "file") //prefixo no application.properties
public class FileStorageProperties {
  private String uploadDir;
}
