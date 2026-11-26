# Streaming: contas, planos, perfis e histórico (Spring Boot)

Backend de um serviço de streaming. Cada conta tem um usuário **assinante** e pode ter usuários
**não assinantes**, dentro da cota do plano. Cada usuário tem um perfil (infantil, adolescente ou adulto)
que define o que ele pode assistir. Dados em memória (H2).

## Como executar

Requisitos: Java 17+ e Maven (ou uma IDE que importe projetos Maven).

```
mvn spring-boot:run     # sobe a API em http://localhost:8080 (com 6 filmes de exemplo)
mvn test                # roda os testes
```

## Identificação de quem faz a requisição

O usuário solicitante é informado no cabeçalho `X-Usuario-Id`. É uma simplificação do exercício;
em um sistema real ele viria da autenticação/sessão.

## Endpoints

| Funcionalidade                          | Requisição                                  | Quem pode                          |
|-----------------------------------------|---------------------------------------------|------------------------------------|
| Criar assinante (abre uma conta)        | `POST /usuarios` com `tipo: ASSINANTE`      | Qualquer pessoa                    |
| Adicionar usuário não assinante         | `POST /usuarios` com `tipo: NAO_ASSINANTE`  | Assinante da conta                 |
| Remover usuário não assinante           | `DELETE /usuarios/{id}`                     | Assinante da conta                 |
| Consultar a cota de compartilhamento    | `GET /contas/{id}/cota`                     | Assinante da conta                 |
| Listar filmes disponíveis               | `GET /filmes`                               | Qualquer usuário (vê o que o seu plano e perfil permitem) |
| Migrar o plano da conta                 | `PUT /contas/{id}/plano`                    | Assinante da conta                 |
| Assistir a um filme                     | `POST /filmes/{id}/assistir`                | Qualquer usuário                   |
| Histórico de filmes assistidos          | `GET /usuarios/{id}/historico`              | O próprio usuário ou o assinante   |

Exemplos de corpo:

```json
{ "nome": "Ana", "email": "ana@email.com", "tipo": "ASSINANTE", "plano": "PADRAO" }
{ "nome": "Joao", "email": "joao@email.com", "tipo": "NAO_ASSINANTE", "perfil": "INFANTIL", "contaId": 1 }
{ "plano": "PREMIUM" }
```

## Regras de negócio

- **Planos e cota** (usuários não assinantes permitidos): BASICO = 1, PADRAO = 2, PREMIUM = 4.
- **Plano x filme:** o filme tem um plano mínimo; planos maiores incluem os filmes dos menores.
- **Perfil x filme:** o filme tem um perfil mínimo. INFANTIL assiste só a filmes infantis; ADOLESCENTE a
  infantis e de adolescentes; ADULTO assiste a todos. O assinante tem sempre o perfil ADULTO.
- **Migração de plano:** o plano pertence à conta, então a mudança vale para todos os usuários dela.
  A migração é recusada (409) se o novo plano tiver cota menor que o número de usuários não assinantes.
- **Histórico:** cada vez que um filme é assistido cria-se um registro novo. Os registros saem na ordem
  em que foram assistidos, e um filme visto duas vezes aparece duas vezes, cada uma na sua posição.
- **Códigos HTTP:** 400 dados inválidos, 401 sem `X-Usuario-Id`, 403 sem permissão (não é o assinante,
  perfil ou plano não permitem), 404 não encontrado, 409 cota esgotada ou conflito.

## Testes

Os testes foram organizados em torno das regras de negócio (abordagem TDD):

- `UsuarioServiceTest`, `ContaServiceTest` e `FilmeServiceTest`: unitários, com Mockito.
- `StreamingApiIntegrationTest`: integração com H2 e requisições HTTP simuladas (MockMvc).
