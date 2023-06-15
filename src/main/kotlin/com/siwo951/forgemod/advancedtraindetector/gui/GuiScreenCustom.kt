package com.siwo951.forgemod.advancedtraindetector.gui

import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import java.io.IOException

abstract class GuiScreenCustom: GuiScreen() {
    val textFieldList = mutableListOf<GuiTextField>()

    override fun initGui() {
        super.initGui()
        super.buttonList.clear()
        textFieldList.clear()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTick: Float) {
        super.drawDefaultBackground() //背景
        textFieldList.forEach { it.drawTextBox() }
        super.drawScreen(mouseX, mouseY, partialTick)
    }

    override fun updateScreen() {
        super.updateScreen()
        textFieldList.forEach { it.updateCursorCounter() }
    }

    @Throws(IOException::class)
    override fun mouseClicked(x: Int, y: Int, btn: Int) {
        super.mouseClicked(x, y, btn)
        textFieldList.forEach { it.mouseClicked(x, y, btn) }
    }

    //GUI開いてるときにマイクラが止まるかどうか
    override fun doesGuiPauseGame(): Boolean = false
}