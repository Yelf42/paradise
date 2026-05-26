package com.yelf42.paradise.registry;

import net.minecraft.world.level.block.DispenserBlock;

import java.util.ArrayList;
import java.util.List;

public class ModDispenserBehaviours {
    public static final List<Runnable> DISPENSER_BEHAVIORS = new ArrayList<>();

    public static void registerDispenserBehavior() {
        DISPENSER_BEHAVIORS.add(() ->
                DispenserBlock.registerProjectileBehavior(ModItems.DIGITAL_ARROW)
        );


        DISPENSER_BEHAVIORS.forEach(Runnable::run);
    }

}
