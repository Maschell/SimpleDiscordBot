# Simple DiscordBot
A simple Discord Bot with very basic features.

# Features
- Log deleted messages
- Log when someone joins/leaves the server
- Create custom commands to get roles

# Building
`mvn clean package`

# Usage
When started for the first time, the bot will ask you for the settings (bot token, channel ids etc.).  
If you are not sure about these parameters, only fill in the bot token and comment the server, channel and role logging in `Starter.java`. Either modify the properties files manually or delete it to create a new one.

Use the command `!adminhelp` in the bot_config_channel to get more information on how use the role feature.

# Used libraries:
- [Javacord](https://javacord.org/)
- [sdcf4j](https://github.com/Bastian/sdcf4j)
- [lombok](https://projectlombok.org/)
- [javanna](https://github.com/renatoathaydes/javanna)
- [sqlite-jdbc](https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc)