# Paradise
Paradise is a pocket dimension mod lightly inspired by Dimensional Doors and Shimeji Simulation.
It adds quaint digital worlds perfect for building in, 
and to act as a teleportation hub to get to different parts of your world. There are also a number of unique decorative 
and functional blocks and items that add flavour and utility to the experience.

The mod also adds a complex PvP element through a Whitelist and Intrusion system. Each pocket dimension has a whitelist
that can be edited by those on the whitelist (or anyone if the whitelist is empty). Player who aren't whitelisted won't
be able to teleport into the dimension through normal means<sup>1</sup>. However, by using a DataScrambler a player can 
hack their way in and when they do so, they become a pixelated Intruder and trigger the dimensions defense mechanism, the 
**Watcher**. If at any time the intruder lets go of the DataScrambler, the system will lock onto them and eject them
from the dimension in 20 seconds

<sup>1</sup> Teleportation into a paradise dimension via other means (such as Waystones) cannot block unwhitelisted
players, but will auto inflict the EJECTION effect on intruders

## Commands
These commands all require OP permission 2 or higher, and are intended for server admin use.

`/paradiseTransitLog <dimension>` </br>
This command will output the TransitLog for the given dimension, in the server log or the chat. </br>
The data is identical to what can be seen in a DigitalTransitRecord block. </br>

`/paradiseIntruders <dimension> [addPlayer]` </br>
By default, this command will list the players in the given dimension. </br>
If they are online, it will give their username, and if they are offline it will give their UUID. </br>
If you add a player to the end of the command, it will add that player to the dimensions intruder list 
(this is for testing purposes)

`/paradiseRemoveDimension <dimension> <replaceDataServer?>` </br>
Deletes the specified dimension and corresponding Whitelist data </br>
If `replaceDataServer?` is true, a new DataServer will be placed where
the original dimensions DataServer was, and will generate a new dimension.
This does nothing for dimensions that lack a DataServer

`/paradiseGenDataServer <dataServerLocation>` </br>
Places a DataServer block at the specified location, and prints the dimension name created </br>
(This is pretty useless as you can just place the block in-game.
Holding shift when placing will set the DataServer's dimension to NULLSPACE)

`/paradiseGenDimension <DAY|NIGHT>` </br>
Creates a new dimension without a DataServer, of the specified type

`/paradiseGenDisc <dimension>` </br>
Has to be run by a player. </br>
Gives the player an AccessDisc linked to the specified dimension

`/paradiseTp <dimension> [location]` </br>
Teleports the player to the DataServer corresponding to the dimension. </br>
If a location is provided, teleports the player to that location within the specified dimension

`/paradiseWhitelists (list|add|remove|flip|check) <dimension> [player]` </br>
**List:** </br>
Lists the active and history whitelisted player names for the given dimension

**Add:** </br>
Requires a player </br>
Adds the player to the active list </br>
Removes the player from the history list if they were present </br>
Removes the player from the intruder list if they were present </br>

**Remove:** </br>
Requires a player </br>
Removes the player from either list if present </br>

**Flip:** </br>
Requires a player </br>
Moves the player to the opposite list if they were in either </br>
Removes the player from the intruder list if they are now in the active list </br>

**Check:** </br>
Requires a player </br>
Outputs whether the input player is whitelisted in the input dimension </br>
