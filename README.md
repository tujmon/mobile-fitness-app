# HackerFit

App Android de exercícios baseado na Escada Fitness do livro *"The Hacker's Diet"* de John Walker. Treinos diários de 10–15 minutos, sem equipamento, progressão por 48 degraus.

---

## Pré-requisitos

- **JDK 17** — [Eclipse Temurin](https://adoptium.net/) recomendado
- **Android SDK** (command-line tools, platform 35, build-tools 35.0.1, platform-tools)

### Variáveis de ambiente

```
JAVA_HOME    = C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot
ANDROID_HOME = C:\Android\Sdk
```

Adicione ao PATH:
```
%JAVA_HOME%\bin
%ANDROID_HOME%\platform-tools
```

---

## Instalação rápida (Windows)

### 1. Instalar JDK 17

```powershell
winget install --id EclipseAdoptium.Temurin.17.JDK --accept-package-agreements --accept-source-agreements
```

### 2. Instalar Android SDK (sem Android Studio)

```powershell
# Criar pasta do SDK
New-Item -ItemType Directory -Path "C:\Android\Sdk\cmdline-tools" -Force

# Baixar command-line tools
$url = "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"
Invoke-WebRequest -Uri $url -OutFile "$env:TEMP\android-cmdline-tools.zip" -UseBasicParsing

# Extrair
Expand-Archive -Path "$env:TEMP\android-cmdline-tools.zip" -DestinationPath "C:\Android\Sdk\cmdline-tools" -Force
Rename-Item "C:\Android\Sdk\cmdline-tools\cmdline-tools" "latest"

# Aceitar licenças e instalar componentes
$sdk = "C:\Android\Sdk"
Write-Output "y" | & "$sdk\cmdline-tools\latest\bin\sdkmanager.bat" --sdk_root=$sdk "platforms;android-35" "build-tools;35.0.1" "platform-tools"
```

### 3. Configurar `local.properties`

O arquivo `local.properties` já existe com:

```
sdk.dir=C:\\Android\\Sdk
```

Se o SDK estiver em outro caminho, ajuste esse valor.

---

## Build

### Debug APK

```powershell
# No PowerShell, na raiz do projeto:
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
.\gradlew.bat assembleDebug
```

O APK será gerado em:
```
app\build\outputs\apk\debug\app-debug.apk
```

### Release APK (minificado)

```powershell
.\gradlew.bat assembleRelease
```

O APK será gerado em:
```
app\build\outputs\apk\release\app-release-unsigned.apk
```

> **Nota:** O release APK é unsigned. Para assinar, gere uma keystore e configure `signingConfigs` no `app/build.gradle.kts`.

---

## Instalar no celular

Com o APK gerado, transfira para o Android e instale:

1. Copie o `.apk` para o celular (USB, Google Drive, etc.)
2. No celular, vá em **Configurações > Segurança > Permitir fontes desconhecidas**
3. Abra o arquivo `.apk` e instale

Ou via ADB (se `platform-tools` estiver no PATH):

```powershell
adb install app\build\outputs\apk\debug\app-debug.apk
```

---

## Estrutura do Projeto

```
app/src/main/java/com/hackerfit/
├── data/           # Room DB, DataStore, repositórios
├── domain/         # Modelos, interfaces, constantes (48 degraus)
├── ui/             # Telas Compose + ViewModels + navegação
├── service/        # WorkManager (lembrete diário)
├── di/             # Módulos Hilt (DI)
├── HackerFitApp.kt # Application class
└── MainActivity.kt # Entry point
```

---

## Funcionalidades

- **48 degraus** de exercícios (15 introdutórios + 33 vitalícios)
- **5 exercícios**: Flexão para Frente, Abdominal, Elevação de Pernas, Flexão de Braço, Corrida e Salto
- **Progressão automática**: avaliação após 5 dias em cada degrau
- **Streak com congelamentos**: 1 freeze a cada 5 dias, máx. 5 acumulados
- **Lembrete diário** configurável
- **Histórico** de treinos e avaliações
- **100% offline** — dados salvos no dispositivo (Room + DataStore)
- **Idioma**: pt-BR
