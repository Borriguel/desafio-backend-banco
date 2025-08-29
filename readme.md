# Desafio Backend Banco

Este é um projeto de backend para um sistema bancário desenvolvido em Kotlin com Spring Boot. O sistema permite gerenciar clientes, carteiras e transações financeiras entre eles.

Link do desafio: https://github.com/PicPay/picpay-desafio-backend

## Sumário

- [Tecnologias](#tecnologias)
- [Como Executar](#como-executar)
- [Modelos de Domínio](#modelos-de-domínio)
- [Regras de Negócio](#regras-de-negócio)
- [Rotas da API](#rotas-da-api)
  - [Clientes](#clientes)
  - [Carteiras](#carteiras)
  - [Transações](#transações)

## Tecnologias

- **Kotlin** - Linguagem de programação
- **Spring Boot** - Framework principal
- **Spring WebFlux** - Para programação reativa
- **Spring Data R2DBC** - Para acesso a dados reativo
- **H2 Database** - Banco de dados em memória
- **Maven** - Gerenciador de dependências
- **Swagger/OpenAPI** - Documentação da API
- **WebClient** - Cliente HTTP para chamadas externas

## Como Executar

### Pré-requisitos

- Java 21
- Maven

### Passos para execução

1. Clone o repositório:
   ```bash
   git clone https://github.com/Borriguel/desafio-backend-banco
   ```

2. Navegue até o diretório do projeto:
   ```bash
   cd desafio-backend-banco
   ```

3. Execute o projeto:
   ```bash
   ./mvnw spring-boot:run
   ```

4. Acesse a documentação da API:
   ```
   http://localhost:8080/swagger-ui.html
   ```

### Banco de Dados

O projeto utiliza o banco de dados H2 em memória.

## Modelos de Domínio

### Client

Representa um cliente do banco.

- `id: Long` - Identificador único
- `email: String` - Email do cliente
- `name: String` - Nome do cliente
- `document: String` - Documento (CPF) do cliente
- `password: String` - Senha do cliente
- `walletId: Long` - ID da carteira associada

### Wallet

Representa a carteira de um cliente.

- `id: Long` - Identificador único
- `balance: BigDecimal` - Saldo da carteira
- `type: AccountType` - Tipo da conta (COMMON ou SHOPKEEPER)

### Transaction

Representa uma transação financeira entre carteiras.

- `id: Long` - Identificador único
- `payerId: Long` - ID da carteira do pagador
- `payeeId: Long` - ID da carteira do recebedor
- `value: BigDecimal` - Valor da transação
- `idempotencyKey: String` - Chave para evitar transações duplicadas
- `status: Status` - Status da transação (CREATED, PROCESSING, AUTHORIZED, FAILED)
- `createdAt: LocalDateTime` - Data de criação
- `processedAt: LocalDateTime?` - Data de processamento
- `authorizedAt: LocalDateTime?` - Data de autorização
- `failedAt: LocalDateTime?` - Data de falha

### Enums

#### AccountType
- `COMMON` - Conta comum de pessoa física
- `SHOPKEEPER` - Conta de lojista

#### Status
- `CREATED` - Transação criada
- `PROCESSING` - Transação em processamento
- `AUTHORIZED` - Transação autorizada
- `FAILED` - Transação falhou

## Regras de Negócio

1. **Clientes**
   - O email deve ser válido
   - A senha deve ter no mínimo 6 caracteres
   - O nome deve ter entre 3 e 100 caracteres
   - O documento deve ter exatamente 11 dígitos numéricos
   - Não podem existir dois clientes com o mesmo email ou documento

2. **Carteiras**
   - O saldo inicial não pode ser negativo
   - O valor de depósitos e saques deve ser maior que zero
   - Contas do tipo SHOPKEEPER não podem realizar transações (apenas receber)

3. **Transações**
   - O pagador e recebedor devem ser diferentes
   - O valor da transação deve ser maior que zero
   - A chave de idempotência não pode estar em branco
   - Uma transação com a mesma chave de idempotência não será criada novamente
   - O pagador deve ter saldo suficiente para realizar a transação
   - Transações são processadas de forma assíncrona com agendamento automático

4. **Processamento de Transações**
   - Transações criadas são processadas a cada 15 segundos
   - É feita uma chamada a um serviço externo de autorização
   - Se a autorização for bem-sucedida, a transação é concluída
   - Se houver falha, o sistema tenta novamente por até 3 vezes com backoff exponencial

## Rotas da API

### Clientes

#### Criar cliente
```
POST /clients
```

**Corpo da requisição:**
```json
{
  "email": "cliente@example.com",
  "password": "senha123",
  "name": "Nome do Cliente",
  "document": "12345678900",
  "accountType": "COMMON"
}
```

**Resposta de sucesso:**
```json
{
  "id": 1,
  "email": "cliente@example.com",
  "name": "Nome do Cliente",
  "document": "12345678900",
  "walletId": 1
}
```

#### Buscar cliente por ID
```
GET /clients/{id}
```

**Resposta de sucesso:**
```json
{
  "id": 1,
  "email": "cliente@example.com",
  "name": "Nome do Cliente",
  "document": "12345678900",
  "walletId": 1
}
```

#### Detalhes do cliente
```
GET /clients/details/{id}
```

**Resposta de sucesso:**
```json
{
  "id": 1,
  "name": "Nome do Cliente",
  "document": "12345678900",
  "wallet": {
    "id": 1,
    "balance": 0,
    "type": "COMMON"
  }
}
```

#### Listar todos os clientes
```
GET /clients
```

**Resposta de sucesso:**
```json
[
  {
    "id": 1,
    "email": "cliente@example.com",
    "name": "Nome do Cliente",
    "document": "12345678900",
    "walletId": 1
  }
]
```

#### Atualizar cliente
```
PUT /clients/{id}
```

**Corpo da requisição:**
```json
{
  "email": "novoemail@example.com",
  "password": "novasenha123",
  "name": "Novo Nome",
  "document": "09876543211",
  "accountType": "COMMON"
}
```

**Resposta de sucesso:**
```json
{
  "id": 1,
  "email": "novoemail@example.com",
  "name": "Novo Nome",
  "document": "09876543211",
  "walletId": 1
}
```

#### Excluir cliente
```
DELETE /clients/{id}
```

### Carteiras

#### Buscar carteira por ID
```
GET /wallets/{id}
```

**Resposta de sucesso:**
```json
{
  "id": 1,
  "balance": 100.50,
  "type": "COMMON"
}
```

#### Depositar em carteira
```
POST /wallets/{id}/deposit
```

**Corpo da requisição:**
```json
{
  "amount": 50.75
}
```

**Resposta de sucesso:**
```json
{
  "id": 1,
  "balance": 151.25,
  "type": "COMMON"
}
```

#### Sacar da carteira
```
POST /wallets/{id}/withdraw
```

**Corpo da requisição:**
```json
{
  "amount": 25.50
}
```

**Resposta de sucesso:**
```json
{
  "id": 1,
  "balance": 125.75,
  "type": "COMMON"
}
```

### Transações

#### Criar transação
```
POST /transactions
```

**Corpo da requisição:**
```json
{
  "payerId": 1,
  "payeeId": 2,
  "value": 100.00,
  "idempotencyKey": "chave-única-de-transacao"
}
```

**Resposta de sucesso:**
```json
{
  "id": 1,
  "payerId": 1,
  "payeeId": 2,
  "value": 100.00,
  "idempotencyKey": "chave-única-de-transacao",
  "status": "CREATED",
  "createdAt": "2023-01-01T10:00:00"
}
```

#### Buscar transação por ID
```
GET /transactions/{id}
```

**Resposta de sucesso:**
```json
{
  "id": 1,
  "payerId": 1,
  "payeeId": 2,
  "value": 100.00,
  "idempotencyKey": "chave-única-de-transacao",
  "status": "AUTHORIZED",
  "createdAt": "2023-01-01T10:00:00",
  "authorizedAt": "2023-01-01T10:00:05"
}
```

#### Listar transações por pagador
```
GET /transactions/wallet/payer/{payerId}
```

**Resposta de sucesso:**
```json
[
  {
    "id": 1,
    "payerId": 1,
    "payeeId": 2,
    "value": 100.00,
    "idempotencyKey": "chave-única-de-transacao",
    "status": "AUTHORIZED",
    "createdAt": "2023-01-01T10:00:00",
    "authorizedAt": "2023-01-01T10:00:05"
  }
]
```

#### Listar transações por recebedor
```
GET /transactions/wallet/payee/{payeeId}
```

**Resposta de sucesso:**
```json
[
  {
    "id": 1,
    "payerId": 1,
    "payeeId": 2,
    "value": 100.00,
    "idempotencyKey": "chave-única-de-transacao",
    "status": "AUTHORIZED",
    "createdAt": "2023-01-01T10:00:00",
    "authorizedAt": "2023-01-01T10:00:05"
  }
]
```
