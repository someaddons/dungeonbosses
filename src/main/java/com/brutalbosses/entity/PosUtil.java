package com.brutalbosses.entity;

import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

public class PosUtil
{
    public static Direction getFacing(final Vector3d from, final Vector3d to)
    {
        final Vector3d vector = to.subtract(from);

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
