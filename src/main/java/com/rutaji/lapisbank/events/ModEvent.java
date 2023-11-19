package com.rutaji.lapisbank.events;

import com.rutaji.lapisbank.LapisBank;
import com.rutaji.lapisbank.commands.PrintCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.ConfigCommand;

@Mod.EventBusSubscriber(modid = LapisBank.MOD_ID)
public class ModEvent {

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event)
    {
        new PrintCommand(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public  static void onPLayerCloneEvent(PlayerEvent.Clone event)
    {
        if(!event.getOriginal().getEntityWorld().isRemote())
        {

            event.getPlayer().getPersistentData().putInt(PrintCommand.PLayerKey,event.getOriginal().getPersistentData().getInt(PrintCommand.PLayerKey));
        }
    }
}
