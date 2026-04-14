# Discord bot (CSCI 220)

**Runtime (fixed):** `BotBootstrap` loads **AWS Secrets Manager** → `BotConfiguration` (token + channel id/name). `ChannelScope` resolves the target text channel after JDA connects. **Plug-ins** add behavior.

**Default plug-in:** `MinimalDeploymentPlugin` — one “online” line in the target channel + **`!ping`** → **`pong`** (deployment smoke test).

**Your bot logic:** implement `edu.moravian.csci220.discordbot.spi.DiscordBotPlugin`:

- `afterChannelScopeReady(JDA, ChannelScope)` — runs right after the channel is bound (good for one-shot announcements).
- `createListeners(ChannelScope, BotConfiguration)` — return JDA listeners; use `scope.isTargetChannel(channel)` so commands only run in the configured channel.
- `extraIntents()` — optional extra gateway intents.

Register implementations in **`src/main/resources/META-INF/services/edu.moravian.csci220.discordbot.spi.DiscordBotPlugin`** (one fully qualified class name per line). To **replace** the default, remove `MinimalDeploymentPlugin` from that file and list only your class. To **add** behavior, append your class on a new line (both load). If the file is missing or empty, `MinimalDeploymentPlugin` is used as a fallback.

The shaded JAR includes **`ServicesResourceTransformer`** so `ServiceLoader` works inside the fat jar.

---

**Secret / env:** same as before — `DISCORD_TOKEN`, `CHANNEL_NAME` / ids, `AWS_REGION`, `AWS_SECRET_NAME`. **Message Content Intent** on in the Developer Portal.

**Local:** `bash scripts/local-deploy.sh`  
**EC2:** `bash scripts/run-bot.sh`  
**Manual:** `mvn -q -DskipTests package && java -jar target/discord-bot-1.0.0.jar`

### CI Status

![Testing](https://github.com/cs220s26/britan-jackson-alex-project-repo
/actions/workflows/run_tests.yml/badge.svg)
