package ueg.diario_de_obra_digital_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ueg.diario_de_obra_digital_backend.config.FileStorageProperties;
import ueg.diario_de_obra_digital_backend.service.exception.FileNotFoundInStorageException;
import ueg.diario_de_obra_digital_backend.service.exception.FileStorageCreationException;
import ueg.diario_de_obra_digital_backend.service.exception.InvalidFileName;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

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

  public String storeFile(MultipartFile file) {
    String originalFileName = StringUtils
        .cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

    if(originalFileName.contains("..")) {
      throw new InvalidFileName("Nome do arquivo inválido, renomeie e tente novamente.");
    }

    System.out.print(originalFileName);

    // efetivamente salva o arquivo no diretório
    try {
      // constrói o caminho de destino, usando o método resolve
      Path targetLocation = this.fileStorageLocation.resolve(originalFileName);
      try (InputStream inputStream = file.getInputStream()) {
        Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
      }

      // posteriormente o nome será tratado, por isso o retorno vai ser um nome que contém data e outras informações relevantes
      return originalFileName;
    } catch (IOException e) {
      throw new FileStorageCreationException("Não foi possível salvar o arquivo");
    }
  }

  // método que carrega um arquivo do storage como um Resource
  public Resource loadFileAsResource(String fileName) {
    try {
      // tenta encontrar o arquivo dentro do storage
      Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
      Resource resource = new UrlResource(filePath.toUri()); //UrlResource pode lançar uma MalformedURLException

      if(resource.exists()) {
        return resource;
      } else {
        throw new FileNotFoundInStorageException("Arquivo não encontrado no storage: " + fileName);
      }
    } catch (MalformedURLException e) {
      throw new FileNotFoundInStorageException("Arquivo não encontrado no storage: " + fileName);
    }
  }
}
