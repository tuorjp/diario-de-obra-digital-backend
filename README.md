# Diário de Obra Digital (Hefesto)

Este é o back-end para o sistema de Diário de Obra Digital, construído com Spring Boot, Spring Security (JWT), JPA/Hibernate e Flyway para gerenciamento de banco de dados.

---

## 🐳 Rodando com Docker (recomendado)

A forma mais simples de subir o ambiente completo (banco + aplicação) com um único comando.

### Pré-requisitos
- **Docker Desktop** instalado e em execução

### Passos

1. Clone o repositório e entre na pasta do projeto.

2. Copie o arquivo de variáveis de ambiente e edite-o se necessário:
   ```bash
   cp .env.example .env
   ```

3. Suba os containers:
   ```bash
   docker compose up --build
   ```
   A aplicação estará disponível em `http://localhost:8090`.

4. Para encerrar:
   ```bash
   docker compose down
   ```
   > Os dados do banco ficam persistidos no volume `postgres_data`. Para apagar tudo, use `docker compose down -v`.

---

## ⚙️ Rodando localmente (sem Docker)

### Pré-requisitos

-   **Java 21**
-   **Maven 3**
-   **PostgreSQL 17** instalado localmente

### Banco de Dados

1.  Abra seu cliente PostgreSQL (pgAdmin, DBeaver, etc.).
2.  Crie um novo banco de dados chamado `diario-de-obra-ueg`.
3.  Você **não precisa** criar nenhuma tabela. As migrations do Flyway cuidarão disso automaticamente.

### Configuração da Aplicação

1.  Navegue até `src/main/resources/`.
2.  Abra o arquivo `application.properties`. As credenciais padrão são:
    ```properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/diario-de-obra-ueg
    spring.datasource.username=postgres
    spring.datasource.password=12345
    ```
    Ajuste se suas credenciais locais forem diferentes (via variáveis de ambiente ou editando o arquivo).
