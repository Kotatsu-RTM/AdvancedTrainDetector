package com.siwo951.forgemod.advancedtraindetector.block.tileentity

import com.siwo951.forgemod.advancedtraindetector.toBlockPos
import jp.ngt.rtm.rail.TileEntityLargeRailBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.ITickable
import net.minecraft.util.math.BlockPos

class TileEntityAdvancedTrainDetector: TileEntityCustom(), ITickable {
    private var isRsOutput: Boolean = false

    private var canTurnOff: Boolean = false
    private var isDetectPrev: Boolean = false

    var detectPos: MutableList<BlockPos> = mutableListOf(BlockPos.ORIGIN)
    var isFlipFlop: Boolean = false
    var isReversal: Boolean = false

    override fun update() {
        if (world.isRemote) return

        val isDetect = detectPos.mapNotNull { world.getTileEntity(it) }
            .filterIsInstance<TileEntityLargeRailBase>().any { it.isTrainOnRail }

        if (isDetect == isDetectPrev) return

        when {
            !isFlipFlop -> {
                isRsOutput = isDetect
                canTurnOff = false
            }
            isDetect -> when (isRsOutput) {
                true -> canTurnOff = true
                else -> isRsOutput = true
            }
            else -> if (canTurnOff) {
                canTurnOff = false
                isRsOutput = false
            }
        }

        isDetectPrev = isDetect

        world.notifyNeighborsOfStateChange(pos, blockType, true)
    }

    override fun readFromNBT(nbt: NBTTagCompound) {
        super.readFromNBT(nbt)

        detectPos.clear()
        nbt.getTagList("detectPosList", 10).apply {
            repeat(tagCount()) { detectPos += getCompoundTagAt(it).getLong("pos").toBlockPos() }
        }

        isFlipFlop = nbt.getBoolean("isFlipFlop")
        isReversal = nbt.getBoolean("isReversal")
    }
    override fun writeToNBT(nbt: NBTTagCompound): NBTTagCompound {
        super.writeToNBT(nbt)

        val detectPosList = NBTTagList()
        detectPos.forEach {
            val tag = NBTTagCompound()
            tag.setLong("pos", it.toLong())
            detectPosList.appendTag(tag)
        }
        nbt.setTag("detectPosList", detectPosList)

        nbt.setBoolean("isFlipFlop", isFlipFlop)
        nbt.setBoolean("isReversal", isReversal)

        return nbt
    }

    fun isRsOutput(): Boolean = if (isReversal) !isRsOutput else isRsOutput
}