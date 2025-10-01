-- Flyway migration script: V1 -- Create Initial Schema

-- 1. Cria a tabela de usuários com a coluna 'login' em vez de 'email'
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

-- 2. Tabelas de "Tipo" (sem alterações)
CREATE TABLE equipamentos (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE maos_de_obra (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE servicos (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL UNIQUE,
    unidade_medida VARCHAR(50) NOT NULL
);

-- (O resto do arquivo continua exatamente o mesmo...)
-- 3. Tabela de Obras
CREATE TABLE obras (
    id BIGSERIAL PRIMARY KEY,
    contratante VARCHAR(255) NOT NULL,
    contratada VARCHAR(255) NOT NULL,
    projeto VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    fiscal_id BIGINT,
    CONSTRAINT fk_obras_fiscal FOREIGN KEY (fiscal_id) REFERENCES users(id)
);

-- 4. Tabela de junção para Obras e Engenheiros (ManyToMany)
CREATE TABLE obras_engenheiros (
    obra_id BIGINT NOT NULL,
    engenheiro_id BIGINT NOT NULL,
    PRIMARY KEY (obra_id, engenheiro_id),
    CONSTRAINT fk_obras_engenheiros_obra FOREIGN KEY (obra_id) REFERENCES obras(id),
    CONSTRAINT fk_obras_engenheiros_user FOREIGN KEY (engenheiro_id) REFERENCES users(id)
);

-- 5. Tabela de Diários de Obra
CREATE TABLE diarios_de_obra (
    id BIGSERIAL PRIMARY KEY,
    obra_id BIGINT NOT NULL,
    data DATE NOT NULL,
    condicao_climatica VARCHAR(255) NOT NULL,
    observacoes TEXT,
    status VARCHAR(50) NOT NULL,
    deletado BOOLEAN NOT NULL DEFAULT FALSE,
    autor_id BIGINT NOT NULL,
    validador_id BIGINT,
    comentario_validacao TEXT,
    CONSTRAINT fk_diarios_obra FOREIGN KEY (obra_id) REFERENCES obras(id),
    CONSTRAINT fk_diarios_autor FOREIGN KEY (autor_id) REFERENCES users(id),
    CONSTRAINT fk_diarios_validador FOREIGN KEY (validador_id) REFERENCES users(id),
    CONSTRAINT uq_diario_obra_data UNIQUE (obra_id, data)
);

-- 6. Tabelas de Associação
CREATE TABLE diario_equipamentos (
    id BIGSERIAL PRIMARY KEY,
    diario_id BIGINT NOT NULL,
    equipamento_id BIGINT NOT NULL,
    quantidade INT NOT NULL,
    CONSTRAINT fk_de_diario FOREIGN KEY (diario_id) REFERENCES diarios_de_obra(id),
    CONSTRAINT fk_de_equipamento FOREIGN KEY (equipamento_id) REFERENCES equipamentos(id)
);

CREATE TABLE diario_maos_de_obra (
    id BIGSERIAL PRIMARY KEY,
    diario_id BIGINT NOT NULL,
    mao_de_obra_id BIGINT NOT NULL,
    quantidade INT NOT NULL,
    CONSTRAINT fk_dmo_diario FOREIGN KEY (diario_id) REFERENCES diarios_de_obra(id),
    CONSTRAINT fk_dmo_mao_de_obra FOREIGN KEY (mao_de_obra_id) REFERENCES maos_de_obra(id)
);

CREATE TABLE diario_servicos (
    id BIGSERIAL PRIMARY KEY,
    diario_id BIGINT NOT NULL,
    servico_id BIGINT NOT NULL,
    -- Usando a Solução B do problema anterior (DOUBLE PRECISION para o tipo Double)
    quantidade DOUBLE PRECISION NOT NULL,
    CONSTRAINT fk_ds_diario FOREIGN KEY (diario_id) REFERENCES diarios_de_obra(id),
    CONSTRAINT fk_ds_servico FOREIGN KEY (servico_id) REFERENCES servicos(id)
);

-- 7. Tabelas para coleções de elementos simples (fotos e visitas)
CREATE TABLE diario_fotos (
    diario_id BIGINT NOT NULL,
    foto_url VARCHAR(255) NOT NULL,
    CONSTRAINT fk_diario_fotos_diario FOREIGN KEY (diario_id) REFERENCES diarios_de_obra(id)
);

CREATE TABLE diario_visitas (
    diario_id BIGINT NOT NULL,
    visita_registro VARCHAR(255) NOT NULL,
    CONSTRAINT fk_diario_visitas_diario FOREIGN KEY (diario_id) REFERENCES diarios_de_obra(id)
);