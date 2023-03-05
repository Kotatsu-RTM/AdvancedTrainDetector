package com.siwo951.forgemod.advancedtraindetector.block.tileentity

import com.siwo951.forgemod.advancedtraindetector.toBlockPos
import jp.ngt.rtm.rail.TileEntityLargeRailBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.ITickable
import net.minecraft.util.math.BlockPos

internal class TileEntityAdvancedTrainDetector: TileEntityCustom(), ITickable {
    private var isRsOutput: Boolean = false

    private var canTurnOff: Boolean = false
    private var isDetectPrev: Boolean = false

    var detectPos: MutableList<BlockPos> = mutableListOf(BlockPos.ORIGIN)
    var isFlipFlop: Boolean = false
    var isReversal: Boolean = false

    override fun update() {
        if (this.world.isRemote) return

        val isDetect = this.detectPos.mapNotNull { this.world.getTileEntity(it) }
            .filterIsInstance<TileEntityLargeRailBase>().any { it.isTrainOnRail }

        if (isDetect == this.isDetectPrev) return

        when (this.isFlipFlop) {
            true -> when (isDetect) {
                true -> when (this.isRsOutput) {
                    true -> this.canTurnOff = true
                    else -> this.isRsOutput = true
                }
                else -> if (this.canTurnOff) {
                    this.canTurnOff = false
                    this.isRsOutput = false
                }
            }
            else -> {
                this.isRsOutput = isDetect
                this.canTurnOff = false
            }
        }

        this.isDetectPrev = isDetect

        this.world.notifyNeighborsOfStateChange(this.pos, this.blockType, true)
    }

    override fun readFromNBT(nbt: NBTTagCompound) {
        super.readFromNBT(nbt)

        this.detectPos.clear()
        nbt.getTagList("detectPosList", 10).apply {
            repeat(tagCount()) { detectPos += getCompoundTagAt(it).getLong("pos").toBlockPos() }
        }

        this.isFlipFlop = nbt.getBoolean("isFlipFlop")
        this.isReversal = nbt.getBoolean("isReversal")
    }
    override fun writeToNBT(nbt: NBTTagCompound): NBTTagCompound {
        super.writeToNBT(nbt)

        val detectPosList = NBTTagList()
        this.detectPos.forEach {
            val tag = NBTTagCompound()
            tag.setLong("pos", it.toLong())
            detectPosList.appendTag(tag)
        }
        nbt.setTag("detectPosList", detectPosList)

        nbt.setBoolean("isFlipFlop", this.isFlipFlop)
        nbt.setBoolean("isReversal", this.isReversal)

        return nbt
    }

    fun isRsOutput(): Boolean = if (this.isReversal) !this.isRsOutput else this.isRsOutput
}