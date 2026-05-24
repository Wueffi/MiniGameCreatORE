![Static Badge](https://img.shields.io/badge/Version-1.0.0_SNAPSHOT-blue)
# MiniGameCreatORE

**MiniGameCore** is a paper plugin that allows you to create MiniGames for [MiniGameCore](https://github.com/Wueffi/MiniGameCore) while still ingame!

---

## 🔧 Installation

1. Download the latest `.jar` from [Modrinth](https://modrinth.com/plugin/minigamemaker) or Releases.
2. Place the file in the `plugins/` folder of your Minecraft server.
3. For permission-management, you can optionally use a plugin like [LuckPerms](https://luckperms.net/).
4. Restart the server once.

---
## 📜 Commands & Permissions

| Command                                                                | Description                                             | Permission          |
|------------------------------------------------------------------------|---------------------------------------------------------|---------------------|
| `/mgcreator create <gameName> <maxPlayers>`                            | Creates a new Draft                                     | `mgcreator.create`  |
| `/mgcreator resume`                                                    | Resume editing your Draft                               | `mgcreator.create`  |
| `/mgcreator editconfig <option> <value>`                               | Edit the config of your game                            | `mgcreator.create`  |
| `/mgcreator showconfig`                                                | Display the current config in chat                      | `mgcreator.create`  |
| `/mgcreator spawnpoint <add\|remove spawnPointName>`                   | Adds and removes single-player spawnpoints to your game | `mgcreator.create`  |
| `/mgcreator teamspawnpoint <add teamID\|remove teamID spawnPointName>` | Adds and removes team spawnpoints to your game          | `mgcreator.create`  |
| `/mgcreator delete`                                                    | Deletes your current draft                              | `mgcreator.create`  |
| `/mgcreator publish <user>`                                            | Publishes the game inside of the MGC Plugin             | `mgcreator.publish` |
