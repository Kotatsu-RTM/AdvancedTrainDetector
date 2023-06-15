package com.siwo951.forgemod.advancedtraindetector.block

import com.siwo951.forgemod.advancedtraindetector.AdvancedTrainDetectorCore
import com.siwo951.forgemod.advancedtraindetector.CreativeTabAdvancedTrainDetector
import com.siwo951.forgemod.advancedtraindetector.block.tileentity.TileEntityAdvancedTrainDetector
import net.minecraft.block.BlockContainer
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumBlockRenderType
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World

class BlockAdvancedTrainDetector: BlockContainer(Material.ROCK) {
    init {
        setCreativeTab(CreativeTabAdvancedTrainDetector.tabUtils)
        this.registryName = ResourceLocation("${AdvancedTrainDetectorCore.MODID}:advancedtraindetector")
        this.unlocalizedName = "advancedtraindetector"
        this.soundType = SoundType.STONE
    }

    override fun getRenderType(state: IBlockState): EnumBlockRenderType = EnumBlockRenderType.MODEL

    override fun onBlockActivated(
        world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer, hand: EnumHand,
        facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float
    ): Boolean {
        player.openGui(
            AdvancedTrainDetectorCore.INSTANCE, AdvancedTrainDetectorCore.guiId_AdvancedTrainDetector,
            player.entityWorld, pos.x, pos.y, pos.z
        )

        return true
    }

    override fun createNewTileEntity(world: World, metadata: Int): TileEntity = TileEntityAdvancedTrainDetector()

    override fun canProvidePower(state: IBlockState): Boolean = true

    override fun getWeakPower(
        blockState: IBlockState, blockAccess: IBlockAccess, pos: BlockPos, side: EnumFacing
    ): Int = this.getStrongPower(blockState, blockAccess, pos, side)

    override fun getStrongPower(
        blockState: IBlockState, blockAccess: IBlockAccess, pos: BlockPos, side: EnumFacing
    ): Int = if (blockAccess.getTileEntity(pos).let { it is TileEntityAdvancedTrainDetector && it.isRsOutput() }) 15 else 0
}