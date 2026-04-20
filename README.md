# StudyBuddy — Discord bot (CSCI 220)

**StudyBuddy** is a Java Discord bot built with **JDA** that helps students coordinate **study groups**, log **sessions**, and surface **XP / leaderboard** style feedback through prefix commands (`!help`, `!session`, `!group`, and friends). The project is structured so configuration is pulled from **AWS Secrets Manager** at startup, the app ships as a **single shaded JAR**, and production hosts can run it under **systemd** on **Amazon Linux** with **IAM role–based** access to secrets—no token baked into the repo.

**Why it exists (course context):** the repo is meant to exercise the CSCI 220 DevOps story—**GitHub** for source control, **Maven** for build and tests, **AWS Secrets Manager** for the Discord token, **EC2 + user data + systemd** for deployment, and **GitHub Actions** for continuous integration on `main`.

> **Architecture sketch (diagram TBD):** Discord clients → **JDA** on EC2 (`bot.service`) → **Secrets Manager** for credentials. **Data layer:** **Redis** (via **Jedis**) — **not implemented yet.**

---

## What you can do with the bot

- **Groups:** `!group create|join|leave|info <name>` — scaffold messaging for group lifecycle.
- **Sessions:** `!session start <subject>`, `!session end`, plus placeholders for `status` and `history`.
- **Gamification:** `!xp`, `!xp rank <group>`, `!leaderboard <group>`.
- **Basics:** `!ping`, `!about`, `!help`.

Commands are handled in **`StudyBuddyCommandHandler`**. An optional **channel name** in the **Secrets Manager** value (JSON) ties the bot to a single text channel via **`ChannelScope`** so traffic stays in one place—there is no `.env` file and no Discord/channel settings read from the local machine’s environment.

---

## Stack

| Layer | Choices |
|--------|---------|
| Runtime | Java **17** (Maven `release` 17); CI uses JDK **21** (Temurin) to run `mvn verify` |
| Discord | **JDA 5**, intents: `GUILD_MESSAGES`, `MESSAGE_CONTENT` |
| Config / secrets | **AWS SDK for Java v2** → Secrets Manager only (token + optional channel in JSON); **Gson** for parsing |
| Data | **Redis** (Jedis) — chosen data layer; **not implemented yet** |
| Build | **Maven** (shade plugin → `target/discord-bot-1.0.0.jar`) |
| Tests | **JUnit 5** |
| Server | **Amazon Linux** + **systemd** (`bot.service`), bootstrap via `userdata.sh` |
| CI | **GitHub Actions** — `.github/workflows/run_tests.yml` |
| Static analysis | **Checkstyle** — *to be implemented* (Maven plugin + `checkstyle:check`, wired into CI alongside tests) |

CI currently runs **`mvn -B verify`** (compile, test, package). **Checkstyle** is not configured yet; adding it is planned so pushes to `main` enforce a shared style gate. Deployment to EC2 is **manual** (see below)—there is no separate “CD” workflow that SSHs from GitHub.

---

## Run (development machine)

**You need:** Git, **Java 17**, **Maven 3.9+**, AWS credentials that can call `secretsmanager:GetSecretValue` for your secret, and a Discord application with **Message Content Intent** enabled.

Configuration comes from **AWS Secrets Manager** and the **default AWS credential chain** (for example `~/.aws/credentials` in a Learner Lab session). The bot uses built-in defaults **`us-east-1`** for the region and **`220_Discord_Token`** for the secret id unless the **host** sets `AWS_REGION` / `AWS_SECRET_NAME` (for example **`Environment=`** lines in **`bot.service`** on EC2). Do not use a `.env` file or local exports for Discord or channel settings.

### 1. Clone

```bash
git clone https://github.com/cs220s26/britan-jackson-alex-project-repo.git
cd britan-jackson-alex-project-repo
```

### 2. Store the Discord token in Secrets Manager

Create a secret named **`220_Discord_Token`** (or another name—if you change it, set `AWS_SECRET_NAME` on the host that runs the bot).

The **secret value** in AWS can be stored in either of two shapes. The bot decides which shape you used by looking at the **first non-whitespace character** of the string: if it is **`{`**, the value is treated as **JSON**; otherwise the **entire string** is treated as the Discord token.

**Plain string (“token alone”)**

- Paste **only** the bot token text you copied from the [Discord Developer Portal](https://discord.com/developers/applications) (Bot section → **Token**).  
- No JSON, no quotes, no curly braces, no key names—just one line of characters, for example:  
  `MTxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx.xxxxxx.xxxxxxxxxxxxxxxxxxxxxxxxxxx`  
- The whole secret body is used as `discordToken`. There is **no** channel name in this format, so the bot listens everywhere your handler allows (not locked to one channel by config).

Use this when you only need the token in Secrets Manager and you are fine configuring an optional channel name later by switching to JSON or by changing the secret.

**JSON object**

- Use JSON when you want **both** the token **and** optional metadata in one secret.  
- Include a token field using one of: **`DISCORD_TOKEN`**, **`discord_token`**, or **`token`**.  
- Optionally add a Discord text channel **name** (not the numeric ID) with **`DISCORD_CHANNEL_NAME`** or **`CHANNEL_NAME`** so **`ChannelScope`** can restrict commands to that channel.  
- If those channel keys are missing or empty, behavior matches “token alone” for channel binding: the bot is **not** tied to a single channel by name from the secret.

### 3. AWS CLI profile (e.g. Learner Lab)

Put temporary credentials in `~/.aws/credentials` under `[default]` so **DefaultCredentialsProvider** can resolve them.

### 4. Build and run

```bash
mvn -q package
java -jar target/discord-bot-1.0.0.jar
```

Ensure the secret exists in the **same region** as the default (`us-east-1`) or set `AWS_REGION` on the machine that runs the JVM (for example in **`bot.service`** on EC2).

---

## First-time production setup (EC2)

Rough path; the secret must live in the same AWS region as **`AWS_REGION`** in **`bot.service`** (default **`us-east-1`**).

1. **Instance:** Amazon Linux 2023 (or compatible), security group allowing **SSH (22)** from your IP; attach an **IAM instance profile** that can read the secret (e.g. Learner Lab **`LabInstanceProfile`** / **`LabRole`** pattern).
2. **User data:** paste **`userdata.sh`** from this repo into **Advanced details → User data**. It installs Git, **Corretto 17**, Maven, clones into **`/opt/discord-bot`**, runs **`mvn package`**, installs **`bot.service`**, and enables the service. (Redis is not part of this script yet—the data layer is not implemented.)
3. **Edit `bot.service`** if your secret is in another region or uses another secret id—the **`Environment=`** lines set **`AWS_REGION`** and **`AWS_SECRET_NAME`** for the process (no `.env` on the instance).
4. **Verify:**

```bash
sudo systemctl status bot.service
sudo journalctl -u bot.service -f --no-pager
```

The JAR path is **`/opt/discord-bot/target/discord-bot-1.0.0.jar`** as referenced in the unit file.

### Redeploying after changes

On the server, use the included script (or equivalent): it pulls `main` and restarts the unit—see **`redeploy.sh`**.

---

## Continuous integration (GitHub Actions)

Workflow: **`.github/workflows/run_tests.yml`**

| Trigger | Behavior |
|---------|----------|
| Push to **`main`** | Checkout → JDK 21 (Temurin) → Maven cache → **`mvn -B verify`** |
| Pull request into **`main`** | Same |

No AWS secrets are required for CI—it only builds and tests in GitHub’s runner. **Checkstyle** will be added to this workflow (and `pom.xml`) when static analysis is implemented.

[![Testing](https://github.com/cs220s26/britan-jackson-alex-project-repo/actions/workflows/run_tests.yml/badge.svg)](https://github.com/cs220s26/britan-jackson-alex-project-repo/actions/workflows/run_tests.yml)

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
