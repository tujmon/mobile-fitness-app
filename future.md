# HackerFit - Roadmap de Melhorias Futuras

> Versão atual: **v1.0** | Última atualização: Maio 2026

---

## 1. Redesign Visual e Paleta de Cores

### Problema atual
- Paleta verde + laranja funciona mas parece genérica
- Falta identidade visual marcante
- Dark mode usa as mesmas cores apenas mais claras, sem adaptação real
- Tipografia customizada (`HackerFitTypography`) já existe mas não é aproveitada com hierarquia visual forte

### Nova paleta proposta

```
Light Theme:
  Primary:        #1DB954 (Verde Neon - energia, vitalidade)
  Primary Var:    #158A3F
  Secondary:      #6C63FF (Roxo Elétrico - progresso, conquistas)
  Secondary Var:  #5A52D5
  Tertiary:       #FF6B6B (Coral - alertas, streaks, urgência)
  Surface:        #FAFAFA
  Background:     #F5F5F5
  OnSurface:      #1A1A2E
  Card:           #FFFFFF (com elevation suave e border sutil)
  Success:        #00E676 (verde brilhante para completado)
  Gradient CTA:   #1DB954 -> #6C63FF (botões principais)

Dark Theme:
  Primary:        #2EE66A
  Primary Var:    #1DB954
  Secondary:      #9B93FF
  Secondary Var:  #6C63FF
  Tertiary:       #FF8A8A
  Surface:        #1A1A2E
  Background:     #0F0F1A
  OnSurface:      #EAEAEA
  Card:           #252540 (com border #3A3A5C)
  Success:        #69F0AE
```

### Mudancas visuais

- **Cards**: Adicionar bordas sutis (`1.dp`, `colorScheme.outlineVariant`) + shadow suave ao inves de elevation only
- **Botoes CTA**: Gradiente horizontal primary->secondary com cantos arredondados (`16.dp`)
- **Bottom bar**: Fundo `surface` com divider no topo, item ativo com pill colorida atras do icon
- **Streak badge**: Animacao de chama com shimmer gradient em vez de pulso simples
- **Icons**: Usar filled com gradient para estado ativo, outlined para inativo
- **Transicoes**: `AnimatedContent` com slide horizontal entre tabs, fade entre screens internas
- **Tipografia**: Display styles para numeros grandes (streak, stats), weight contrast forte (Bold titles + Regular body)

### Arquivos afetados
- `ui/theme/Theme.kt` - reescrever toda a color scheme
- `ui/components/StreakBadge.kt` - nova animacao shimmer
- `ui/navigation/BottomNavBar.kt` - pill indicator + novo estilo
- `ui/screens/home/HomeScreen.kt` - gradient CTA, card borders
- Todos os `*Screen.kt` - aplicar nova hierarquia visual

---

## 2. Nova Aba "Estatisticas" (4 tabs total)

### Motivacao
- Atualmente 3 tabs (Inicio, Historico, Config) fica desbalanceado visualmente
- Historico mistura calendario + stats + logs numa so screen, fica sobrecarregado
- Separar stats do historico deixa ambas mais focadas e limpas

### Nova estrutura de tabs

| # | Tab | Icon | Rota | Conteudo |
|---|-----|------|------|----------|
| 1 | **Inicio** | `Home` | `home` | Dashboard (rung atual, streak, workout de hoje, CTA) |
| 2 | **Estatisticas** | `BarChart` / `Leaderboard` | `stats` | Graficos semanais/mensais, records pessoais, progresso de rung |
| 3 | **Historico** | `Calendar` / `History` | `history` | Calendario interativo (ver item 3) + lista de treinos por dia |
| 4 | **Config** | `Settings` | `settings` | Settings + export/import (ver item 4) |

### Tela de Estatisticas - Conteudo

```
┌─────────────────────────────┐
│  📊 Suas Estatisticas       │
│                              │
│  ┌─────────────────────────┐ │
│  │  Streak Atual    12     │ │
│  │  Melhor Streak   28     │ │
│  │  Total Treinos   45     │ │
│  │  Dias praticou   38     │ │
│  └─────────────────────────┘ │
│                              │
│  ┌─ Grafico Semanal ───────┐ │
│  │  Bar chart: treinos     │ │
│  │  por dia da semana      │ │
│  │  (ultimos 7 dias)       │ │
│  └─────────────────────────┘ │
│                              │
│  ┌─ Progressao de Rung ────┐ │
│  │  Timeline vertical      │ │
│  │  mostrando quando       │ │
│  │  subiu de rung          │ │
│  └─────────────────────────┘ │
│                              │
│  ┌─ Records Pessoais ──────┐ │
│  │  Maior seq: 28 dias     │ │
│  │  Rung atual: 12         │ │
│  │  Treinos mes: 18        │ │
│  └─────────────────────────┘ │
└─────────────────────────────┘
```

### Dados necessarios do ViewModel
- Total de treinos completados (count de `DailyLog` com `completed=true`)
- Melhor streak historico (calcular a partir dos `DailyLog` em sequencia)
- Grafico semanal: count por `DayOfWeek` dos ultimos 7/30 dias
- Progressao de rung: datas em que `AssessmentLog` registrou promocao
- Records: derivados dos dados acima

### Arquivos a criar/modificar
- **NOVO** `ui/screens/stats/StatsScreen.kt` - UI da tela
- **NOVO** `ui/screens/stats/StatsViewModel.kt` - logica e queries
- `ui/navigation/HackerFitNavHost.kt` - adicionar rota `"stats"`
- `ui/navigation/BottomNavBar.kt` - adicionar 4o item + ajustar layout
- `data/local/db/dao/DailyLogDao.kt` - novas queries (count por periodo, sequencia)
- `data/local/db/dao/AssessmentLogDao.kt` - query de promocoes

---

## 3. Calendario Interativo no Historico

### Problema atual
- Calendario mostra so o mes atual (sem navegacao entre meses)
- Dias completados ficam verdes mas **nao sao clicaveis**
- Nao ha como ver o que foi feito em cada dia
- Nao diferencia: treino completo vs treino parcial vs dia sem dados

### Melhorias propostas

#### 3.1 Navegacao entre meses
- Adicionar `<` e `>` arrows ao lado do nome do mes (ex: `< Maio 2026 >`)
- Swipe horizontal para trocar mes (`HorizontalPager` com `YearMonth`)
- Estado no `HistoryViewModel`: `selectedMonth: YearMonth` (default = now)

#### 3.2 Tap no dia para ver detalhes
- Ao clicar num dia que tem dados, abrir **bottom sheet** com:
  ```
  ┌─────────────────────────────┐
  │  📅 15 de Maio, Quinta      │
  │                              │
  │  Rung: 12 - Intermediario    │
  │  Status: ✅ Completo         │
  │                              │
  │  Exercicios:                 │
  │  • Agachamentos   3x15  ✓   │
  │  • Flexoes        3x10  ✓   │
  │  • Prancha        3x30s ✓   │
  │  • Abdominais     3x15  ✓   │
  │  • Corrida        3x(2m) ✓  │
  │                              │
  │  [Ver detalhes do treino]    │
  └─────────────────────────────┘
  ```
- Para dias sem dados: mostrar "Nenhum treino registrado" com opcao de adicionar manualmente
- Para dias futuros: nao clicavel, visual atenuado

#### 3.3 Estados visuais do dia no calendario
| Estado | Visual |
|--------|--------|
| Treino completo | Circulo cheio primary (verde neon) + texto branco |
| Treino parcial | Circulo com border primary + icone mini check |
| Dia sem treino (dentro de streak) | Circulo com border coral (tertiary) - sinal de atencao |
| Hoje | Ring animado ao redor (primary) |
| Dia futuro | Texto atenuado (`onSurface.copy(alpha = 0.3f)`), nao clicavel |
| Selecionado | Elevacao + scale(1.1f) com animacao |

#### 3.4 Lista de treinos abaixo do calendario
- Abaixo do calendario, mostrar lista scrollavel dos treinos do mes selecionado
- Cada item: data + rung + status (completo/parcial) + exercicios resumo
- Clicar no item abre o mesmo bottom sheet de detalhes

### Arquivos a criar/modificar
- `ui/screens/history/HistoryScreen.kt` - reescrever calendario com interacao
- `ui/screens/history/HistoryViewModel.kt` - `selectedMonth`, dados por dia
- **NOVO** `ui/components/DayDetailBottomSheet.kt` - bottom sheet de detalhes do dia
- `data/local/db/dao/DailyLogDao.kt` - query por data especifica com exercicios
- `domain/model/Models.kt` - modelo estendido com dados de exercicio por dia

---

## 4. Exportacao e Importacao de Dados

### Motivacao
- Dados 100% locais = risco de perda total se o app for desinstalado ou device trocar
- Botao "Apagar Todos os Dados" existe sem backup
- Usuarios precisam portar dados entre dispositivos

### Formato escolhido: JSON

```json
{
  "version": 1,
  "app": "HackerFit",
  "exportDate": "2026-05-09T14:30:00Z",
  "profile": {
    "name": "Usuario",
    "currentRung": 12,
    "startDate": "2026-01-15"
  },
  "streak": {
    "currentStreak": 12,
    "freezesBanked": 3,
    "lastFreezeEarnDate": "2026-05-01"
  },
  "dailyLogs": [
    {
      "date": "2026-05-09",
      "rung": 12,
      "completed": true,
      "exercises": [
        { "name": "Agachamentos", "sets": 3, "repsPerSet": 15 },
        { "name": "Flexoes", "sets": 3, "repsPerSet": 10 }
      ]
    }
  ],
  "assessmentLogs": [
    {
      "date": "2026-05-05",
      "previousRung": 11,
      "newRung": 12,
      "passed": true
    }
  ]
}
```

### Funcionalidades

#### 4.1 Exportar dados
- Botao na tela de Config: **"Exportar Dados"** (com icon de download)
- Gera JSON com todos os dados do Room + DataStore
- Usa `ActivityResultContracts.CreateDocument()` para salvar como `.json` no local escolhido pelo usuario
- Opcao de compartilhar via `Intent.ACTION_SEND` (ShareSheet do Android)
- Mostrar toast: `"Dados exportados com sucesso!"`

#### 4.2 Importar dados
- Botao na tela de Config: **"Importar Dados"** (com icon de upload)
- Usa `ActivityResultContracts.OpenDocument()` para abrir picker de arquivos (.json)
- **Fluxo de importacao**:
  1. Ler e parsear JSON
  2. Validar `version` e estrutura
  3. Se ja existem dados: perguntar **"Substituir todos os dados?"** ou **"Mesclar dados"**
     - **Substituir**: apaga tudo e importa
     - **Mesclar**: mantem o mais recente por data para cada entry
  4. Importar para Room + DataStore
  5. Mostrar resumo: `"Importados: 45 treinos, 5 avaliacoes, perfil atualizado"`
  6. Refresh de todas as telas

#### 4.3 Auto-backup (opcional/futuro)
- Backup automatico semanal para armazenamento interno do app
- Sugestao de exportar quando detectar que passou 30+ dias sem export

### UI na tela de Config

```
┌─────────────────────────────┐
│  ⚙️ Configuracoes           │
│                              │
│  ┌─ Dados ─────────────────┐ │
│  │                          │ │
│  │  📤 Exportar Dados       │ │
│  │  Salvar tudo como JSON   │ │
│  │                          │ │
│  │  📥 Importar Dados       │ │
│  │  Restaurar de arquivo    │ │
│  │                          │ │
│  │  🗑️ Apagar Todos os Dados│ │
│  │  (agora com aviso de     │ │
│  │   "exportar antes")      │ │
│  └─────────────────────────┘ │
│                              │
│  ┌─ Lembretes ─────────────┐ │
│  │  (ja existente)          │ │
│  └─────────────────────────┘ │
│                              │
│  HackerFit v1.0              │
└─────────────────────────────┘
```

### Arquivos a criar/modificar
- **NOVO** `data/export/DataExporter.kt` - serializar Room + DataStore para JSON
- **NOVO** `data/export/DataImporter.kt` - parsear JSON + validar + escrever no Room + DataStore
- **NOVO** `domain/model/ExportData.kt` - data classes do formato JSON
- **NOVO** `ui/components/ImportConflictDialog.kt` - dialog substituir/mesclar
- `ui/screens/settings/SettingsScreen.kt` - adicionar botoes de export/import
- `ui/screens/settings/SettingsViewModel.kt` - logica de export/import + activity results
- `app/build.gradle.kts` - adicionar dependencia `kotlinx.serialization` (ou usar Gson/Moshi ja disponivel)

---

## Prioridade de Implementacao

| Ordem | Feature | Esforco | Impacto |
|-------|---------|---------|---------|
| 1 | Nova paleta de cores + visual | Medio | Alto - mudanca imediata de percepcao |
| 2 | Calendario interativo | Medio | Alto - principal痛点 dos usuarios |
| 3 | Aba Estatisticas | Medio | Medio - engajamento e motivacao |
| 4 | Export/Import de dados | Medio | Alto - seguranca dos dados do usuario |

### Ordem recomendada
1. **Cores** primeiro (base para todo o resto)
2. **Calendario** (maior reclamo de usabilidade)
3. **Aba Estatisticas** (aproveitar que vai mexer no calendario para reorganizar historico)
4. **Export/Import** (fechar com seguranca de dados)

---

## Notas Tecnicas

- Tudo continua **100% offline**, sem servidor
- Manter compatibilidade com **Android 12+** (minSdk 31)
- Manter **Jetpack Compose + Material 3** como base
- Para graficos na tela de Estatisticas: avaliar adicionar `Vico` (compose chart library) ou implementar charts custom com `Canvas`
- Para serializacao JSON: adicionar `kotlinx.serialization` ao build.gradle.kts
- Tests: cada feature nova deve ter testes unitarios no ViewModel e testes de DAO
