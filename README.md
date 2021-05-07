# Minecraft Tic Tac Toe
A Tic Tac Toe plugin for Minecraft Spigot using particles.

![2021-05-04_19 33 21](https://user-images.githubusercontent.com/28844450/117198781-1ae77780-adf2-11eb-99ff-50e7b2d0039c.png)

## Features
- Win detection
- You can play with animals and villagers with "super advanced AI" !!
- Automatically resets the board if you walk too far or if a player quits
- You can set rewards in the config file. Examples included
- Players can change their particles to anything you've listed
- You can customize the shape for each player
- You can change/translate nearly all chat messages
- Support for Essentials
- Sounds!

## Commands
- `/ttt play` - gives you a stick you use to play
- `/ttt sounds on/off` - as you might've guessed, turns sounds on and off
- `/ttt challenge player <player> <betAmount>` - this is how you challenge other players with a bet
- `/ttt challenge cancel <player>` - cancel challenge for that specific player
- `/ttt challenge accept <player>` - accept challenge for that specific player
- `/ttt particles change <particleType>` - can be used to change your particles
- `/ttt particles reset [player]` - resets your own particles back to normal. Admins can reset other players

## Permissions
- `ttt.play` - required to play and spawn the stick
- `ttt.play.nocooldown` - used to bypass the cooldown for getting the stick
- `ttt.particles.change` - lets players change their particles if enabled in the config files too
- `ttt.particles.reset` - lets players reset their particles
- `ttt.particles.reset.admin` - lets admins reset other players

## Soft dependency
- Essentials - Betting support.

## Known issues
- None that i know of

## How to play
Once you have a StickTacToe (/ttt play), just right click a player or entities that have been listed in the config file and you're golden! Default entities include villagers and most of the animals. Walking away from the board or quitting counts as forfeit and you lose the game and bet.
