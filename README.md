# Lastkatka Bot

## Useful multi-feature bot for telegram

## Features

### For users

- The "Bulls and Cows" game
- Pair of the day, marriages
- Roleplay, duels, and cakegiving (try `/cake` command to figure out what is it)
- Genshin pull simulator (`/wish`, `/inv`)
- `/f` implementation
- Advanced `/weather` using `wttr.in` service
- Sticker with text generation on new group member
- Various group settings such as forbidden commands, custom greeting stickers and more
- Localization (currently we only support Russian and Ukrainian)

### For admins

- Simple bootstrap
- Prometheus metrics (available on `/prometheus` endpoint) and advanced error logging (directly to your telegram group or channel, with logs, caused JSON and everything you need!)
- Automatic database cleanup - forget about inactive users
- Advanced feedback system - receive feedback from you users, answer anonymously, track actions
- Blacklist for users and groups
- Admin management
- Tracking (Know in what groups are your users in)
- `/broadcast` system
- Low memory consumption - we had this bot running with 140000 users and 2k messages / sec on `-Xmx128M` for the whole month!
- Cross-platform (it's written in java)
- The `/help` command shows only those commands that the user (standard user/admin/main admin) can run

## Requirements

Java development kit 21+

## Build

First, you need to build the jar entering the following commands:

- MacOS/Linux: `./gradlew shadowJar`
- Windows: `gradlew.bat shadowJar`

The "fat jar" with all dependencies will be stored in `/build/libs` directory

## Configuration

Configuration is done by setting environment variables.

Look for `bot` and `datasources.default` section in the [application.yml](src/main/resources/application.yml) file to
get info about environment variables you need

Also look at the permitted variables list in
the [Main Class](src/main/java/com/senderman/lastkatkabot/LastkatkaBot.java)
and [Micronaut configuration reference](https://docs.micronaut.io/latest/guide/configurationreference.html)

- To disable banner, run with `-DdisableBanner`
- To print logs in json format (useful for log collecting tools like ELK/Loki), run with `-DjsonLogs`

### Database

By default, bot connects to Postgresql database named `lastkatkabot` running on localhost:5432

You can set the database address by changing `DBHOST`, `DBPORT` and `DBNAME` environment variables.

For database authorization, use `DBUSER` and `DBPASS` environment variables.
Default login:password is `lastkatkabot:sa`

## Run

`java -jar build/libs/lastkatkabot-version.jar`

Logs are redirected to stdin and stderr

## We use:

- [aNNiMON's tgbots-module](https://github.com/aNNiMON/tgbots-module) official telegram library wrapper
- [Micronaut framework](https://micronaut.io) for DI, IoC and ORM
- [Logback classic](https://logback.qos.ch) for logging
