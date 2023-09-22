package io.github.natanfudge.hardcraft.config

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import io.github.natanfudge.genericutils.client.Buttons
import io.github.natanfudge.genericutils.client.Buttons.create
import io.github.natanfudge.genericutils.client.McColor
import io.github.natanfudge.genericutils.client.RenderContext
import io.github.natanfudge.genericutils.platform.PlatformHelp.configDir
import io.github.natanfudge.genericutils.platform.PlatformHelp.isClient
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.gui.widget.ElementListWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.resource.language.I18n
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.screen.ScreenTexts
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.awt.Color
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.regex.Pattern
import kotlin.math.max
import kotlin.math.min

/**
 * MidnightConfig v2.2.0 by TeamMidnightDust & Motschen
 * Single class config library - feel free to copy!
 *
 *
 * Based on https://github.com/Minenash/TinyConfig
 * Credits to Minenash
 */
abstract class MidnightConfig {
    protected class EntryInfo {
        var field: Field? = null
        var widget: Any? = null
        var width = 0
        var max = 0
        var centered = false
        var comment = false
        var error: Map.Entry<TextFieldWidget, Text>? = null
        var defaultValue: Any? = null
        var value: Any? = null
        var tempValue: String? = null
        var inLimits = true
        var id: String? = null
        var name: Text? = null
        var index = 0
        var colorButton: ClickableWidget? = null
    }

    protected class MidnightConfigScreen(val parent: Screen, val modid: String) : Screen(
        Text.translatable(
            "$modid.midnightconfig.title"
        )
    ) {
        val translationPrefix: String
        var list: MidnightConfigListWidget? = null
        var reload = false

        init {
            translationPrefix = "$modid.midnightconfig."
        }

        // Real Time config update //
        override fun tick() {
            super.tick()
            for (info in entries) {
                try {
                    info.field!![null] = info.value
                } catch (ignored: IllegalAccessException) {
                }
            }
            updateResetButtons()
        }

        fun updateResetButtons() {
            if (list != null) {
                for (entry in list!!.children()) {
                    if (entry!!.buttons != null && entry.buttons.size > 1) {
                        val button = entry.buttons.get(1)
                        if (button is ButtonWidget) {
                            button.active = entry.info.value.toString() != entry.info.defaultValue.toString()
                        }
                    }
                }
            }
        }

        fun loadValues() {
            try {
                gson.fromJson(Files.newBufferedReader(path), configClass[modid])
            } catch (e: Exception) {
                write(modid)
            }
            for (info in entries) {
                if (info.field!!.isAnnotationPresent(Entry::class.java)) try {
                    info.value = info.field!![null]
                    info.tempValue = info.value.toString()
                } catch (ignored: IllegalAccessException) {
                }
            }
        }

        public override fun init() {
            super.init()
            if (!reload) loadValues()
            val done = create(
                ScreenTexts.DONE,
                width / 2 + 4, height - 28, 150, 20
            ) { button: ButtonWidget? ->
                for (info in entries) if (info.id == modid) {
                    try {
                        info.field!![null] = info.value
                    } catch (ignored: IllegalAccessException) {
                    }
                }
                write(modid)
                client!!.setScreen(parent)
            }
            list = MidnightConfigListWidget(client, width, height, 32, height - 32, 25)
            if (client != null && client!!.world != null) list!!.setRenderBackground(false)
            addSelectableChild(list)
            for (info in entries) {
                if (info.id == modid) {
                    val name = Objects.requireNonNullElseGet(info.name) { Text.translatable(translationPrefix + info.field!!.name) }
                    val resetButton: ButtonWidget = Buttons.create(Text.translatable("Reset").formatted(Formatting.RED),width - 205, 0, 40, 20) { button ->
                        info.value = info.defaultValue
                        info.tempValue = info.defaultValue.toString()
                        info.index = 0
                        val scrollAmount = list!!.scrollAmount
                        reload = true
                        client!!.setScreen(this)
                        list!!.scrollAmount = scrollAmount
                    }
                    if (info.widget is Map.Entry<*, *>) {
                        val widget = info.widget as MutableMap.MutableEntry<ButtonWidget.PressAction, Function<Any?, Text>>?
                        if (info.field!!.type.isEnum) widget!!.setValue(Function { value: Any? -> Text.translatable(translationPrefix + "enum." + info.field!!.type.getSimpleName() + "." + info.value.toString()) })
                        list!!.addButton(
                            java.util.List.of(
                                Buttons.create(widget!!.value.apply(info.value),width - 160, 0, 150, 20){widget.key.onPress(it)},
                                resetButton
                            ), name, info
                        )
                    } else if (info.field!!.type == MutableList::class.java) {
                        if (!reload) info.index = 0
                        val widget = TextFieldWidget(textRenderer, width - 160, 0, 150, 20, null)
                        widget.setMaxLength(info.width)
                        if (info.index < (info.value as List<String?>?)!!.size) widget.text =
                            (info.value as List<String>?)!![info.index].toString() else widget.text = ""
                        val processor = (info.widget as BiFunction<TextFieldWidget?, ButtonWidget?, Predicate<String?>?>?)!!.apply(widget, done)
                        widget.setTextPredicate(processor)
                        resetButton.width = 20
                        resetButton.message = Text.literal("R").formatted(Formatting.RED)
                        val cycleButton: ButtonWidget = Buttons.create(Text.literal(info.index.toString()).formatted(Formatting.GOLD),width - 185, 0, 20, 20) { button ->
                            (info.value as MutableList<String?>?)!!.remove("")
                            val scrollAmount = list!!.scrollAmount
                            reload = true
                            info.index = info.index + 1
                            if (info.index > (info.value as List<String?>?)!!.size) info.index = 0
                            client!!.setScreen(this)
                            list!!.scrollAmount = scrollAmount
                        }
                        list!!.addButton(java.util.List.of(widget, resetButton, cycleButton), name, info)
                    } else if (info.widget != null) {
                        val widget = TextFieldWidget(textRenderer, width - 160, 0, 150, 20, null)
                        widget.setMaxLength(info.width)
                        widget.text = info.tempValue
                        val processor = (info.widget as BiFunction<TextFieldWidget?, ButtonWidget?, Predicate<String?>?>?)!!.apply(widget, done)
                        widget.setTextPredicate(processor)
                        if (info.field!!.getAnnotation<Entry>(Entry::class.java).isColor) {
                            resetButton.width = 20
                            resetButton.message = Text.literal("R").formatted(Formatting.RED)
                            val colorButton: ButtonWidget = Buttons.create(Text.literal("⬛"), width - 185, 0, 20, 20) { }
                            try {
                                colorButton.message = Text.literal("⬛").setStyle(Style.EMPTY.withColor(Color.decode(info.tempValue).rgb))
                            } catch (ignored: Exception) {
                            }
                            info.colorButton = colorButton
                            colorButton.active = false
                            list!!.addButton(java.util.List.of(widget, resetButton, colorButton), name, info)
                        } else list!!.addButton(java.util.List.of(widget, resetButton), name, info)
                    } else {
                        list!!.addButton(listOf<ClickableWidget>(), name, info)
                    }
                }
                updateResetButtons()
            }
            addDrawableChild(Buttons.create(ScreenTexts.CANCEL,width / 2 - 154, height - 28, 150, 20) { button ->
                loadValues()
                client!!.setScreen(parent)
            })
            addDrawableChild(done)
        }

        override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) = with(RenderContext(matrices)) {
            renderBackground(matrices)
            list!!.render(matrices, mouseX, mouseY, delta)
            drawCenteredTextWithShadow(title, width / 2, 15, McColor.Black)
            for (info in entries) {
                if (info.id == modid) {
                    if (list!!.getHoveredButton(mouseX.toDouble(), mouseY.toDouble()).isPresent) {
                        val buttonWidget = list!!.getHoveredButton(mouseX.toDouble(), mouseY.toDouble()).get()
                        val text = ButtonEntry.buttonsWithText[buttonWidget]
                        val name: Text = Text.translatable(translationPrefix + info.field!!.name)
                        val key = translationPrefix + info.field!!.name + ".tooltip"
                        if (info.error != null && text == name) drawTooltip(
                            info.error!!.value,
                            mouseX,
                            mouseY
                        ) else if (I18n.hasTranslation(key) && text == name) {
                            val list: MutableList<Text> = ArrayList()
                            for (str in I18n.translate(key).split("\n".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()) list.add(Text.literal(str))
                            drawTooltip(list, mouseX, mouseY)
                        }
                    }
                }
            }
            super.render(matrices, mouseX, mouseY, delta)
        }
    }

    protected class MidnightConfigListWidget(minecraftClient: MinecraftClient?, i: Int, j: Int, k: Int, l: Int, m: Int) :
        ElementListWidget<ButtonEntry?>(minecraftClient, i, j, k, l, m) {
        var textRenderer: TextRenderer

        init {
            centerListVertically = false
            textRenderer = minecraftClient!!.textRenderer
        }

        public override fun getScrollbarPositionX(): Int {
            return width - 7
        }

        fun addButton(buttons: List<ClickableWidget?>, text: Text?, info: EntryInfo) {
            addEntry(ButtonEntry.create(buttons, text, info))
        }

        override fun getRowWidth(): Int {
            return 10000
        }

        fun getHoveredButton(mouseX: Double, mouseY: Double): Optional<ClickableWidget> {
            for (buttonEntry in children()) {
                if (!buttonEntry!!.buttons.isEmpty() && buttonEntry.buttons[0]!!.isMouseOver(mouseX, mouseY)) {
                    return Optional.of(buttonEntry.buttons[0]!!)
                }
            }
            return Optional.empty()
        }
    }

    protected class ButtonEntry private constructor(buttons: List<ClickableWidget?>, text: Text?, info: EntryInfo) : ElementListWidget.Entry<ButtonEntry?>() {
        val buttons: List<ClickableWidget?>
        private val text: Text?
        val info: EntryInfo
        private val children: MutableList<ClickableWidget?> = ArrayList()

        init {
            if (!buttons.isEmpty()) buttonsWithText[buttons[0]] = text
            this.buttons = buttons
            this.text = text
            this.info = info
            children.addAll(buttons)
        }

        override fun render(
            stack: MatrixStack,
            index: Int,
            y: Int,
            x: Int,
            entryWidth: Int,
            entryHeight: Int,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            tickDelta: Float
        )  =with(RenderContext(stack)){
            buttons.forEach(Consumer { b: ClickableWidget? ->
                b!!.y = y
                b.render(stack, mouseX, mouseY, tickDelta)
            })
            val color = if (info.comment) McColor.Gray else McColor.Black
            if (text != null && (!text.string.contains("spacer") || !buttons.isEmpty())) {
                if (info.centered) drawTextWithShadow(
                    textRenderer,
                    text,
                    MinecraftClient.getInstance().window.scaledWidth / 2 - textRenderer.getWidth(text) / 2,
                    y + 5,
                    color
                ) else drawTextWithShadow(
                    textRenderer, text, 12, y + 5, color
                )
            }
        }

        override fun children(): List<Element?> {
            return children
        }

        override fun selectableChildren(): List<Selectable?> {
            return children
        }

        companion object {
            private val textRenderer = MinecraftClient.getInstance().textRenderer
            val buttonsWithText: MutableMap<ClickableWidget?, Text?> = HashMap()
            fun create(buttons: List<ClickableWidget?>, text: Text?, info: EntryInfo): ButtonEntry {
                return ButtonEntry(buttons, text, info)
            }
        }
    }

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FIELD)
    annotation class Entry(
        val width: Int = 100,
        val min: Double = java.lang.Double.MIN_NORMAL,
        val max: Double = Double.MAX_VALUE,
        val name: String = "",
        val isColor: Boolean = false
    )

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FIELD)
    annotation class Client

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FIELD)
    annotation class Server

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FIELD)
    annotation class Hidden

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FIELD)
    annotation class Comment(val centered: Boolean = false)
    class HiddenAnnotationExclusionStrategy : ExclusionStrategy {
        override fun shouldSkipClass(clazz: Class<*>?): Boolean {
            return false
        }

        override fun shouldSkipField(fieldAttributes: FieldAttributes): Boolean {
            return fieldAttributes.getAnnotation(Entry::class.java) == null
        }
    }

    companion object {
        private val INTEGER_ONLY = Pattern.compile("(-?[0-9]*)")
        private val DECIMAL_ONLY = Pattern.compile("-?([\\d]+\\.?[\\d]*|[\\d]*\\.?[\\d]+|\\.)")
        private val HEXADECIMAL_ONLY = Pattern.compile("(-?[#0-9a-fA-F]*)")
        private val entries: MutableList<EntryInfo> = ArrayList()
        val configClass: MutableMap<String, Class<*>> = HashMap()
        private var path: Path? = null
        private val gson = GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT).excludeFieldsWithModifiers(Modifier.PRIVATE)
            .addSerializationExclusionStrategy(HiddenAnnotationExclusionStrategy()).setPrettyPrinting().create()

        fun init(modid: String, config: Class<*>) {
            path = configDir().resolve("$modid.json")
            configClass[modid] = config
            for (field in config.getFields()) {
                val info = EntryInfo()
                if ((field.isAnnotationPresent(Entry::class.java) || field.isAnnotationPresent(Comment::class.java)) && !field.isAnnotationPresent(
                        Server::class.java
                    ) && !field.isAnnotationPresent(Hidden::class.java)
                ) if (isClient()) initClient(modid, field, info)
                if (field.isAnnotationPresent(Comment::class.java)) {
                    info.centered = field.getAnnotation(Comment::class.java).centered
                    info.comment = true
                }
                if (field.isAnnotationPresent(Entry::class.java)) try {
                    info.defaultValue = field[null]
                } catch (ignored: IllegalAccessException) {
                } catch (e: NullPointerException) {
                    throw IllegalArgumentException("Field $field must be static to serve as a config field", e)
                }
            }
            try {
                gson.fromJson(Files.newBufferedReader(path), config)
            } catch (e: Exception) {
                write(modid)
            }
            for (info in entries) {
                if (info.field!!.isAnnotationPresent(Entry::class.java)) try {
                    info.value = info.field!![null]
                    info.tempValue = info.value.toString()
                } catch (ignored: IllegalAccessException) {
                }
            }
        }

        private fun initClient(modid: String, field: Field, info: EntryInfo) {
            val type = field.type
            val e = field.getAnnotation(
                Entry::class.java
            )
            info.width = e?.width ?: 0
            info.field = field
            info.id = modid
            if (e != null) {
                if (e.name != "") info.name = Text.translatable(e.name)
                if (type == Int::class.javaPrimitiveType) textField(
                    info, { s: String -> s.toInt() }, INTEGER_ONLY, e.min.toInt()
                        .toDouble(), e.max.toInt().toDouble(), true
                ) else if (type == Float::class.javaPrimitiveType) textField(
                    info, { s: String -> s.toFloat() }, DECIMAL_ONLY, e.min.toFloat()
                        .toDouble(), e.max.toFloat().toDouble(), false
                ) else if (type == Double::class.javaPrimitiveType) textField(
                    info,
                    { s: String -> s.toDouble() },
                    DECIMAL_ONLY,
                    e.min,
                    e.max,
                    false
                ) else if (type == String::class.java || type == MutableList::class.java) {
                    info.max = if (e.max == Double.MAX_VALUE) Int.MAX_VALUE else e.max.toInt()
                    textField(info, { obj: String -> obj.length }, null, min(e.min, 0.0), max(e.max, 1.0), true)
                } else if (type == Boolean::class.javaPrimitiveType) {
                    val func = Function<Any?, Text> { value: Any? ->
                        Text.translatable(if ((value as Boolean?)!!) "gui.yes" else "gui.no")
                            .formatted(if (value!!) Formatting.GREEN else Formatting.RED)
                    }
                    info.widget = AbstractMap.SimpleEntry<ButtonWidget.PressAction, Function<Any?, Text>>(
                        ButtonWidget.PressAction { button: ButtonWidget ->
                            info.value = !(info.value as Boolean)
                            button.message = func.apply(info.value)
                        }, func
                    )
                } else if (type.isEnum) {
                    val values = Arrays.asList(*field.type.getEnumConstants())
                    val func =
                        Function<Any?, Text> { value: Any? -> Text.translatable(modid + ".midnightconfig." + "enum." + type.getSimpleName() + "." + info.value.toString()) }
                    info.widget = AbstractMap.SimpleEntry<ButtonWidget.PressAction, Function<Any?, Text>>(
                        ButtonWidget.PressAction { button: ButtonWidget ->
                            val index = values.indexOf(info.value) + 1
                            info.value = values[if (index >= values.size) 0 else index]
                            button.message = func.apply(info.value)
                        }, func
                    )
                }
            }
            entries.add(info)
        }

        private fun textField(info: EntryInfo, f: Function<String, Number>, pattern: Pattern?, min: Double, max: Double, cast: Boolean) {
            val isNumber = pattern != null
            info.widget = BiFunction<TextFieldWidget, ButtonWidget, Predicate<String>> { t: TextFieldWidget, b: ButtonWidget ->
                Predicate<String> { s: String ->
                    var s = s
                    s = s.trim { it <= ' ' }
                    if (!(s.isEmpty() || !isNumber || pattern!!.matcher(s).matches())) return@Predicate false
                    var value: Number = 0
                    var inLimits = false
                    info.error = null
                    if (!(isNumber && s.isEmpty()) && s != "-" && s != ".") {
                        value = f.apply(s)
                        inLimits = value.toDouble() >= min && value.toDouble() <= max
                        info.error = if (inLimits) null else AbstractMap.SimpleEntry<TextFieldWidget, Text>(
                            t,
                            Text.literal(if (value.toDouble() < min) "§cMinimum " + (if (isNumber) "value" else "length") + (if (cast) " is " + min.toInt() else " is $min") else "§cMaximum " + (if (isNumber) "value" else "length") + if (cast) " is " + max.toInt() else " is $max")
                        )
                    }
                    info.tempValue = s
                    t.setEditableColor(if (inLimits) -0x1 else -0x8889)
                    info.inLimits = inLimits
                    b.active = entries.stream().allMatch { e: EntryInfo -> e.inLimits }
                    if (inLimits && info.field!!.type != MutableList::class.java) info.value = if (isNumber) value else s else if (inLimits) {
                        if ((info.value as List<String?>?)!!.size == info.index) (info.value as MutableList<String?>?)!!.add("")
                        (info.value as MutableList<String?>?)!![info.index] =
                            Arrays.stream(info.tempValue!!.replace("[", "").replace("]", "").split(", ".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()).toList()[0]
                    }
                    if (info.field!!.getAnnotation<Entry>(Entry::class.java).isColor) {
                        if (!s.contains("#")) s = "#$s"
                        if (!HEXADECIMAL_ONLY.matcher(s).matches()) return@Predicate false
                        try {
                            info.colorButton!!.message = Text.literal("⬛").setStyle(Style.EMPTY.withColor(Color.decode(info.tempValue).rgb))
                        } catch (ignored: Exception) {
                        }
                    }
                    true
                }
            }
        }

        fun write(modid: String) {
            path = configDir().resolve("$modid.json")
            try {
                if (!Files.exists(path)) Files.createFile(path)
                Files.write(
                    path, gson.toJson(
                        configClass[modid]!!.getDeclaredConstructor().newInstance()
                    ).toByteArray()
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun getScreen(parent: Screen, modid: String): Screen {
            return MidnightConfigScreen(parent, modid)
        }
    }
}