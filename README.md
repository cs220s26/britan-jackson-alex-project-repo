# Discord bot (CSCI 220)

This repo is a **shell**: it builds a runnable JAR, loads secrets from AWS, connects to Discord with [JDA](https://github.com/DV8FromTheWorld/JDA), binds an optional **text channel by name**, and attaches **plug-ins** that implement your behavior. The default plug-in is **`MinimalDeploymentPlugin`** (startup line + **`!ping`** → **`pong`**).

---

## End-to-end pipeline (repo → Discord)

Think of four linked pipelines: **build**, **run / deploy**, **configuration**, and **in-process startup + events**.

### 1. Build pipeline (source → fat JAR)

1. You edit Java under `src/main/java` (bootstrap, config, `ChannelScope`, plug-ins, SPI).
2. **`mvn package`** runs tests, then the **compiler** and the **Maven Shade** plugin.
3. Shade produces **`target/discord-bot-1.0.0.jar`**: one “fat” JAR with dependencies and a **`Main-Class`** of **`edu.moravian.csci220.discordbot.BotBootstrap`**.
4. **`META-INF/services/edu.moravian.csci220.discordbot.spi.DiscordBotPlugin`** is merged into that JAR (Shade **ServicesResourceTransformer**) so **`ServiceLoader`** can still discover plug-ins at runtime.

Until you run Maven, there is no single runnable artifact; after package, the JAR is the whole app.

### 2. Run / deploy pipeline (how the JAR is started)

| Path | What happens |
|------|----------------|
| **`bash scripts/local-deploy.sh`** | `cd` to repo root, optionally **`source local.env`** (export `AWS_PROFILE`, `AWS_REGION`, `AWS_SECRET_NAME`, channel env vars, etc.), then **`mvn -q package`** and **`exec java -jar target/discord-bot-1.0.0.jar`**. |
| **`bash scripts/run-bot.sh`** | Same idea for a server (e.g. EC2): ensures Java 21 + Maven if missing, sets default **`AWS_REGION`** / **`AWS_SECRET_NAME`** if unset, packages, runs the same JAR. |
| **Manual** | From repo root: `mvn -q package && java -jar target/discord-bot-1.0.0.jar` with AWS credentials available to the SDK (env, profile, instance role). |

In all cases the **entry point** is **`BotBootstrap.main`**: the JVM loads the shaded JAR and starts that class.

### 3. Configuration pipeline (before JDA connects)

When **`BotConfiguration.load()`** runs inside **`main`**:

1. **Region and secret id:** **`AWS_REGION`** (default `us-east-1`) and **`AWS_SECRET_NAME`** (default `220_Discord_Token`).
2. **AWS Secrets Manager:** the AWS SDK uses the **default credential chain** (environment variables, `~/.aws/credentials` / `AWS_PROFILE`, EC2 instance profile, etc.) and fetches the secret string.
3. **Parse secret:** the string is either the **token alone** (plain text) or **JSON** with a token field (`DISCORD_TOKEN`, `discord_token`, `DISCORD_BOT_TOKEN`, `token`) and optional channel name fields (`DISCORD_CHANNEL_NAME`, `CHANNEL_NAME`, `channel_name`, `channel`).
4. **Merge environment:** if **`DISCORD_CHANNEL_NAME`** or **`CHANNEL_NAME`** is set in the environment, that **wins** over the channel name from the secret. There is **no** user-facing channel **id** in configuration—only an optional **name**. If no name is set anywhere, stderr warns you; the bot can still run with **no channel filter**.

Output of this stage is an immutable **`BotConfiguration(discordToken, configuredChannelName)`**.

### 4. In-process pipeline (JVM → Discord events)

1. **Plug-in discovery:** **`ServiceLoader.load(DiscordBotPlugin.class)`** reads every implementation listed under **`META-INF/services/...DiscordBotPlugin`**. If **none** are found, **`MinimalDeploymentPlugin`** is registered as a fallback.
2. **After-ready hooks:** for each plug-in, **`afterChannelScopeReady`** is collected into a list passed to **`ChannelScope`**.
3. **Gateway intents:** base set **`GUILD_MESSAGES`** + **`MESSAGE_CONTENT`**; each plug-in can add more via **`extraIntents()`**.
4. **`JDABuilder`:** token from config, intents enabled, **`ChannelScope`** registered, then every object from each **`createListeners(scope, config)`** (usually **`ListenerAdapter`** instances).
5. **`build().awaitReady()`:** opens the Discord gateway and blocks until the session is **Ready**.
6. **`ChannelScope.onReady`:** if a channel **name** was configured, JDA **`getTextChannelsByName`** runs; the **first** match sets an **internal** channel id used only inside the app (not something you configure). If no name was configured, the scope stays **unbound**. Then every **`afterChannelScopeReady(jda, scope)`** runs (good for one-shot “bot online” messages).
7. **Steady state:** Discord delivers events to **`ChannelScope`** and to each plug-in listener. Typical pattern: in **`MessageReceived`**, ignore bots; if you only want the configured channel, check **`scope.isBound()`** and **`scope.isTargetChannel(channel)`**; use **`scope.activeTextChannel(jda)`** when you need to send to that channel by resolving the stored id through JDA.

**Discord Developer Portal:** enable **Message Content Intent** for the application, or message-content-based commands will not work reliably.

---

## Your bot logic (plug-in SPI)

Implement **`edu.moravian.csci220.discordbot.spi.DiscordBotPlugin`**:

- **`afterChannelScopeReady(JDA, ChannelScope)`** — optional; runs once after **`ChannelScope`** finishes **`Ready`** handling (channel name resolved or left unbound).
- **`createListeners(ChannelScope, BotConfiguration)`** — return JDA listener objects registered on the builder.
- **`extraIntents()`** — optional extra **`GatewayIntent`** values.

Register one **fully qualified class name per line** in:

`src/main/resources/META-INF/services/edu.moravian.csci220.discordbot.spi.DiscordBotPlugin`

- **Replace** the sample: remove **`MinimalDeploymentPlugin`** and list only your class.
- **Stack** plug-ins: add more lines (each class loads).
- **Empty / missing** service file: bootstrap falls back to **`MinimalDeploymentPlugin`**.

---

## Quick commands

```bash
# Local (optional: cp local.env.example local.env and edit)
bash scripts/local-deploy.sh
```

```bash
# Server-style (e.g. EC2)
bash scripts/run-bot.sh
```

```bash
# Manual
mvn -q package && java -jar target/discord-bot-1.0.0.jar
```

Copy **`local.env.example`** to **`local.env`** for AWS profile, region, secret name, and optional **`CHANNEL_NAME`** / **`DISCORD_CHANNEL_NAME`**.
