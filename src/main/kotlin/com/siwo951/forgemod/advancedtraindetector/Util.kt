package com.siwo951.forgemod.advancedtraindetector

import net.minecraft.util.math.BlockPos

fun Long.toBlockPos(): BlockPos = BlockPos.fromLong(this)