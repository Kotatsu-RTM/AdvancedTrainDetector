package com.siwo951.forgemod.advancedtraindetector.gui

import com.siwo951.forgemod.advancedtraindetector.AdvancedTrainDetectorCore
import com.siwo951.forgemod.advancedtraindetector.block.tileentity.TileEntityAdvancedTrainDetector
import com.siwo951.forgemod.advancedtraindetector.network.PacketAdvancedTrainDetector
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.resources.I18n
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.client.config.GuiCheckBox
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.io.IOException

@Suppress("SameParameterValue", "SpellCheckingInspection")
internal class GuiAdvancedTrainDetector(private val tile: TileEntityAdvancedTrainDetector) : GuiScreenCustom() {
    private val translatePrefix = "AdvancedTrainDetector.gui"

    private val detectPos: MutableList<BlockPos> = tile.detectPos
    private var isFlipFlop: Boolean = tile.isFlipFlop
    private var isReversal: Boolean = tile.isReversal

    private lateinit var isFlipFlopCheck: GuiCheckBox
    private lateinit var isReversalCheck: GuiCheckBox

    private var currentScroll = 0

    //チェックボックスとボタンはここ
    override fun initGui() {
        super.initGui()

        val hHeight = height / 2
        val hWidth = width / 2

        isFlipFlopCheck = GuiCheckBox(
            2, hWidth - 80, hHeight - 50 + currentScroll,
            I18n.format("$translatePrefix.flipflop"), isFlipFlop
        )
        isReversalCheck = GuiCheckBox(
            3, hWidth - 80, hHeight - 35 + currentScroll,
            I18n.format("$translatePrefix.reversal"), isReversal
        )

        buttonList.apply {
            add(
                GuiButton(
                    0, hWidth - 110, height - 30,
                    100, 20, I18n.format("$translatePrefix.done")
                )
            )
            add(
                GuiButton(
                    1, hWidth + 10, height - 30,
                    100, 20, I18n.format("$translatePrefix.cancel")
                )
            )

            add(isFlipFlopCheck)
            add(isReversalCheck)
        }

        var textFieldId = 100

        detectPos.forEachIndexed { i, it ->
            val positionH = (hHeight + currentScroll) + (25 * i)

            guiTextField(textFieldId++, it.x, hWidth - 80, positionH, Byte.MAX_VALUE.toInt(), 40)
            guiTextField(textFieldId++, it.y, hWidth - 35, positionH, Byte.MAX_VALUE.toInt(), 40)
            guiTextField(textFieldId++, it.z, hWidth + 10, positionH, Byte.MAX_VALUE.toInt(), 40)

            if (detectPos.size < 50) buttonList.add(
                GuiButton(1000 + i, hWidth + 55, positionH, 20, 20, "+")
            )
            if (detectPos.size > 1) buttonList.add(
                GuiButton(1100 + i, hWidth + 80, positionH, 20, 20, "-")
            )
        }
    }

    // 毎tick呼び出される
    // 文字描画だったりボタンの表示非表示の更新だった李をするところ
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTick: Float) {
        super.drawScreen(mouseX, mouseY, partialTick)

        val hHeight = height / 2
        val hWidth = width / 2

        with(fontRenderer) {
            drawString("X", hWidth - 63, hHeight - 15 + currentScroll, 0xffffff)
            drawString("Y", hWidth - 18, hHeight - 15 + currentScroll, 0xffffff)
            drawString("Z", hWidth + 27, hHeight - 15 + currentScroll, 0xffffff)

            for (i in detectPos.indices) drawString(
                "${i + 1}", hWidth - 100, (hHeight + 6 + currentScroll) + (25 * i), 0xffffff
            )
        }

        if (isFlipFlopCheck.isMouseOver) this.drawHoveringText(
            I18n.format("$translatePrefix.flipflop_hover"), mouseX, mouseY
        )
        if (isReversalCheck.isMouseOver) this.drawHoveringText(
            I18n.format("$translatePrefix.reversal_hover"), mouseX, mouseY
        )
    }

    // キーボード入力時のevent
    @Throws(IOException::class)
    public override fun keyTyped(par1: Char, par2: Int) {
        super.keyTyped(par1, par2)

        textFieldList.forEach { it.textboxKeyTyped(par1, par2) }

        if (par2 != Keyboard.KEY_RETURN) return
        sendPacket()
        mc.displayGuiScreen(null)
    }

    // button押したときのevent
    override fun actionPerformed(button: GuiButton) {
        //決定(0), キャンセル(1)
        if (button.id in 0..1) {
            mc.displayGuiScreen(null)
            if (button.id == 0) sendPacket()
        }

        if (button.id >= 1000) {
            saveValue()
            when {
                button.id >= 1100 -> detectPos.removeAt(button.id - 1100)
                button.id >= 1000 -> detectPos.add(button.id - 1000 + 1, BlockPos.ORIGIN)
            }
            initGui()
        }
    }

    @Throws(IOException::class)
    override fun handleMouseInput() {
        super.handleMouseInput()

        if (Mouse.getEventDWheel() == 0) return

        currentScroll = (currentScroll + (if (Mouse.getEventDWheel() > 0) 25 else -25)).coerceIn(-width, 0)

        saveValue()
        initGui()
    }

    private fun sendPacket() {
        saveValue()
        AdvancedTrainDetectorCore.NETWORK_WRAPPER.sendToServer(
            PacketAdvancedTrainDetector(tile, detectPos, isFlipFlop, isReversal)
        )
    }

    private fun saveValue() {
        buttonList.forEach {
            if (it.id == 2) isFlipFlop = (it as GuiCheckBox).isChecked
            if (it.id == 3) isReversal = (it as GuiCheckBox).isChecked
        }

        repeat(detectPos.size) {
            detectPos[it] = BlockPos(
                getIntGuiTextFieldText(3 * it),
                getIntGuiTextFieldText(3 * it + 1),
                getIntGuiTextFieldText(3 * it + 2)
            )
        }
    }

    private fun getIntGuiTextFieldText(number: Int): Int = textFieldList[number].text.toIntOrNull() ?: 0

    private fun guiTextField(id: Int, value: Int, xPosition: Int, yPosition: Int, maxLength: Int, width: Int) =
        GuiTextField(id, fontRenderer, xPosition, yPosition, width, 20).apply {
            isFocused = false
            maxStringLength = maxLength
            text = value.toString()
            textFieldList.add(this)
        }
}