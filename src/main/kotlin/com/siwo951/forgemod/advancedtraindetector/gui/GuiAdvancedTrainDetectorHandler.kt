package com.siwo951.forgemod.advancedtraindetector.gui

import com.siwo951.forgemod.advancedtraindetector.AdvancedTrainDetectorCore
import com.siwo951.forgemod.advancedtraindetector.block.tileentity.TileEntityAdvancedTrainDetector
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.common.network.IGuiHandler

internal class GuiAdvancedTrainDetectorHandler : IGuiHandler {
    override fun getServerGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): Any? = null

    override fun getClientGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): Any? {
        if (ID != AdvancedTrainDetectorCore.guiId_AdvancedTrainDetector) return null
        return GuiAdvancedTrainDetector(
            player.entityWorld.getTileEntity(BlockPos(x, y, z)) as TileEntityAdvancedTrainDetector
        )
    }
}