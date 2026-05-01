# StudyBuddy — Discord bot (CSCI 220)

[![Testing](https://img.shields.io/github/actions/workflow/status/cs220s26/britan-jackson-alex-project-repo/run_tests.yml?branch=main&label=Testing)](https://github.com/cs220s26/britan-jackson-alex-project-repo/actions/workflows/run_tests.yml)

**StudyBuddy** is a Java (**JDA**) Discord bot for study groups, sessions, and XP/leaderboards.

---

## What you can do with the bot

- **Groups**: `!group create|join|leave|info <name>`
- **Sessions**: `!session start <subject>` / `!session end`
- **XP/leaderboards**: `!xp`, `!xp rank <group>`, `!leaderboard <group>`
- **Basic**: `!ping`, `!about`, `!help`

---

## Stack

| Layer | Choices |
|--------|---------|
| Runtime | Java **17** (Maven `release` 17); CI uses JDK **17** (Temurin) to run `mvn verify` |
| Discord | **JDA 5**, intents: `GUILD_MESSAGES`, `MESSAGE_CONTENT` |
| Config / secrets | **AWS SDK for Java v2** → Secrets Manager only (token + optional channel in JSON); **Gson** for parsing |
| Data | **Redis** (Jedis) — `RedisManager` implements `RedisRepository` |
| Build | **Maven** (shade plugin → `target/discord-bot-1.0.0.jar`) |
| Tests | **JUnit 5** |
| Server | **Ubuntu on EC2** + **systemd** (`bot.service`) + **Docker Compose**, bootstrap via `scripts/userdata.sh` |
| CI | **GitHub Actions** — `.github/workflows/run_tests.yml` |
| Static analysis | **Checkstyle** — runs during `mvn verify` using `config/checkstyle/checkstyle.xml` |

CI runs **`mvn -B verify`** (compile, tests, package, and **Checkstyle**). Deployment to EC2 is **manual** (see below)—there is no separate “CD” workflow that SSHs from GitHub.

---

## First-time production setup (EC2)

1. **Create the token secret**
   - In **AWS Secrets Manager** (region **`us-east-1`**), create **`220_Discord_Token`**
   - Secret value (Secret string) should be JSON like:

```json
{"DISCORD_TOKEN":"YOUR_DISCORD_BOT_TOKEN","CHANNEL_NAME":"<lastName>-bot"}
```

   - `CHANNEL_NAME` is optional:
     - If set, the bot will only respond in that channel name.
     - If omitted, the bot will respond in any channel it can see.

2. **Download your SSH key**
   - Download/save your Learner Lab key file (for example **`labuser.pem`**)

3. **Launch EC2**
   - AMI: **Ubuntu**
   - Security group: allow **SSH (22)** from your IP
   - Key pair (login): select the key pair named **`vockey`**
   - IAM: attach an **instance profile** that can read the secret

4. **Paste user data**
   - Paste **`scripts/userdata.sh`** into EC2 User data (installs Docker/Compose and enables `bot.service`)

5. **Verify**

```bash
sudo systemctl status bot.service
sudo journalctl -u bot.service -f --no-pager
```

### Redeploying after changes

On the server:

```bash
sudo /opt/discord-bot/scripts/redeploy.sh
```

---

## Run (development machine)

1. **Clone:**

```bash
git clone https://github.com/cs220s26/britan-jackson-alex-project-repo.git
cd britan-jackson-alex-project-repo
```

2. **AWS CLI + creds (so the bot can read Secrets Manager):**
   - Install AWS CLI and confirm it works:

```bash
aws --version
```

   - Copy and paste the following into `~/.aws/credentials` (replace values):

```ini
[default]
aws_access_key_id=YOUR_ACCESS_KEY_ID
aws_secret_access_key=YOUR_SECRET_ACCESS_KEY
aws_session_token=YOUR_SESSION_TOKEN
```

   - (Recommended) Set your default region in `~/.aws/config`:

```ini
[default]
region=us-east-1
```

3. **Secrets Manager:** create **`220_Discord_Token`** in **`us-east-1`** with a JSON secret string like:

```json
{"DISCORD_TOKEN":"YOUR_DISCORD_BOT_TOKEN","CHANNEL_NAME":"<lastName>-bot"}
```

   `CHANNEL_NAME` is optional:
   - If set, the bot will only respond in that channel name.
   - If omitted, the bot will respond in any channel it can see.

4. **Run bot + Redis:**

```bash
docker compose --profile bot up --build
```

- **Local creds into container**: `docker-compose.yml` mounts `${HOME}/.aws` read-only so the bot can read Secrets Manager.

To stop:

```bash
docker compose down
```

## Continuous integration (GitHub Actions)

Workflow: **`.github/workflows/run_tests.yml`**

| Trigger | Behavior |
|---------|----------|
| Push to **`main`** | Checkout → JDK 17 (Temurin) → Maven cache → **`mvn -B verify`** |
| Pull request into **`main`** | Same |

No AWS secrets are required for CI—it only builds and tests in GitHub’s runner. Because Checkstyle is wired into Maven’s `verify` phase, CI enforces it automatically.

**Runs:** [GitHub Actions tab](https://github.com/cs220s26/britan-jackson-alex-project-repo/actions/workflows/run_tests.yml)

---

## Project map

| Entry / wiring | `edu.moravian.csci220.discordbot.BotBootstrap` |
|----------------|------------------------------------------------|
| Config + secret parsing | `BotConfiguration` |
| Optional single-channel filter | `ChannelScope` |
| Listeners + startup hook | `BotHandlers` |
| Command implementations | `StudyBuddyCommandHandler` |

---

## References (helpful while building)

- [JDA wiki](https://github.com/DV8FromTheWorld/JDA/wiki) — intents and gateway setup
- [Discord Developer Portal](https://discord.com/developers/applications) — bot token and privileged intents
- [AWS Secrets Manager + Java SDK v2](https://docs.aws.amazon.com/secretsmanager/latest/userguide/secrets-manager-client.html)
- [systemd unit files](https://www.freedesktop.org/software/systemd/man/latest/systemd.service.html)
- [GitHub Actions — workflow syntax](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions)
