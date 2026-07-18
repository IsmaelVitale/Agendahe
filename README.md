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

## Publicar uma versão permanente

Depois de validar as mudanças na `main`, crie e envie uma tag sem reutilizar números anteriores:

```powershell
git checkout main
git pull origin main
git tag v0.1.0-beta.1
git push origin v0.1.0-beta.1
```

Tags com sufixo, como `v0.1.0-beta.1`, criam uma pré-versão. Uma tag como `v1.0.0` cria uma versão estável marcada como a mais recente.

O workflow **Publicar versão Windows** gera e anexa permanentemente na página de Releases:

- instalador `AgendaElizaHair-Setup-vX.Y.Z.exe`;
- pacote `AgendaElizaHair-portable-vX.Y.Z.zip`;
- arquivo `SHA256SUMS.txt` para verificação de integridade;
- notas automáticas das alterações desde a versão anterior.

Página de versões: <https://github.com/IsmaelVitale/Agendahe/releases>
