package com.siwo951.forgemod.advancedtraindetector

import com.siwo951.forgemod.advancedtraindetector.block.BlockAdvancedTrainDetector
import com.siwo951.forgemod.advancedtraindetector.block.tileentity.TileEntityAdvancedTrainDetector
import com.siwo951.forgemod.advancedtraindetector.gui.GuiAdvancedTrainDetectorHandler
import com.siwo951.forgemod.advancedtraindetector.item.ItemPosSettingTool
import com.siwo951.forgemod.advancedtraindetector.network.PacketAdvancedTrainDetector
import jp.ngt.ngtlib.NGTCore
import jp.ngt.rtm.RTMCore
import net.minecraft.block.Block
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.util.*
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.common.ForgeVersion
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLConstructionEvent
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@Mod(
    modid = AdvancedTrainDetectorCore.MODID, version = AdvancedTrainDetectorCore.VERSION, name = AdvancedTrainDetectorCore.MODNAME,
    dependencies = "required-after:${ForgeVersion.MOD_ID};required-after:${RTMCore.MODID};required-after:${NGTCore.MODID}"
)
class AdvancedTrainDetectorCore {
    @Mod.EventHandler
    fun fmlConstEvent(event: FMLConstructionEvent?) {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun registerItems(event: RegistryEvent.Register<Item?>) {
        event.registry.register(
            POS_SETTING_TOOL
        )
        event.registry.register(
            ItemBlock(ADVANCED_TRAIN_DETECTOR_BLOCK).setRegistryName(MODID, "advancedtraindetector")
        )
    }

    @SubscribeEvent
    fun registerBlocks(event: RegistryEvent.Register<Block?>) = event.registry.register(ADVANCED_TRAIN_DETECTOR_BLOCK)

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    fun registerModels(event: ModelRegistryEvent?) {
        ModelLoader.setCustomModelResourceLocation(
            POS_SETTING_TOOL, 0,
            ModelResourceLocation(ResourceLocation(MODID, "pos_setting_tool"), "inventory")
        )
        ModelLoader.setCustomModelResourceLocation(
            Item.getItemFromBlock(ADVANCED_TRAIN_DETECTOR_BLOCK), 0,
            ModelResourceLocation(ResourceLocation(MODID, "advancedtraindetector"), "inventory")
        )
    }

    @Mod.EventHandler
    fun fmlInitEvent(event: FMLInitializationEvent?) {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, GuiAdvancedTrainDetectorHandler())
        NETWORK_WRAPPER.registerMessage(
            PacketAdvancedTrainDetector::class.java, PacketAdvancedTrainDetector::class.java, 0, Side.SERVER
        )
        GameRegistry.registerTileEntity(
            TileEntityAdvancedTrainDetector::class.java,
            ResourceLocation(MODID, "TE_AdvancedTrainDetector")
        )
    }

    companion object {
        const val MODNAME = "AdvancedTrainDetector"
        const val MODID = "advancedtraindetector"
        const val VERSION = "1.0.1"

        const val guiId_AdvancedTrainDetector = 0

        val NETWORK_WRAPPER: SimpleNetworkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel("advanced_td")
        val ADVANCED_TRAIN_DETECTOR_BLOCK: Block = BlockAdvancedTrainDetector()
        val POS_SETTING_TOOL: Item = ItemPosSettingTool()

        @Mod.Instance(MODID)
        lateinit var INSTANCE: AdvancedTrainDetectorCore
    }
}