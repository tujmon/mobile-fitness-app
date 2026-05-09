# HackerFit — Plano de Desenvolvimento

App Android de exercícios baseado na *Escada Fitness* do livro "The Hacker's Diet" de John Walker.

---

## 1. Visão Geral

| Item | Decisão |
|---|---|
| Nome | HackerFit |
| Plataforma | Android (APK) |
| SDK Mínimo | API 31 (Android 12) |
| Linguagem | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Armazenamento | Room (dados estruturados) + DataStore (preferências) |
| Arquitetura | MVVM + Repository |
| Idioma | pt-BR apenas |
| Dados | 100% no dispositivo, sem servidor |

---

## 2. Arquitetura

### Padrão: MVVM + Repository

```
UI (Compose) → ViewModel → Repository → Data Source (Room / DataStore)
```

- **ViewModel** expõe `StateFlow` para a UI observar
- **Repository** centraliza acesso aos dados, orquestra Room + DataStore
- **Room** para dados estruturados (log diário, histórico, avaliações)
- **DataStore Preferences** para configurações simples (horário do lembrete, estado do onboarding)

### Injeção de Dependência

- **Hilt** para DI (ViewModels, Repositories, Database)

### Navegação

- **Jetpack Navigation Compose** com rotas tipadas
- **Bottom Navigation Bar** com 3 destinos: Início, Histórico, Configurações

### Notificações

- **WorkManager** para agendar lembrete diário no horário escolhido pelo usuário
- **NotificationCompat** para exibir a notificação

---

## 3. Modelo de Dados

### Entidades Room

```kotlin
@Entity
UserProfile {
    id: Int (PK, sempre 1 — app mono-usuário)
    currentRung: Int (1–48)
    phase: String ("introductory" | "lifetime")
    rungStartDate: LocalDate
    dailyReminderHour: Int?
    dailyReminderMinute: Int?
    onboardingComplete: Boolean
}

@Entity
DailyLog {
    id: Long (PK, auto-gerado)
    date: LocalDate (índice único)
    rung: Int
    completed: Boolean
    completedAt: LocalDateTime?
}

@Entity
AssessmentLog {
    id: Long (PK, auto-gerado)
    date: LocalDate
    fromRung: Int
    toRung: Int
    passed: Boolean
    notes: String?
}
```

### DataStore (Preferences)

```kotlin
streakCount: Int        // dias consecutivos atuais
freezesBanked: Int      // congelamentos disponíveis (max 5)
lastFreezeEarnDate: LocalDate?
```

### Constantes (não persistido)

- Dados dos 48 degraus: repetições de cada exercício, sets e passos extras
- Definição dos 5 exercícios: nome, descrição introdutória, descrição lifetime
- Lista estática em um object Kotlin, sem necessidade de banco de dados

---

## 4. Estrutura de Pastas

```
com.hackerfit/
├── data/
│   ├── local/
│   │   ├── db/
│   │   │   ├── HackerFitDatabase.kt
│   │   │   ├── dao/
│   │   │   │   ├── UserProfileDao.kt
│   │   │   │   ├── DailyLogDao.kt
│   │   │   │   └── AssessmentLogDao.kt
│   │   │   └── entity/
│   │   │       ├── UserProfileEntity.kt
│   │   │       ├── DailyLogEntity.kt
│   │   │       └── AssessmentLogEntity.kt
│   │   └── preferences/
│   │       └── StreakDataStore.kt
│   ├── repository/
│   │   ├── UserProfileRepositoryImpl.kt
│   │   ├── DailyLogRepositoryImpl.kt
│   │   ├── AssessmentRepositoryImpl.kt
│   │   └── StreakRepositoryImpl.kt
│   └── mapper/                (Entity ↔ Domain mappers)
├── domain/
│   ├── model/
│   │   ├── Rung.kt
│   │   ├── Exercise.kt
│   │   ├── WorkoutSession.kt
│   │   └── Phase.kt
│   ├── repository/
│   │   ├── UserProfileRepository.kt
│   │   ├── DailyLogRepository.kt
│   │   ├── AssessmentRepository.kt
│   │   └── StreakRepository.kt
│   └── constants/
│       └── FitnessLadder.kt   (48 degraus com todas as repetições)
├── ui/
│   ├── navigation/
│   │   ├── HackerFitNavHost.kt
│   │   └── BottomNavBar.kt
│   ├── theme/
│   │   ├── Theme.kt
│   │   ├── Color.kt
│   │   └── Type.kt
│   ├── screens/
│   │   ├── onboarding/
│   │   │   ├── OnboardingScreen.kt
│   │   │   └── OnboardingViewModel.kt
│   │   ├── home/
│   │   │   ├── HomeScreen.kt
│   │   │   └── HomeViewModel.kt
│   │   ├── workout/
│   │   │   ├── WorkoutScreen.kt
│   │   │   └── WorkoutViewModel.kt
│   │   ├── assessment/
│   │   │   ├── AssessmentScreen.kt
│   │   │   └── AssessmentViewModel.kt
│   │   ├── exercise/
│   │   │   └── ExerciseDetailScreen.kt
│   │   ├── history/
│   │   │   ├── HistoryScreen.kt
│   │   │   └── HistoryViewModel.kt
│   │   └── settings/
│   │       ├── SettingsScreen.kt
│   │       └── SettingsViewModel.kt
│   └── components/
│       ├── StreakBadge.kt
│       ├── RungIndicator.kt
│       ├── ExerciseCard.kt
│       ├── RepCounter.kt
│       └── FreezeIndicator.kt
├── service/
│   └── ReminderWorker.kt      (WorkManager para lembrete diário)
├── di/
│   ├── DatabaseModule.kt
│   ├── RepositoryModule.kt
│   └── UseCaseModule.kt
└── HackerFitApp.kt            (Application class com Hilt)
```

---

## 5. Telas (Pages)

### 5.1 Onboarding (primeiro acesso apenas)

- 3-4 cards deslizáveis explicando o programa
- Card 1: "Bem-vindo ao HackerFit" — filosofia do programa (10-15 min/dia, sem equipamento)
- Card 2: "A Escada Fitness" — explicação dos degraus e fases
- Card 3: "Os 5 Exercícios" — visão geral dos exercícios
- Card 4: "Vamos começar!" — botão "Começar no Degrau 1"
- Ao finalizar, marca `onboardingComplete = true` no perfil

### 5.2 Home (Tela Principal)

- **Header**: nome do app, ícone de configurações
- **Card do Degrau Atual**: número do degrau, fase (Introdutória / Vitalícia), dias no degrau
- **Streak Badge**: dias consecutivos, congelamentos disponíveis
- **Treino de Hoje**: lista dos 5 exercícios com repetições do degrau atual
- **Botão "Iniciar Treino"**: abre o fluxo de treino guiado
- **Banner de Avaliação** (condicional): aparece após 5+ dias no degrau — "Tentar próximo degrau?"
- **Estado vazio**: se já completou hoje, mostra mensagem "Treino de hoje concluído!" com ícone de check

### 5.3 Sessão de Treino (Workout)

- Fluxo step-by-step, um exercício por vez
- **Topo**: progresso (exercício 2 de 5), nome do exercício
- **Centro**: instruções textuais do exercício, repetições alvo
- **Contador**: botões +/- para exercícios de repetição (Bend, Sit Up, Leg Lift, Push Up)
- **Para Run & Jump**: mostra alvo (ex: "4 sets de 75 passos + 10 polichinelos, + 40 passos extras"), botão "Concluir"
- **Rodapé**: botão "Concluir Exercício" → avança para o próximo
- **Último exercício**: botão "Finalizar Treino" → salva log, retorna à Home

### 5.4 Avaliação (Assessment)

- Acessada pelo banner na Home após 5+ dias no degrau
- **Passo 1**: "Hoje você vai tentar o Degrau X" (mostra repetições do próximo degrau)
- **Passo 2**: Fluxo de treino normal com as repetições do próximo degrau
- **Passo 3**: Auto-avaliação — "Como você se sentiu?"
  - "Fácil, posso avançar" → avança degrau, salva AssessmentLog(passed=true)
  - "Difícil, vou ficar no degrau atual" → mantém, salva AssessmentLog(passed=false)
- **Caso especial (degrau 15 → 16)**: alerta informando que as repetições diminuem porque os exercícios ficam mais difíceis (fase introdutória → vitalícia)

### 5.5 Detalhe do Exercício

- Acessível a partir da tela de treino (ícone de info)
- Mostra descrição completa do exercício
- Indica se é variante introdutória ou vitalícia com base no degrau atual
- Texto descritivo extraído diretamente do livro (traduzido para pt-BR)

### 5.6 Histórico

- **Calendário**: dias completados marcados, permite navegar meses
- **Timeline de Degraus**: gráfico/linha mostrando progressão de degraus ao longo do tempo
- **Estatísticas**: total de treinos, streak máximo, dias no degrau atual
- **Lista de avaliações**: histórico de avaliações (passou / ficou)

### 5.7 Configurações (Settings)

- **Horário do lembrete**: time picker para definir horário da notificação diária
- **Lembrete ativo**: toggle para ativar/desativar
- **Resetar dados**: botão com confirmação (apaga tudo, volta ao onboarding)
- **Sobre**: versão do app, referência ao livro "The Hacker's Diet"

---

## 6. Regras de Negócio

### 6.1 Sistema de Degraus

- 48 degraus divididos em 2 fases:
  - Degraus 1–15: Escada Introdutória (variantes mais fáceis)
  - Degraus 16–48: Escada Vitalícia (variantes completas)
- 5 exercícios por degrau: Flexão para Frente, Abdominal, Elevação de Pernas, Flexão de Braço, Corrida e Salto
- Cada degrau define: repetições de cada exercício, sets e passos extras para Corrida e Salto

### 6.2 Progressão

- Sempre iniciar no Degrau 1
- Permanecer no mínimo 5 dias em cada degrau
- Após 5 dias, oferecer avaliação para tentar o próximo degrau
- Critérios para avançar: sentir que foi tão fácil quanto o degrau anterior na semana passada
- Critérios para ficar: não conseguir completar, passar de 15 minutos, sentir dor
- Na transição degrau 15 → 16: repetições diminuem (exercícios ficam mais difíceis)

### 6.3 Streak e Congelamentos

- Streak: contagem de dias consecutivos de treino completado
- Ao completar um treino, streak incrementa
- Ao não completar um treino em um dia:
  - Se tem congelamento disponível → consome 1, streak não quebra
  - Se não tem → streak reseta para 0
- Ganho de congelamento: 1 congelamento a cada 5 dias consecutivos
- Máximo acumulado: 5 congelamentos
- Congelamentos são consumidos automaticamente

### 6.4 Treino Diário

- Duração: 10-15 minutos (todos os degraus)
- Todos os 5 exercícios devem ser completados para marcar o dia como concluído
- Exercícios seguem ordem fixa: Flexão para Frente → Abdominal → Elevação de Pernas → Flexão de Braço → Corrida e Salto

---

## 7. Design Visual

### Tema

- Material 3 com cor dinâmica (Dynamic Color) no Android 12+
- Suporte automático a modo claro/escuro do sistema
- Cores primárias derivadas do wallpaper do usuário

### Componentes Visuais

- **Streak Badge**: ícone de chama com número do streak, badge de congelamento
- **Rung Indicator**: indicador circular com número do degrau, cor diferente para cada fase
- **Exercise Card**: card com nome do exercício, repetições alvo, ícone informativo
- **Rep Counter**: botões grandes de +/- com display central do contador
- **Freeze Indicator**: ícone de floco de neve com quantidade de congelamentos

### Princípios de UX

- Botões grandes e acessíveis (usuário em movimento)
- Textos em pt-BR, claros e diretos
- Fluxos simples com mínimo de toques
- Feedback visual ao completar exercícios e treinos

---

## 8. Notificações

- Lembrete diário no horário escolhido pelo usuário (padrão: 08:00)
- Agendado via WorkManager (PeriodicWorkRequest de 24h)
- Conteúdo: "Hora do seu treino! Degrau X te espera."
- Canal de notificação dedicado (pode ser silenciado pelo usuário)
- Toggle on/off nas configurações

---

## 9. Dependências Principais

```toml
[Libraries]
jetpack-compose-bom = "latest"
compose-material3 = "latest"
compose-navigation = "latest"
room-runtime = "latest"
room-ktx = "latest"
datastore-preferences = "latest"
hilt-android = "latest"
hilt-navigation-compose = "latest"
work-runtime-ktx = "latest"
kotlinx-coroutines = "latest"
kotlinx-serialization = "latest" (para rotas de navegação tipadas)
```

---

## 10. Fases de Implementação

### Fase 1 — Fundação
- Setup do projeto Gradle com Compose, Room, DataStore, Hilt
- Constantes da escada fitness (48 degraus com todas as repetições)
- Modelo de dados + banco Room + entidades + DAOs
- Repositories (interfaces + implementações)
- Tema Material 3 + esqueleto de navegação com Bottom Nav

### Fase 2 — Treino Principal
- Tela Home (degrau atual, exercícios do dia, streak)
- Fluxo de treino step-by-step (um exercício por vez)
- Instruções textuais dos exercícios (pt-BR)
- Detalhe do exercício (tela de info)
- Gravação do log diário ao completar treino

### Fase 3 — Progressão e Streaks
- Sistema de streak com congelamentos (1 por 5 dias, max 5)
- Fluxo de avaliação (bloqueio de 5 dias, auto-avaliação)
- Lógica de avanço de degrau
- Transição de fase (introdutória → vitalícia no degrau 16)

### Fase 4 — Histórico e Polimento
- Tela de histórico (calendário, timeline de degraus, estatísticas)
- Fluxo de onboarding (cards introdutórios)
- Notificação de lembrete diário (WorkManager)
- Tela de configurações

### Fase 5 — Build e QA
- Configuração ProGuard/R8
- Build de APK de release
- Testes no dispositivo físico
- Revisão final dos textos em pt-BR
