package com.rutaji.lapisbank.data;

import net.minecraft.command.CommandSource;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GlobalBank extends WorldSavedData {

    private static final String DATA_NAME = "LapisBank";
    private int Value = 0;


    public GlobalBank() {
        super(DATA_NAME);
    }


    public static GlobalBank get(ServerWorld world) {
        return world.getSavedData().getOrCreate(GlobalBank::new,DATA_NAME);
    }

    public int get()
    {

        return Value;
    }
    public void add(int a){
        Value +=a;
        markDirty();
    }

    public Map<UUID, Integer> getLeaderBoard() {
        markDirty();
        return LeaderBoard;
    }

    private Map<UUID,Integer> LeaderBoard = new HashMap<UUID,Integer>();
    public void ChangeLeaderboard(UUID uuid, int add)
    {
        if(!LeaderBoard.containsKey(uuid))
        {
            LeaderBoard.put(uuid,0);
        }
        LeaderBoard.put(uuid,LeaderBoard.get(uuid) + add);
    }

    @Override
    public void read(CompoundNBT nbt)
    {

        int[] array = nbt.getIntArray("leaderboard");
        for(int i = 0 ; i< array.length;i++)
        {
            LeaderBoard.put(nbt.getUniqueId(String.valueOf(i)),array[i]);
        }
        Value = nbt.getInt("value");
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt)
    {

        int[] values = new int[getLeaderBoard().size()];
        int i = 0;

        for ( Map.Entry<UUID, Integer> entry :getLeaderBoard().entrySet() )
        {
            values[i] = entry.getValue();
            nbt.putUniqueId(String.valueOf(i),entry.getKey());
            i++;
        }

        nbt.putIntArray("leaderboard",values);
        nbt.putInt("value", Value);
        return nbt;
    }

    public void remove(int i) {
        Value -= i;
    }
}