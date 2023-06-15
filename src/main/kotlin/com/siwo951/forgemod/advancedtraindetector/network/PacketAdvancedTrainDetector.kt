package com.siwo951.forgemod.advancedtraindetector.network

import com.siwo951.forgemod.advancedtraindetector.block.tileentity.TileEntityAdvancedTrainDetector
import com.siwo951.forgemod.advancedtraindetector.toBlockPos
import io.netty.buffer.ByteBuf
import jp.ngt.ngtlib.NGTCore
import jp.ngt.ngtlib.network.PacketNBT
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

internal class PacketAdvancedTrainDetector: IMessage, IMessageHandler<PacketAdvancedTrainDetector, IMessage?> {
    private var entityPos: BlockPos? = null

    private var detectPos: MutableList<BlockPos>? = null
    private var isFlipFlop: Boolean? = null
    private var isReversal: Boolean? = null

    constructor()
    constructor(entity: TileEntity, detectPos: MutableList<BlockPos>, isFlipFlop: Boolean, isReversal: Boolean) {
        this.entityPos = entity.pos

        this.detectPos = detectPos
        this.isFlipFlop = isFlipFlop
        this.isReversal = isReversal
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeLong(this.entityPos!!.toLong())

        buf.writeBlockPosList(this.detectPos!!)
        buf.writeBoolean(this.isFlipFlop!!)
        buf.writeBoolean(this.isReversal!!)
    }

    override fun fromBytes(buf: ByteBuf) {
        this.entityPos = buf.readLong().toBlockPos()

        this.detectPos = buf.readBlockPosList()
        this.isFlipFlop = buf.readBoolean()
        this.isReversal = buf.readBoolean()
    }

    override fun onMessage(message: PacketAdvancedTrainDetector, ctx: MessageContext): IMessage? {
        val tile = ctx.serverHandler.player.world.getTileEntity(message.entityPos!!) as TileEntityAdvancedTrainDetector

        tile.detectPos = message.detectPos!!
        tile.isFlipFlop = message.isFlipFlop!!
        tile.isReversal = message.isReversal!!

        NGTCore.NETWORK_WRAPPER.sendTo(
            PacketNBT::class.java
                .getDeclaredConstructor(TileEntity::class.java, Boolean::class.java)
                .apply { isAccessible = true }
                .newInstance(tile, true),
            ctx.serverHandler.player
        )

        tile.markDirty()

        tile.world.notifyNeighborsOfStateChange(tile.pos, tile.blockType, true)

        return null
    }

    private fun ByteBuf.readBlockPosList(): MutableList<BlockPos> {
        val blockPosList = mutableListOf<BlockPos>()

        val valueSize = this.readInt()
        repeat(valueSize) { blockPosList += this.readLong().toBlockPos() }

        return blockPosList
    }
    private fun ByteBuf.writeBlockPosList(value: MutableList<BlockPos>) {
        this.writeInt(value.size)
        value.forEach { this.writeLong(it.toLong()) }
    }
}