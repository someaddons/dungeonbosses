package com.brutalbosses.entity;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class PosUtil
{
    public static Direction getFacing(final Vec3 from, final Vec3 to)
    {
        final Vec3 vector = to.subtract(from);

        if (Math.abs(vector.x()) > Math.abs(vector.z()))
        {
            return vector.x() > 0 ? Direction.EAST : Direction.WEST;
        }
        else
        {
            return vector.z() > 0 ? Direction.SOUTH : Direction.NORTH;
        }
    }
}
