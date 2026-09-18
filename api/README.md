# SmartCollector API

API REST do sistema **SmartCollector**, construída com **Spring Boot 3.4** e **Oracle**.

Atende as aplicações cliente (o app do catador e do descartador). O [painel administrativo](../README.md), na raiz deste repositório, é a outra ponta: os dois falam com o **mesmo schema Oracle**, cada um com a sua responsabilidade.

---

## Stack

- **Java 21** · **Spring Boot 3.4**
- **Spring Security** com autenticação **JWT** (stateless)
- **Spring Data JPA / Hibernate** sobre **Oracle**
- **Bean Validation** nos DTOs de entrada
- **PL/SQL** — a API chama as rotinas do pacote `PKG_RELATORIO` criado pelo admin

---

## Divisão de responsabilidades com o admin

| | API (este módulo) | Admin (raiz do repo) |
|---|---|---|
| Quem usa | Aplicações cliente | Operação interna |
| Protocolo | REST + JSON, token JWT | JSF server-side, sessão |
| Schema Oracle | Consome (`ddl-auto=validate`) | **Versiona** (Flyway: V1, V2, V3) |
| Regras de entrega | Delega ao PL/SQL | Delega ao PL/SQL |

As migrações vivem só no admin. Rodar duas cadeias de Flyway contra o mesmo schema geraria conflito de checksum, então aqui o Hibernate apenas **valida** que as entidades batem com as tabelas — se não baterem, a aplicação não sobe, que é o comportamento desejado.

### Por que a entrega é feita em PL/SQL

`ColetaService.finalizar` não implementa a regra em Java: ela chama `PKG_RELATORIO.prc_registrar_entrega`. O motivo é concreto — essa operação precisa, **numa única transação**, travar a linha da coleta e a do centro (`FOR UPDATE`), validar o espaço, marcar os itens como entregues, finalizar a coleta e publicar o evento no outbox que alimenta o read model do admin.

Reescrever isso em Java criaria uma segunda versão da mesma regra, e a versão em Java não publicaria o evento — o histórico do admin simplesmente deixaria de ver as entregas feitas pela API.

---

## Autenticação

```
POST /auth/login
{ "email": "ana@smartcollector.br", "senha": "..." }

200 OK
{ "token": "eyJhbGciOiJIUzI1NiJ9...", "nome": "Ana", "funcao": "USER", "expiraEm": "..." }
```

Nas demais chamadas: `Authorization: Bearer <token>`.

O papel vem da coluna `FUNCAO` (`ADMIN` ou `USER`). Cadastro de usuários, catadores, descartadores, centros e itens exige `ADMIN`; o fluxo de coleta exige apenas um usuário autenticado.

---

## Endpoints

| Método | Rota | Acesso |
|---|---|---|
| `POST` | `/auth/login` | público |
| `GET POST PUT DELETE` | `/usuarios` | `ADMIN` |
| `GET` | `/catadores` · `/descartadores` | autenticado |
| `POST PUT DELETE` | `/catadores` · `/descartadores` | `ADMIN` |
| `GET` | `/centros` · `/itens` | autenticado |
| `POST PUT DELETE` | `/centros` · `/itens` | `ADMIN` |
| `GET` | `/coletas` · `/coletas/disponiveis` · `/coletas/{id}` | autenticado |
| `POST` | `/coletas` | autenticado |
| `PUT` | `/coletas/{id}/aceitar/{idCatador}` | autenticado |
| `PUT` | `/coletas/{id}/finalizar/{idCentro}` | autenticado |
| `DELETE` | `/coletas/{id}` | `ADMIN` |

Erros saem no formato `application/problem+json` (RFC 7807): `404` para recurso inexistente, `409` para violação de regra de negócio, `400` com o mapa `campos` para erro de validação.

---

## Como executar

**1. Suba o Oracle.**

```bash
docker compose up -d
```

**2. Rode o admin uma vez** para aplicar as migrações (ele cria as tabelas e o PL/SQL que esta API consome). Instruções na [raiz do repositório](../README.md).

**3. Suba a API.**

```bash
export DB_URL=jdbc:oracle:thin:@localhost:1521/FREEPDB1
export DB_USER=smartcollector
export DB_PASSWORD=smartcollector
export JWT_SECRET=uma-chave-com-pelo-menos-32-bytes-aqui

mvn spring-boot:run
```

API em `http://localhost:8080`. O admin roda em `8081`, então os dois sobem lado a lado.

> Sem `JWT_SECRET` a aplicação ainda sobe, mas gera uma chave aleatória a cada boot e avisa no log — os tokens deixam de valer no restart. Serve para desenvolvimento; em produção defina a variável.

### Primeiro usuário

Não há auto-cadastro: criar usuário exige um token de `ADMIN`, e isso é proposital — um endpoint público de cadastro que aceita `"funcao": "ADMIN"` no corpo é uma escada de privilégio. O primeiro admin entra direto no banco:

```sql
INSERT INTO tb_usuario (id, nome, email, senha, funcao)
VALUES (usuario_seq.NEXTVAL, 'Admin', 'admin@smartcollector.br', '<hash-bcrypt>', 'ADMIN');
```

Para gerar o hash: `htpasswd -bnBC 10 "" sua-senha | tr -d ':\n'`.

### Testes

```bash
mvn test
```

---

## Decisões e limitações conhecidas

- **Sem Flyway aqui.** O schema é versionado pelo admin. A consequência é que a API não sobe num banco vazio — depende do admin ter rodado antes. Em troca, não há duas cadeias de migração disputando o mesmo schema.
- **`ddl-auto=validate`, nunca `update`.** Se uma entidade divergir da tabela, a aplicação falha no boot em vez de alterar o schema por conta própria.
- **Entidades expostas direto nos endpoints de catálogo.** `Item`, `CentroColeta`, `Catador` e `Descartador` não têm DTO de resposta: não carregam dado sensível e a serialização é estável. `Usuario` tem — a senha nunca sai da aplicação.
- **Sem paginação.** As listagens devolvem tudo. Com o volume atual não é problema; o dia que for, `Pageable` resolve sem mudar o contrato de forma incompatível.
- **`TB_CATADOR_ITEM` só é preenchida ao aceitar a coleta.** Um catador que recolhe itens fora de uma coleta registrada não aparece no cálculo de capacidade.
- **Cobertura de testes parcial.** Os testes cobrem as regras de negócio do serviço (capacidade, estado da coleta, hash de senha). Não há teste de integração contra um Oracle real — o caminho natural seria Testcontainers.
