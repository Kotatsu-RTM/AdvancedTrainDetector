package com.siwo951.forgemod.advancedtraindetector

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class CreativeTabAdvancedTrainDetector(label: String) : CreativeTabs(label) {
    @SideOnly(Side.CLIENT)
    override fun getTabIconItem(): ItemStack = ItemStack(Item.getItemFromBlock(AdvancedTrainDetectorCore.ADVANCED_TRAIN_DETECTOR_BLOCK))

    @SideOnly(Side.CLIENT)
    override fun getTranslatedTabLabel(): String = "AdvancedTrainDetector"

    companion object {
        val tabUtils: CreativeTabs = CreativeTabAdvancedTrainDetector("AdvancedTrainDetector")
    }
}