package ueg.diario_de_obra_digital_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ueg.diario_de_obra_digital_backend.config.FileStorageProperties;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileStorageService {
  private final Path fileStorageLocation;

  //construtor que inicializa o fileStorageLocation, com base no diretório especificado na classe de configuração
  @Autowired
  public FileStorageService(FileStorageProperties fileStorageProperties) {
    //seta o caminho dos arquivos com base no application.properties
    this.fileStorageLocation = Paths
        .get(fileStorageProperties.getUploadDir())
        .toAbsolutePath()
        .normalize();

    try {
      //cria diretório com base no caminho setado
      Files.createDirectories(this.fileStorageLocation);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
