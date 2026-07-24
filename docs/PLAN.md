# ReadBridge — Plano do Aplicativo Android (leitor com backend Wallabag)

## 1. Visão geral

**ReadBridge** é um leitor de artigos "read-it-later" para Android que usa uma
instância **Wallabag** (self-hosted ou wallabag.it) como provider/backend. O app
não armazena os artigos como fonte da verdade: ele sincroniza com o Wallabag via
sua API REST, faz cache local para leitura offline e reflete de volta ações do
usuário (arquivar, favoritar, tags, anotações, progresso de leitura).

Objetivo do MVP: **login → lista de artigos → leitura offline confortável →
arquivar/favoritar sincronizados**.

## 2. Decisões de stack (recomendadas)

| Área | Escolha | Motivo |
|------|---------|--------|
| Linguagem | **Kotlin** | Padrão Android; `.gitignore` já é de projeto Gradle nativo |
| UI | **Jetpack Compose + Material 3** | UI declarativa, tema dinâmico, dark mode |
| Arquitetura | **MVVM + Clean Arch** (camadas `data` / `domain` / `ui`) | Testável, separação clara |
| Estado | Coroutines + **Flow** (UDF, `StateFlow`) | Reativo, um só fluxo de estado |
| DI | **Hilt** | Injeção padrão do ecossistema |
| Rede | **Retrofit + OkHttp + kotlinx.serialization** | Cliente REST + interceptors de auth |
| Persistência | **Room** (offline-first) + **Paging 3** | Cache de entries, listas grandes paginadas |
| Auth storage | **EncryptedSharedPreferences** (tokens) + DataStore (prefs) | Tokens sensíveis criptografados |
| Imagens | **Coil** | Carregamento de `preview_picture` e imagens do conteúdo |
| Reader | **WebView** com CSS próprio (tipografia/tema) | Conteúdo do Wallabag já vem em HTML sanitizado |
| TTS | Android `TextToSpeech` (fase 2) | Ouvir artigos |
| Background sync | **WorkManager** | Sync periódico e ações offline enfileiradas |
| Testes | JUnit, Turbine, **MockWebServer**, Compose UI Test | Unit + integração de rede + UI |
| Min SDK | **API 26 (Android 8)**, target = SDK mais recente | Cobertura ampla + APIs modernas |

## 3. Integração com o Wallabag (a parte central)

O Wallabag é **self-hosted**, então cada usuário tem seu próprio servidor. O
onboarding precisa capturar: **URL do servidor**, **client_id**, **client_secret**
(criados em *Wallabag → Configurações do desenvolvedor → Criar novo cliente*),
**usuário** e **senha**.

### 3.1 Autenticação (OAuth2 — password grant)
- `POST {server}/oauth/v2/token`
  - `grant_type=password`, `client_id`, `client_secret`, `username`, `password`
  - Resposta: `access_token`, `refresh_token`, `expires_in`
- Refresh: `POST {server}/oauth/v2/token` com `grant_type=refresh_token`.
- **OkHttp `Authenticator`** faz refresh automático em `401` e reenvia a request;
  se o refresh falhar → forçar re-login.
- Tokens em `EncryptedSharedPreferences`.

### 3.2 Endpoints principais usados
| Ação | Endpoint |
|------|----------|
| Listar artigos | `GET /api/entries.json` (params: `archive`, `starred`, `sort`, `order`, `page`, `perPage`, `since`, `tags`, `detail`) |
| Sync incremental | `GET /api/entries.json?since={epoch}` |
| Artigo único | `GET /api/entries/{id}.json` |
| Adicionar URL | `POST /api/entries.json` (`url`) |
| Arquivar/favoritar/editar | `PATCH /api/entries/{id}.json` (`archive`, `starred`, `tags`, `title`) |
| Excluir | `DELETE /api/entries/{id}.json` |
| Tags | `GET /api/tags.json`, `POST /api/entries/{id}/tags.json`, `DELETE .../tags/{tag}` |
| Anotações | `GET/POST /api/annotations/{entry}.json` |
| Export (epub/pdf) | `GET /api/entries/{id}/export.{format}` |
| Versão/info | `GET /api/info.json`, `GET /api/version.json` |

**Campos de entry relevantes:** `id`, `title`, `url`, `content` (HTML pronto para
render), `created_at`, `updated_at`, `is_archived`, `is_starred`, `reading_time`,
`preview_picture`, `domain_name`, `tags`, `annotations`.

> Nota de compatibilidade: usar `GET /api/info.json` no login para detectar a
> versão do servidor e degradar recursos que dependam de versão (ex.: anotações).

## 4. Modelo de dados local (Room)

- `EntryEntity` — espelha os campos acima + `contentCachedAt`, `syncState`.
- `TagEntity` + `EntryTagCrossRef` (relação N:N).
- `AnnotationEntity`.
- `PendingActionEntity` — **outbox** de ações feitas offline (archive/star/tag/
  delete/add-url) para reenvio pelo WorkManager.
- `ReadingProgressEntity` — posição de scroll por artigo (local; opcionalmente
  espelhado em tag/annotation no servidor).

**Estratégia offline-first:** a UI lê sempre do Room (via Flow/Paging); a rede
apenas atualiza o Room. Ações do usuário gravam no Room + enfileiram
`PendingAction`; um `SyncWorker` reconcilia com o servidor e resolve conflitos
por *last-write-wins* baseado em `updated_at`.

## 5. Arquitetura de camadas

```
ui/         Compose screens + ViewModels (StateFlow, eventos)
domain/     Modelos puros + UseCases + interfaces de Repository
data/
  remote/   Retrofit API, DTOs, Authenticator, interceptors
  local/    Room DAOs, entities, DataStore
  repo/     Implementações de Repository (single source of truth = Room)
  sync/     WorkManager workers (SyncWorker, PushActionsWorker)
di/         Módulos Hilt
```

## 6. Telas (MVP)

1. **Onboarding / Login** — server URL, client id/secret, user/pass; validação e
   teste de conexão (`/api/version`).
2. **Lista de artigos** — abas/filtros: *Não lidos / Favoritos / Arquivados / Tags*;
   busca; pull-to-refresh; paginação infinita; card com imagem, título, domínio,
   tempo de leitura.
3. **Leitor** — WebView com HTML do artigo + painel de leitura customizável
   (ver §6-A), barra de progresso e ações rápidas (favoritar, arquivar, tags,
   abrir original, compartilhar).
4. **Adicionar artigo** — colar URL + **Share Target** (compartilhar link de
   qualquer app → salva no Wallabag).
5. **Configurações** — conta/logout, intervalo de sync, tema, limpar cache,
   preferências de tipografia.

## 6-A. Experiência de leitura (detalhada)

O leitor é a tela mais importante do app. O conteúdo vem do Wallabag como HTML
já limpo (readability), então a estratégia é renderizá-lo num **WebView** cujo
visual é 100% controlado por um **template HTML + CSS com variáveis** que refletem
as preferências do usuário. Ajustar uma preferência atualiza as **CSS variables**
via `evaluateJavascript(...)` — mudança **em tempo real, sem recarregar** o artigo
nem perder a posição de leitura.

### 6-A.1 Formatação do texto (liberdade total)
Painel deslizante (bottom sheet "Aa") com controles ao vivo:

| Controle | Faixa / opções | CSS/efeito |
|----------|----------------|------------|
| **Tamanho da fonte** | slider 12–32 sp (passo 1) | `font-size` |
| **Família da fonte** | Serif / Sans-serif / Slab / Mono / **OpenDyslexic** | `font-family` (fontes empacotadas em `assets`) |
| **Peso** | Leve / Normal / Médio / Negrito | `font-weight` |
| **Entrelinha** | 1.2 – 2.2 (altura de linha) | `line-height` |
| **Espaço entre parágrafos** | Compacto → Amplo | `margin` de `<p>` |
| **Espaço entre letras** | -0.02em – 0.08em | `letter-spacing` |
| **Largura da coluna** | Estreita / Média / Larga / Cheia | `max-width` + margens |
| **Margens laterais** | slider | `padding` |
| **Alinhamento** | Esquerda / Justificado | `text-align` |
| **Hifenização** | on/off | `hyphens` |
| **Texto em negrito realçado** | on/off | reforço de contraste |

Extras de conforto:
- **Preview ao vivo** — todo controle reflete imediatamente no artigo por trás do
  sheet.
- **Reset** — voltar ao padrão do tema.
- Aumentar/diminuir fonte rápido por **gesto de pinça (pinch-to-zoom)** e/ou
  botões de volume (opcional, em Configurações).

### 6-A.2 Temas de leitura
Temas prontos, cada um define fundo, cor de texto, cor de link e cor de seleção:

- **Claro** (branco/quase-branco)
- **Sépia** (creme, quente — reduz cansaço)
- **Cinza** (dark suave, cinza-escuro)
- **Escuro/OLED** (preto puro `#000` — economia em telas AMOLED)
- **Seguir o sistema** (claro/escuro automático)

Personalização:
- **Tema customizado** — usuário escolhe cor de fundo e cor de texto (color
  picker) e o app valida o **contraste (WCAG AA)** avisando se ficar ilegível.
- **Brilho no app** — override de brilho só dentro do leitor (slider),
  independente do sistema.
- Imagens/tabelas/code blocks recebem CSS específico por tema (ex.: fundo de
  bloco de código, bordas de tabela) para não "quebrar" no modo escuro.

### 6-A.3 Conforto e navegação
- **Barra de progresso** + **% lido** e **tempo restante** estimado.
- **Auto-scroll** com velocidade ajustável (para leitura mãos-livres).
- **Modo foco / imersivo** — esconde barras do sistema e chrome do app.
- **Manter tela ligada** enquanto lê (toggle).
- **Retomar leitura** — ao reabrir, volta à posição salva (`ReadingProgressEntity`).
- Ir para próximo/anterior artigo sem sair do leitor.
- (Fase 5) **TTS** e **highlights/anotações** reaproveitam o mesmo template.

### 6-A.4 Persistência das preferências
- Todas as opções ficam em **DataStore (Preferences)** como um objeto
  `ReadingPreferences` (tema, fonte, tamanho, entrelinha, largura, etc.),
  exposto como `Flow<ReadingPreferences>` e injetado no `ReaderViewModel`.
- São **globais por padrão** (valem para todos os artigos) e aplicadas na
  abertura de qualquer leitor.
- Mudanças persistem **imediatamente** (cada ajuste do painel grava no DataStore),
  então sobrevivem a fechar o app, reabrir e reiniciar o dispositivo.
- **Progresso de leitura por artigo** é salvo separadamente no Room
  (`ReadingProgressEntity`, chaveado por `entryId`), não no DataStore global.
- Migração de schema de preferências versionada (para evoluir opções sem quebrar
  quem já tinha configurado).
- (Opcional, fase 5) **presets nomeados** ("Dia", "Noite", "Dislexia") que
  agrupam um conjunto de opções para troca rápida.

## 7. Roadmap por fases

**Fase 0 — Fundação (scaffold)**
- Projeto Gradle (version catalog), Compose, Hilt, navegação, tema Material 3.
- CI (GitHub Actions: build + lint + testes).

**Fase 1 — Auth + rede**
- Fluxo OAuth2, storage seguro de tokens, Authenticator com refresh.
- Camada Retrofit + DTOs + tela de login funcional.

**Fase 2 — Lista + cache offline (MVP core)**
- Room + Paging 3, repositório offline-first, sync inicial e incremental (`since`).
- Filtros (unread/starred/archived/tags) + busca + pull-to-refresh.

**Fase 3 — Leitor (ver §6-A)**
- WebView com template HTML + CSS variables; painel "Aa" de formatação ao vivo
  (fonte, tamanho, entrelinha, largura, alinhamento…).
- Temas de leitura (claro/sépia/cinza/OLED/sistema) + tema customizado com
  checagem de contraste; `ReadingPreferences` em DataStore (persistência global).
- Progresso de leitura por artigo (Room), retomar leitura, modo foco.
- Ações (star/archive/tags) com escrita otimista + outbox.

**Fase 4 — Escrita/sync robusto**
- Adicionar URL + Share Target; `SyncWorker` periódico; reconciliação de conflitos;
  reprocessamento da fila offline.

**Fase 5 — Extras**
- TTS (ouvir artigo), anotações/highlights, export epub/pdf, widget, atalhos,
  temas dinâmicos, ordenação personalizada.

## 8. Segurança e robustez
- Suportar apenas HTTPS por padrão; avisar em HTTP (instâncias locais).
- Nunca logar tokens; secrets fora do VCS.
- Tratar servidores auto-hospedados com certificados/versões variadas.
- WebView: JavaScript desativado no reader por segurança; sanitização confiando
  no HTML já limpo do Wallabag; imagens via Coil/cache.

## 9. Testes
- **Unit:** UseCases, mapeadores DTO↔entity, lógica de sync/conflito.
- **Rede:** MockWebServer para fluxos OAuth (incl. refresh em 401) e paginação.
- **DB:** DAOs Room in-memory.
- **UI:** Compose tests das telas principais; Turbine para `StateFlow`.

## 10. Riscos / pontos em aberto
- **Onboarding com client_id/secret** é atrito: exige o usuário criar o cliente
  API no Wallabag. Mitigar com um guia passo-a-passo na tela de login.
- **Variação de versões** de servidor (2.4 / 2.5 / 2.6): feature-detection.
- **Progresso de leitura** não é nativo na API — decidir se fica só local ou é
  espelhado (ex.: via annotation/tag) para sincronizar entre dispositivos.
- Renderização de HTML complexo (tabelas, code blocks) precisa de CSS caprichado.
```
