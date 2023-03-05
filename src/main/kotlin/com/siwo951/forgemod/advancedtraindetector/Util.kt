package com.siwo951.forgemod.advancedtraindetector

import com.siwo951.forgemod.advancedtraindetector.block.tileentity.TileEntityAdvancedTrainDetector
import io.netty.buffer.ByteBuf
import net.minecraft.util.math.BlockPos

internal fun ByteBuf.readBlockPosList(): MutableList<BlockPos> {
    val blockPosList = mutableListOf<BlockPos>()

    val valueSize = this.readInt()
    repeat(valueSize) { blockPosList += this.readLong().toBlockPos() }

    return blockPosList
}
internal fun ByteBuf.writeBlockPosList(value: MutableList<BlockPos>) {
    this.writeInt(value.size)
    value.forEach { this.writeLong(it.toLong()) }
}
internal fun Long.toBlockPos(): BlockPos {
    return BlockPos.fromLong(this)
}
internal fun TileEntityAdvancedTrainDetector.addDetectPos(detectPos: BlockPos): Boolean {
    this.detectPos.apply {
        if (any { it == detectPos }) return false
        when (this[0] == BlockPos.ORIGIN) {
            true -> this[0] = detectPos
            else -> this += detectPos
        }
    }
    return true
}