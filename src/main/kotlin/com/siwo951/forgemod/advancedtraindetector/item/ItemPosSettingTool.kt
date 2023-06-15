package com.siwo951.forgemod.advancedtraindetector.item

import com.siwo951.forgemod.advancedtraindetector.AdvancedTrainDetectorCore
import com.siwo951.forgemod.advancedtraindetector.CreativeTabAdvancedTrainDetector
import com.siwo951.forgemod.advancedtraindetector.block.tileentity.TileEntityAdvancedTrainDetector
import com.siwo951.forgemod.advancedtraindetector.toBlockPos
import jp.ngt.ngtlib.NGTCore
import jp.ngt.ngtlib.network.PacketNBT
import jp.ngt.rtm.rail.TileEntityLargeRailBase
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.world.World

class ItemPosSettingTool : Item() {
    private val translatePrefix = "AdvancedTrainDetector.itempos"

    init {
        this.creativeTab = CreativeTabAdvancedTrainDetector.tabUtils
        this.registryName = ResourceLocation("${AdvancedTrainDetectorCore.MOD_ID}:itempossettingtool")
        this.unlocalizedName = "${AdvancedTrainDetectorCore.MOD_ID}:itempossettingtool"
        this.hasSubtypes = true
    }

    override fun onItemUse(
        player: EntityPlayer, world: World, blockPos: BlockPos, hand: EnumHand,
        facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float
    ): EnumActionResult {
        if (world.isRemote) return EnumActionResult.PASS

        val itemStack = player.getHeldItem(hand)

        when (val tileEntity = world.getTileEntity(blockPos)) {
            is TileEntityLargeRailBase -> {
                if (itemStack.tagCompound == null) itemStack.tagCompound = NBTTagCompound()
                itemStack.tagCompound!!.setLong("pos", blockPos.toLong())
                player.sendChat("$translatePrefix.item_coordinates_saved", blockPos)
            }
            is TileEntityAdvancedTrainDetector -> itemStack.tagCompound.apply {
                if (this?.hasKey("pos") != true) {
                    player.sendChat("$translatePrefix.item_coordinates_notsaved")
                    return EnumActionResult.SUCCESS
                }

                getLong("pos").toBlockPos().let {
                    when (tileEntity.addDetectPos(it)) {
                        true -> player.sendChat("$translatePrefix.block_coordinates_save", it)
                        else -> player.sendChat("$translatePrefix.block_coordinates_already")
                    }
                }

                NGTCore.NETWORK_WRAPPER.sendTo(
                    PacketNBT::class.java
                        .getDeclaredConstructor(TileEntity::class.java, Boolean::class.java)
                        .apply { isAccessible = true }
                        .newInstance(tileEntity, true),
                    player as EntityPlayerMP
                )
            }
        }

        return EnumActionResult.SUCCESS
    }

    override fun getSubItems(tab: CreativeTabs, items: NonNullList<ItemStack>) {
        if (!isInCreativeTab(tab)) return
        items.add(ItemStack(this, 1, 0))
    }

    override fun getUnlocalizedName(itemStack: ItemStack): String = "${super.getUnlocalizedName()}.${itemStack.itemDamage}"

    private fun EntityPlayer.sendChat(translateKey: String, blockPos: BlockPos? = null) {
        this.sendMessage(
            TextComponentTranslation(translateKey, blockPos?.x, blockPos?.y, blockPos?.z)
        )
    }
    private fun TileEntityAdvancedTrainDetector.addDetectPos(detectPos: BlockPos): Boolean {
        this.detectPos.apply {
            if (any { it == detectPos }) return false
            when (this[0] == BlockPos.ORIGIN) {
                true -> this[0] = detectPos
                else -> this += detectPos
            }
        }
        return true
    }
}