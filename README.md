# StudyBuddy Discord Bot

Java Discord bot using [JDA](https://github.com/DV8FromTheWorld/JDA).

- **Commands / bot logic**: `edu.moravian.StudyBuddyCommandHandler`
- **Entry point**: `edu.moravian.csci220.discordbot.BotBootstrap`
- **Optional channel lock**: set a channel name so commands only run there

## Run

```bash
AWS_REGION=us-east-1 AWS_SECRET_NAME=220_Discord_Token mvn -q package && \
java -jar target/discord-bot-1.0.0.jar
```

## EC2 deployment (systemd)

This repo includes:
- `userdata.sh` (paste into EC2 **User data**)
- `bot.service` (systemd unit the instance installs/enables)

### Prereqs

- **Amazon Linux** instance (uses `yum`)
- **IAM role on the instance** that can read your Secrets Manager secret (e.g. `secretsmanager:GetSecretValue`)
- Your secret exists in **the same region** as `AWS_REGION`

### Steps

1. Launch an EC2 instance and attach an IAM role with Secrets Manager read access.
2. In **Advanced details → User data**, paste the contents of `userdata.sh`.
3. (Optional) Edit `bot.service` in this repo before launching if you want different values:
   - `AWS_REGION`
   - `AWS_SECRET_NAME`
4. After the instance boots, SSH in and check status/logs:

```bash
sudo systemctl status bot.service
sudo journalctl -u bot.service --no-pager
```

## Config

- **AWS**: `AWS_REGION` (default `us-east-1`), `AWS_SECRET_NAME` (default `220_Discord_Token`)
- **Channel name (optional)**: `DISCORD_CHANNEL_NAME` or `CHANNEL_NAME`
- **Secret**: plain token string, or JSON with `DISCORD_TOKEN` / `discord_token` / `token`

## Flow

```mermaid
flowchart TD
  A[AWS Secrets Manager + env] --> B[BotConfiguration]
  B --> C[BotBootstrap]
  C --> D[ChannelScope - optional channel name filter]
  C --> E[StudyBuddyCommandHandler listener]
  D --> E
```

**Local:** `AWS_REGION=... AWS_SECRET_NAME=... mvn -q package && java -jar target/discord-bot-1.0.0.jar`

### CI Status

[![Testing](https://github.com/cs220s26/britan-jackson-alex-project-repo/actions/workflows/run_tests.yml/badge.svg)](https://github.com/cs220s26/britan-jackson-alex-project-repo/actions/workflows/run_tests.yml)
