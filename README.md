# Agenda Eliza Hair

Aplicação desktop de agenda semanal e controle de caixa para salão, construída com JavaFX e SQLite.

## Requisitos

- JDK 21
- Maven 3.9 ou mais recente

O JavaFX usado pelo projeto é a versão 21.0.2. Para evitar avisos de incompatibilidade, configure o IntelliJ para executar o projeto com um JDK 21, e não com o JDK 26.

## Executar

```bash
mvn clean javafx:run
```

O banco `dados_salao.db` é criado na pasta em que a aplicação é iniciada. Se existir um banco da primeira versão do projeto, a estrutura de agendamentos é migrada automaticamente e a tabela antiga é preservada como `agendamentos_legado`.

## Testar

```bash
mvn verify
```

A agenda exibe sempre sete dias, de segunda-feira a domingo.
