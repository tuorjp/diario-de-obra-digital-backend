# Diário de Obra Digital (Hefesto)

Este é o back-end para o sistema de Diário de Obra Digital, construído com Spring Boot, Spring Security (JWT), JPA/Hibernate e Flyway para gerenciamento de banco de dados.

## Configuração Inicial

Siga os passos abaixo para configurar seu ambiente de desenvolvimento.

### Pré-requisitos

-   **Java 23+** (o projeto está configurado para Java 23, mas deve ser compatível com versões mais recentes do Java)
-   **Maven 3.8+**
-   **PostgreSQL 17+**
-   Uma IDE de sua preferência (as instruções abaixo são para o **IntelliJ IDEA**)

### Banco de Dados

1.  Abra seu cliente PostgreSQL (pgAdmin, DBeaver, etc.).
2.  Crie um novo banco de dados. Um nome recomendado é `diario_obra_ueg`.
3.  Você **não precisa** criar nenhuma tabela. As migrations do Flyway cuidarão disso automaticamente.

### Configuração da Aplicação

1.  Navegue até `src/main/resources/`.
3.  Abra o arquivo `application.properties` e preencha com as suas credenciais do PostgreSQL:

```properties
# Configuração do Banco de Dados PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/diario_obra_ueg
spring.datasource.username=seu_usuario_postgres
spring.datasource.password=sua_senha_postgres

# Configuração do JPA/Hibernate
spring.jpa.hibernate.ddl-auto=validate

# Chave secreta para geração de tokens JWT
api.security.token.secret=sua_chave_secreta_aqui
```
