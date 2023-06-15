package com.siwo951.forgemod.advancedtraindetector.block.tileentity

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SPacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity

abstract class TileEntityCustom: TileEntity() {
    override fun getUpdateTag(): NBTTagCompound = writeToNBT(NBTTagCompound())
    override fun onDataPacket(net: NetworkManager, pkt: SPacketUpdateTileEntity) = readFromNBT(pkt.nbtCompound)
}