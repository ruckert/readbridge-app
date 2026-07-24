# ReadBridge

Leitor de artigos "read-it-later" para **Android** que usa uma instância
**[Wallabag](https://wallabag.org/)** (self-hosted ou wallabag.it) como
provider/backend. Foco em leitura offline e uma experiência de leitura altamente
customizável (tipografia, temas, persistência de preferências).

> 📄 Plano completo do produto e da arquitetura: [`docs/PLAN.md`](docs/PLAN.md)
> (experiência de leitura detalhada em **§6-A**).

## Status

**Fase 0 — Scaffold** ✅
Projeto Android nativo (Kotlin + Jetpack Compose + Material 3), Hilt, Navigation,
tema claro/escuro (Material You), telas placeholder navegáveis e CI no GitHub Actions.

**Fase 1 — Autenticação OAuth2 + rede** ✅
Login funcional contra uma instância Wallabag (self-hosted ou wallabag.it):
- Fluxo OAuth2 *password grant* (`/oauth/v2/token`) + **refresh automático** em 401
  via OkHttp `Authenticator`.
- Base URL **dinâmica** (interceptor) — suporta instalações em subpath.
- Tokens em **`EncryptedSharedPreferences`**; estado de sessão reativo (`StateFlow`)
  dirige a navegação (login ⇄ app).
- Tela de login (URL, client id/secret, usuário, senha) com validação e mensagens
  de erro; tela pós-login com logout.

## Stack

Kotlin · Jetpack Compose · Material 3 · Hilt · Navigation Compose ·
**Retrofit + OkHttp + kotlinx.serialization** · **security-crypto** · DataStore ·
Coroutines/Flow. (Room e WorkManager entram nas fases seguintes.)

## Requisitos

- **JDK 17**
- Android Studio (Ladybug ou mais recente) **ou** Android SDK via linha de comando
- `compileSdk = 35`, `minSdk = 26`

## Build & run

```bash
# testes unitários
./gradlew testDebugUnitTest

# APK de debug
./gradlew assembleDebug

# instalar em um device/emulador conectado
./gradlew installDebug
```

Ou abra a pasta no Android Studio e rode a configuração `app`.

## Estrutura

```
app/src/main/java/com/readbridge/app/
├─ ReadBridgeApp.kt        # Application + Hilt
├─ MainActivity.kt         # Compose host + edge-to-edge
├─ ui/theme/               # Material 3 (cores, tipografia, tema claro/escuro)
├─ ui/navigation/          # NavGraph + rotas
├─ ui/screens/             # telas (placeholders na Fase 0)
└─ di/                     # módulos Hilt
```

## Roadmap

Ver [`docs/PLAN.md`](docs/PLAN.md) §7. Próxima fase: **Fase 2 — Lista de artigos +
cache offline (Room + Paging 3, sync incremental)**.
