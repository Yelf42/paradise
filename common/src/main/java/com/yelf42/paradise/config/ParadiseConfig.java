package com.yelf42.paradise.config;

import folk.sisby.kaleido.api.WrappedConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.ChangeWarning;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.IntegerRange;

public class ParadiseConfig extends WrappedConfig {

    @Comment("Are intrusions via Scramblers allowed?")
    @Comment("Note this only affects future intrusions")
    public boolean intrusionsAllowed = true;

    @Comment("")
    @Comment("-------------------------------")
    @Comment("")
    @Comment("How many days until the whitelist forgets a player?")
    @Comment("-1 will effectively disable whitelisting")
    @IntegerRange(min=-1, max=Integer.MAX_VALUE)
    public int whitelistDuration = 3;

    @Comment("")
    @Comment("-------------------------------")
    @Comment("")
    @Comment("What percentage of Paradise dimensions should be night?")
    @Comment("Note this will only affect DataServers that haven't been generated yet")
    @IntegerRange(min=0, max=100)
    public int nightChance = 25;

    @Comment("")
    @Comment("-------------------------------")
    @Comment("")
    @Comment("What should the border size be?")
    @Comment("Note this won't make the island bigger, just the buildable area")
    @Comment("The value is the side length of the square border")
    @Comment("WARNING: Requires restarting the server")
    @Comment("WARNING: Does not move entities back in if border shrinks")
    @ChangeWarning(folk.sisby.kaleido.lib.quiltconfig.api.metadata.ChangeWarning.Type.RequiresRestart)
    @IntegerRange(min=256, max=2048)
    public int borderSize = 256;

    @Comment("")
    @Comment("-------------------------------")
    @Comment("")
    @Comment("Should dying in Paradise respawn you in Paradise?")
    @Comment("This does not include NULLSPACE or intruders")
    public boolean safeRespawn = true;

    @Comment("")
    @Comment("-------------------------------")
    @Comment("")
    @Comment("How many bunkers should generate in total?")
    @Comment("WARNING: More bunkers means more dimensions to store")
    @Comment("WARNING: Requires restarting the server")
    @Comment("WARNING: Changing post-generation may cause issues (including use of /locate)")
    @ChangeWarning(folk.sisby.kaleido.lib.quiltconfig.api.metadata.ChangeWarning.Type.RequiresRestart)
    @IntegerRange(min=1, max=4095)
    public int bunkerCount = 48;

    @Comment("")
    @Comment("-------------------------------")
    @Comment("")
    @Comment("How far apart should rings for bunker generation be?")
    @Comment("Note this is quite weird and complex, see https://minecraft.wiki/w/Structure_set#Placement_types")
    @Comment("WARNING: Requires restarting the server")
    @Comment("WARNING: Changing post-generation may cause issues (including use of /locate)")
    @ChangeWarning(folk.sisby.kaleido.lib.quiltconfig.api.metadata.ChangeWarning.Type.RequiresRestart)
    @IntegerRange(min=0, max=1023)
    public int bunkerDistance = 8;

    @Comment("")
    @Comment("-------------------------------")
    @Comment("")
    @Comment("How many bunkers should generate on the first ring?")
    @Comment("Note this is quite weird and complex, see https://minecraft.wiki/w/Structure_set#Placement_types")
    @Comment("WARNING: Requires restarting the server")
    @Comment("WARNING: Changing post-generation may cause issues (including use of /locate)")
    @ChangeWarning(folk.sisby.kaleido.lib.quiltconfig.api.metadata.ChangeWarning.Type.RequiresRestart)
    @IntegerRange(min=0, max=1023)
    public int bunkerSpread = 4;
}
