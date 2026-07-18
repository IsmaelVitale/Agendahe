# Agenda Eliza Hair

Aplicação desktop de agenda semanal e controle de caixa para salão, construída com JavaFX e SQLite.

## Requisitos

- JDK 21
- O Maven é baixado automaticamente pelo Maven Wrapper

O JavaFX usado pelo projeto é a versão 21.0.2. Para evitar avisos de incompatibilidade, configure o IntelliJ para executar o projeto com um JDK 21, e não com o JDK 26.

## Executar

```powershell
.\mvnw.cmd clean javafx:run
```

No Windows, o banco fica em `%LOCALAPPDATA%\AgendaElizaHair\dados_salao.db`. Na primeira execução, o banco existente na pasta do projeto é copiado automaticamente para esse local. Se ele utilizar o esquema da primeira versão, seus agendamentos também são migrados.

## Testar

```powershell
.\mvnw.cmd verify
```

A agenda exibe sempre sete dias, de segunda-feira a domingo.

## Gerar aplicação para Windows

O formato portátil não exige WiX. Ele cria uma pasta contendo o executável e a runtime Java:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\package-windows.ps1 -Type app-image
```

O resultado fica em `dist\Agenda Eliza Hair`. Essa pasta deve ser distribuída inteira.

Para gerar um instalador `.exe`, instale o WiX Toolset 3 e execute:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\package-windows.ps1 -Type exe
```

O GitHub Actions produz automaticamente tanto o `AgendaElizaHair-portable.zip` quanto o instalador `.exe`.
