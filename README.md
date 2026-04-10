# Discord bot (CSCI 220)

Java 21 + Maven. Loads the token from **AWS Secrets Manager** (`DefaultCredentialsProvider`: CLI profile, env vars, or EC2 role). Optional env: `AWS_REGION` (default `us-east-1`), `AWS_SECRET_NAME` (default `220_Discord_Token`). Secret: plain text token, or JSON with `DISCORD_TOKEN` / `token`.

**Local machine (test before push)** — same AWS flow as production; `local.env` only sets things like `AWS_PROFILE` / region / secret name (not the token).

```bash
cp local.env.example local.env   # uncomment and set as needed
bash scripts/local-deploy.sh
```

**EC2 / Linux**

```bash
bash scripts/run-bot.sh
```

Or: `mvn -q -DskipTests package && java -jar target/discord-bot-1.0.0.jar`
