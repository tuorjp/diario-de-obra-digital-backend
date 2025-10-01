package ueg.diario_de_obra_digital_backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "file") //prefixo no application.properties

public class FileStorageProperties {
  private String uploadDir;
}
