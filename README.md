# Paradise
Paradise is a pocket dimension mod lightly inspired by Dimensional Doors and Shimeji Simulation. It adds quaint digital worlds perfect for building in, and to act as a teleportation hub to get to different parts of your world. There are also a number of unique decorative and functional blocks and items that add flavour and utility to the experience.

The mod also adds a complex PvP element through a Whitelist and Intrusion system. Each pocket dimension has a whitelist that can be edited by those on the whitelist (or anyone if the whitelist is empty). Players who aren't whitelisted won't be able to teleport into the dimension through normal means<sup>1</sup>. However, by using a DataScrambler a player can hack their way in and when they do so, they become a pixelated Intruder and trigger the dimension's defense mechanism, the **Watcher**. If at any time the intruder lets go of the DataScrambler, the system will lock onto them and eject them from the dimension in 20 seconds.

<sup>1</sup> Teleportation into a paradise dimension via other means (such as Waystones) cannot block unwhitelisted players, but will auto inflict the EJECTION effect on intruders.

## Commands
These commands all require OP permission 2 or higher, and are intended for server admin use.

` /paradiseTransitLog <dimension> `  
This command will output the TransitLog for the given dimension, in the server log or the chat.  
The data is identical to what can be seen in a DigitalTransitRecord block.

` /paradiseIntruders <dimension> [addPlayer] `  
By default, this command will list the players in the given dimension.  
If they are online, it will give their username, and if they are offline it will give their UUID.  
If you add a player to the end of the command, it will add that player to the dimension's intruder list (this is for testing purposes).

` /paradiseRemoveDimension <dimension> <replaceDataServer?> `  
Deletes the specified dimension and corresponding Whitelist data.  
If `replaceDataServer?` is true, a new DataServer will be placed where the original dimension's DataServer was, and will generate a new dimension.  
This does nothing for dimensions that lack a DataServer.

` /paradiseGenDataServer <dataServerLocation> `  
Places a DataServer block at the specified location, and prints the dimension name created.  
(This is pretty useless as you can just place the block in-game. Holding shift when placing will set the DataServer's dimension to NULLSPACE).

` /paradiseGenDimension <DAY|NIGHT> `  
Creates a new dimension without a DataServer, of the specified type.

` /paradiseGenDisc <dimension> `  
Has to be run by a player.  
Gives the player an AccessDisc linked to the specified dimension.

` /paradiseTp <dimension> [location] `  
Teleports the player to the DataServer corresponding to the dimension.  
If a location is provided, teleports the player to that location within the specified dimension.

` /paradiseWhitelists (list|add|remove|flip|check) <dimension> [player] `

* **List:** Lists the active and history whitelisted player names for the given dimension.
* **Add:** Requires a player. Adds the player to the active list, removes them from the history list if present, and removes them from the intruder list if present.
* **Remove:** Requires a player. Removes the player from either list if present.
* **Flip:** Requires a player. Moves the player to the opposite list if they were in either, and removes them from the intruder list if they are now in the active list.
* **Check:** Requires a player. Outputs whether the input player is whitelisted in the input dimension.

## TODO one day
* Maybe give the DigitalVolume texture some flare
* Raids of intruders if your dim is whitelisted
* Annoying wandering trader if your dim isn't whitelisted
* Locked dims that require using the DataScrambler to enter (maybe timed, dim gets removed afterwards?)
* Tunnel to access dungeons dims, but longer access increases intruder chance
* Trojan horse enemy, like skeleton horse
