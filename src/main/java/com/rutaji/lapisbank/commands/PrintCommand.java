package com.rutaji.lapisbank.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.rutaji.lapisbank.LapisBank;
import com.rutaji.lapisbank.data.GlobalBank;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerEvent;

import javax.annotation.Nonnull;
import java.util.*;

public class PrintCommand {
    private GlobalBank Bank;
    public static final String PLayerKey = LapisBank.MOD_ID + "privateBank";
    public PrintCommand(CommandDispatcher<CommandSource> dispatcher){
        dispatcher.register(Commands.literal("lapisbank").then(Commands.literal("print").executes((command) ->{return GlobalPrint(command.getSource());})));
        dispatcher.register(Commands.literal("lapisbank").then(Commands.literal("add").executes((command) ->{return GlobalAdd(command.getSource());})));
        dispatcher.register(Commands.literal("lapisbank").then(Commands.literal("get").executes((command) ->{return GlobalGet(command.getSource());})));
        dispatcher.register(Commands.literal("lapisbank").then(Commands.literal("help").executes((command) ->{return Help(command.getSource());})));
        dispatcher.register(Commands.literal("lapisbank").then(Commands.literal("leaderboard").executes((command) ->{return LeaderBoard(command.getSource());})));
        dispatcher.register(Commands.literal("lapisbank").then(Commands.literal("add").then(Commands.argument("amount", IntegerArgumentType.integer(0)).executes((command) ->{return GlobalAddArgument(command);}))));
        dispatcher.register(Commands.literal("lapisbank").then(Commands.literal("get").then(Commands.argument("amount", IntegerArgumentType.integer(0,1000)).executes((command) ->{return GlobalGetArgument(command);}))));

        dispatcher.register(Commands.literal("lapisbank").then(Commands.literal("private").then(Commands.literal("print").executes((command) ->{return PrivatePrint(command.getSource());}))));
        dispatcher.register(Commands.literal("lapisbank").then(Commands.literal("private").then(Commands.literal("get").executes((command) ->{return PrivateGet(command.getSource());}))));
        dispatcher.register(Commands.literal("lapisbank").then(Commands.literal("private").then(Commands.literal("get").then(Commands.argument("amount",IntegerArgumentType.integer(0,1000)).executes((command) ->{return PrivateGetArgument(command);})))));
        dispatcher.register(Commands.literal("lapisbank").then(Commands.literal("private").then(Commands.literal("add").executes((command) ->{return PrivateAdd(command.getSource());}))));
        dispatcher.register(Commands.literal("lapisbank").then(Commands.literal("private").then(Commands.literal("add").then(Commands.argument("amount",IntegerArgumentType.integer(0)).executes((command) ->{return PrivateAddArgument(command);})))));
    }

    private int GlobalGetArgument(CommandContext<CommandSource> context) throws CommandSyntaxException {
        int  input = IntegerArgumentType.getInteger(context, "amount");
        CommandSource source = ((CommandSource)context.getSource());
        ServerPlayerEntity player = source.asPlayer();
        int sum =  GetBank(source.getWorld()).get() < input ? Bank.get() : input;

        GiveToPlayer(player,sum,source.getWorld());
        GetBank(source.getWorld()).ChangeLeaderboard(player.getUniqueID(),sum*-1);
        Bank.add(sum * -1);
        return 1;
    }

    private int PrivateAddArgument(CommandContext<CommandSource> context) throws CommandSyntaxException {
        int input = IntegerArgumentType.getInteger(context, "amount");
        ServerPlayerEntity player = ((CommandSource)context.getSource()).asPlayer();
        int sum = TakeAllLapis(player,input);
        AddPrivate(player,sum);
        return 1;
    }

    private int PrivateGetArgument(CommandContext context) throws CommandSyntaxException
    {
        int  input = IntegerArgumentType.getInteger(context, "amount");
        CommandSource source = ((CommandSource)context.getSource());
        ServerPlayerEntity player = source.asPlayer();
        int sum = Math.min(GetPrivate(player), input);

        GiveToPlayer(player,sum,source.getWorld());
        AddPrivate(player,sum * -1);
        return 1;
    }
    private void GiveToPlayer(ServerPlayerEntity player, int sum, World world)
    {
        for (int i = 0; i < player.inventory.mainInventory.size();i++)
        {
            ItemStack stack = player.inventory.mainInventory.get(i);
            if(stack.getItem() == Items.LAPIS_LAZULI && stack.getCount() < 64)
            {
                int toAdd = Math.min( 64 - stack.getCount(),sum );
                stack.setCount(stack.getCount() + toAdd);
                sum -= toAdd;
                if(sum == 0){return;}
            }
        }
        while(sum != 0)
        {
            int toAdd = Math.min( 64 ,sum );
            sum -= toAdd;
            ItemEntity itemEntity = new ItemEntity(world, player.getPosX(), player.getPosY(), player.getPosZ(), new ItemStack(Items.LAPIS_LAZULI,toAdd));
            world.addEntity(itemEntity);
        }
    }


    private int TakeAllLapis(ServerPlayerEntity player,int max)
    {
        int sum = 0;
        NonNullList<ItemStack> inv = player.inventory.mainInventory;
        for(int i = 0 ; i < inv.size();i++)
        {
            if(inv.get(i).getItem() == Items.LAPIS_LAZULI)
            {
                int remaining = max - sum;
                if(inv.get(i).getCount() > remaining)
                {
                    inv.get(i).setCount(inv.get(i).getCount() - remaining );
                    sum += remaining;
                    return sum;
                }
                sum += inv.get(i).getCount();
                inv.set(i,ItemStack.EMPTY);


            }
        }
        inv = player.inventory.offHandInventory;
        for(int i = 0 ; i < inv.size();i++)
        {
            if(inv.get(i).getItem() == Items.LAPIS_LAZULI)
            {
                int remaining = max - sum;
                if(inv.get(i).getCount() > remaining)
                {
                    inv.get(i).setCount(inv.get(i).getCount() - remaining );
                    sum += remaining;
                    return sum;
                }
                sum += inv.get(i).getCount();
                inv.set(i,ItemStack.EMPTY);


            }
        }

        return sum;
    }
    private int GlobalAddArgument(CommandContext context) throws CommandSyntaxException {
        int  input= IntegerArgumentType.getInteger(context, "amount");
        ServerPlayerEntity player = ((CommandSource)context.getSource()).asPlayer();
        int sum = TakeAllLapis(player,input);
        GetBank(((CommandSource)context.getSource()).getWorld()).ChangeLeaderboard(player.getUniqueID(),sum);
        Bank.add(sum);

        return 1;
    }

    private int GetPrivate(ServerPlayerEntity player)
    {
        return player.getPersistentData().getInt(PLayerKey);
    }
    private void AddPrivate(ServerPlayerEntity player,int value)
    {
        int previus =player.getPersistentData().getInt(PLayerKey);
        player.getPersistentData().putInt(PLayerKey,value + previus);
    }


    private int PrivatePrint(CommandSource source) throws CommandSyntaxException {

        ServerPlayerEntity player = source.asPlayer();
        int value = GetPrivate(player);
        Entity entity = source.getEntity();
        entity.sendMessage( new StringTextComponent(String.valueOf(value)),entity.getUniqueID());
        return 1;

    }
    private int PrivateAdd(CommandSource source) throws CommandSyntaxException {

        ServerPlayerEntity player = source.asPlayer();
        AddPrivate(player, TakeAllLapis(player));
        return 1;

    }
    private int PrivateGet(CommandSource source) throws CommandSyntaxException {

        ServerPlayerEntity player = source.asPlayer();
        int sum =  GetPrivate(player) < 64 ? GetPrivate(player) : 64;
        source.asPlayer().inventory.addItemStackToInventory(new ItemStack(Items.LAPIS_LAZULI,sum));
        AddPrivate(player,sum * -1);
        return 1;

    }


    private int Help(CommandSource source)
    {
        Entity entity = source.getEntity();
        IFormattableTextComponent message =  new StringTextComponent("add ").mergeStyle(TextFormatting.DARK_RED)
                        .appendSibling(new StringTextComponent("adds lapis to bank from your inventory\n").mergeStyle(TextFormatting.WHITE))
                        .appendSibling(new StringTextComponent("get ").mergeStyle(TextFormatting.DARK_RED))
                        .appendSibling(new StringTextComponent("gets lapis from bank to your inventory\n").mergeStyle(TextFormatting.WHITE))
                        .appendSibling(new StringTextComponent("print ").mergeStyle(TextFormatting.DARK_RED))
                        .appendSibling(new StringTextComponent("prints how much lapis is in the bank\n").mergeStyle(TextFormatting.WHITE))
                        .appendSibling(new StringTextComponent("private ").mergeStyle(TextFormatting.DARK_RED))
                        .appendSibling(new StringTextComponent("if you don't want to store your lapis in public bank that's open for all players, you can use command private to access your own bank\n").mergeStyle(TextFormatting.WHITE))
                        .appendSibling(new StringTextComponent("leaderboard ").mergeStyle(TextFormatting.DARK_RED))
                        .appendSibling(new StringTextComponent("shows, how much players put inside the global bank\n").mergeStyle(TextFormatting.WHITE));;

        entity.sendMessage(message,entity.getUniqueID());
        return 1;
    }

    private GlobalBank GetBank(ServerWorld s)
    {
        if(Bank == null){Bank = GlobalBank.get(s);}
        return Bank;

    }
    private int GlobalPrint(CommandSource source) throws CommandSyntaxException
    {

        int value = GetBank(source.getWorld()).get();
        Entity entity = source.getEntity();
        entity.sendMessage( new StringTextComponent(String.valueOf(value)),entity.getUniqueID());
        return 1;
    }
    private int TakeAllLapis(ServerPlayerEntity player)
    {
        int sum = 0;
        NonNullList<ItemStack> inv = player.inventory.mainInventory;
        for(int i = 0 ; i < inv.size();i++)
        {
            if(inv.get(i).getItem() == Items.LAPIS_LAZULI)
            {
                sum += inv.get(i).getCount();
                inv.set(i,ItemStack.EMPTY);


            }
        }
         inv = player.inventory.offHandInventory;
        for(int i = 0 ; i < inv.size();i++)
        {
            if(inv.get(i).getItem() == Items.LAPIS_LAZULI)
            {
                sum += inv.get(i).getCount();
                inv.set(i,ItemStack.EMPTY);


            }
        }

        return sum;
    }
    private int LeaderBoard(CommandSource source)
    {
        ServerWorld world = source.getWorld();
        StringBuilder builder = new StringBuilder();
        for ( Map.Entry<UUID, Integer> entry : GetBank(world).getLeaderBoard().entrySet() ) {
            ITextComponent name = world.getPlayerByUuid(entry.getKey()).getDisplayName();
            builder.append("\n" + name.getString() + "   " + String.valueOf(entry.getValue()) );
        }
        Entity entity = source.getEntity();
        entity.sendMessage( new StringTextComponent(builder.toString()),entity.getUniqueID());
        return 1;
    }

    private int GlobalAdd(CommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.asPlayer();
        int sum = TakeAllLapis(player);

        GetBank(source.getWorld()).ChangeLeaderboard(player.getUniqueID(),sum);
        Bank.add(sum);
        return 1;
    }
    private int GlobalGet(CommandSource source) throws CommandSyntaxException {
        GetBank(source.getWorld());
        int sum =  Bank.get() < 64 ? Bank.get() : 64;
        ServerPlayerEntity player = source.asPlayer();
        player.inventory.addItemStackToInventory(new ItemStack(Items.LAPIS_LAZULI,sum));
        Bank.ChangeLeaderboard(player.getUniqueID(),sum*-1);
        Bank.remove(sum);
        return 1;
    }
}
