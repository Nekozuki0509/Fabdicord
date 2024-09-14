[![Modrinth Version](https://img.shields.io/modrinth/v/JLjSjB3e?logo=modrinth&color=1bd768)![Modrinth Downloads](https://img.shields.io/modrinth/dt/JLjSjB3e?logo=modrinth&color=1bd768)![Modrinth Game Versions](https://img.shields.io/modrinth/game-versions/JLjSjB3e?logo=modrinth&color=1bd768)](https://modrinth.com/mod/fabdicord)
[![Discord](https://img.shields.io/discord/1241236305741090836?logo=discord&color=5765f2)](https://discord.gg/352Cdy8MjV)
[![Static Badge](https://img.shields.io/badge/litlink-Nekozuki0509-9594f9)](https://lit.link/nekozuki0509)

[![Static Badge](https://img.shields.io/badge/github-Velodicord-blue?logo=github)](https://github.com/Nekozuki0509/Velodicord)
[![Static Badge](https://img.shields.io/badge/github-V4S4J-blue?logo=github)](https://github.com/Nekozuki0509/V4S4J)

# Fabdicord
## 説明
[Velodicord](https://modrinth.com/plugin/velodicord)のfabricサーバー側のアドオン。
## 使い方
1. fabricサーバーのmodsフォルダにこのmodを入れて再起動
1. config/Fabdicord/fabdicordconfig.jsonを編集
1. 楽しみましょう!
## configファイル
```
{
  "_Scomment_" : "サーバー名",
  "ServerName" : "s",
  "_bcomment_" : "discordbotのtoken",
  "BotToken" : "aaaaaa",
  "_Pcomment_" : "discordのプラグインメッセージチャンネルID",
  "PMChannelID" : "000000",
  "_lcomment_" : "マイクラサーバーのログチャンネルID(自動設定)",
  "LogChannelID" : "000000",
  "_ncomment_" : "discordの入退出などの通知チャンネルID(自動設定)",
  "NoticeChannelID" : "000000",
  "_cccomment_" : "discordのコマンドチャンネルID(自動設定)",
  "CommandChannelID" : "000000",
  "_crcomment_" : "管理者コマンドを実行できるロールID(自動設定)",
  "CommandRoleID" : "aaaaaa"
}
```
