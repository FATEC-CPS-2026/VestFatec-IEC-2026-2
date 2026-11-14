# VestFatec

VestFatec é um projeto estudantil com backend em Java 21 e Spring Boot.

O banco relacional é o H2 embarcado. Não é necessário iniciar serviços externos
nem definir variáveis de ambiente para executar a aplicação.

## Executar

Com Java 21 e Maven instalados:

```sh
mvn spring-boot:run
```

A API fica disponível em `http://localhost:8080`. A variável `PORT` permite alterar
a porta.

Por padrão, os dados são persistidos em `./data/vestfatec.mv.db` e permanecem entre
reinicializações. É possível configurar `DB_URL` com outra URL JDBC do H2 e alterar
as credenciais pelas variáveis `DB_USER` e `DB_PASSWORD` (padrões: `sa` e senha vazia).
Ao executar em Docker, monte um volume em `/app/data` para manter os dados após
a remoção do contêiner.

## Autenticação

- `POST /auth/register`: recebe `email`, `password` e `role` (`ADMIN` ou `CANDIDATE`).
- `POST /auth/login`: recebe `email` e `password`.
- `POST /auth/logout`: encerra a sessão e retorna `204`.

Cadastro e login iniciam uma sessão HTTP e retornam `id`, `email` e `role`.
O cliente deve guardar o cookie `JSESSIONID` e enviá-lo nas próximas requisições.
No navegador, use `credentials: "include"` no `fetch` quando necessário.

Usuários `ADMIN` podem cadastrar e consultar candidatos em `/api/candidates`.
Usuários `CANDIDATE` podem apenas consultar.

## Testes

```sh
mvn test
```

Os testes usam H2 em memória, separado do banco local em arquivo.
