package com.views.simplekeyboard

import android.annotation.SuppressLint
import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button


private enum class KeyTypes(val value: String) {
    LOAD_MAIN_KEYBOARD("LOAD_MAIN_KEYBOARD"),
    LOAD_SYMBOLS_KEYBOARD("LOAD_SYMBOLS_KEYBOARD"),
    CTRL("CTRL"),
    ALT("ALT"),
    SHIFT("SHIFT")
}

private const val TAG = "SimpleKeyboard"


class SimpleKeyboard: InputMethodService() {

    private lateinit var mainKeyboardView: View
    private lateinit var symbolsKeyboardView: View
    private lateinit var currentKeyboardView: View

    private val ctrlMask: Int = KeyEvent.META_CTRL_MASK
    private val altMask: Int = KeyEvent.META_ALT_MASK
    private val shiftMask: Int = KeyEvent.META_SHIFT_MASK
    private val doubleTapMaxDelay: Long = 200

    private var metaState: Int = 0
    private var ctrlPressed: Boolean = false
    private var altPressed: Boolean = false
    private var shiftPressed: Boolean = false

    private var ctrlLocked: Boolean = false
    private var altLocked: Boolean = false
    private var shiftLocked: Boolean = false

    var lastMetaKeyPressedInfo: HashMap<String?, Any?> = HashMap<String?, Any?>()

    @SuppressLint("InflateParams")
    override fun onCreateInputView(): View? {
        mainKeyboardView = layoutInflater.inflate(R.layout.keyboard_view, null)
        symbolsKeyboardView = layoutInflater.inflate(R.layout.symbols_keyboard_view, null)
        currentKeyboardView = mainKeyboardView
        return currentKeyboardView
    }

    fun onKeyClick(keyView: View) {
        val pressedKey = keyView as Button
        val inputConnection = getCurrentInputConnection()
        if (inputConnection == null) return

        val now = System.currentTimeMillis()
        var keyType = pressedKey.tag as String

        if (keyType.contains("SHIFT_")) {
            shiftPressed = !shiftPressed
            keyType = keyType.substring(
                6,
                keyType.length
            )
        }

        when(keyType) {
            KeyTypes.LOAD_MAIN_KEYBOARD.value -> {
                setInputView(mainKeyboardView)
                currentKeyboardView = mainKeyboardView
                mainKeyboardView.findViewWithTag<Button>("CTRL").isActivated = ctrlPressed
                mainKeyboardView.findViewWithTag<Button>("ALT").isActivated = altPressed
                mainKeyboardView.findViewWithTag<Button>("SHIFT").isActivated = shiftPressed
            }

            KeyTypes.LOAD_SYMBOLS_KEYBOARD.value -> {
                setInputView(symbolsKeyboardView)
                currentKeyboardView = symbolsKeyboardView
                symbolsKeyboardView.findViewWithTag<Button>("CTRL").isActivated = ctrlPressed
                symbolsKeyboardView.findViewWithTag<Button>("ALT").isActivated = altPressed
                symbolsKeyboardView.findViewWithTag<Button>("SHIFT").isActivated = shiftPressed
            }

            KeyTypes.CTRL.value -> {
                if (ctrlLocked) {
                    ctrlLocked = false
                    ctrlPressed = false
                    pressedKey.isActivated = false
                } else {
                    ctrlPressed = !ctrlPressed
                    pressedKey.isActivated = ctrlPressed
                }

                if (lastMetaKeyPressedInfo.get("Key") === keyType && ((now - lastMetaKeyPressedInfo.get(
                        "Time"
                    ).toString().toLong()) < doubleTapMaxDelay )
                ) {
                    ctrlLocked = true
                    ctrlPressed = true
                    pressedKey.isActivated = true
                }

                lastMetaKeyPressedInfo.put("Key", keyType)
                lastMetaKeyPressedInfo.put("Time", now)
            }

            KeyTypes.ALT.value -> {
                if (altLocked) {
                    altLocked = false
                    altPressed = false
                    pressedKey.isActivated = false
                } else {
                    altPressed = !altPressed
                    pressedKey.isActivated = ctrlPressed
                }

                if (lastMetaKeyPressedInfo.get("Key") === keyType && ((now - lastMetaKeyPressedInfo.get(
                        "Time"
                    ).toString().toLong()) < doubleTapMaxDelay )
                ) {
                    altLocked = true
                    altPressed = true
                    pressedKey.isActivated = true
                }

                lastMetaKeyPressedInfo.put("Key", keyType)
                lastMetaKeyPressedInfo.put("Time", now)
            }

            KeyTypes.SHIFT.value -> {
                if (shiftLocked) {
                    shiftLocked = false
                    shiftPressed = false
                    pressedKey.isActivated = false
                } else {
                    shiftPressed = !shiftPressed
                    pressedKey.isActivated = ctrlPressed
                }

                if (lastMetaKeyPressedInfo.get("Key") === keyType && ((now - lastMetaKeyPressedInfo.get(
                        "Time"
                    ).toString().toLong()) < doubleTapMaxDelay )
                ) {
                    shiftLocked = true
                    shiftPressed = true
                    pressedKey.isActivated = true
                }

                lastMetaKeyPressedInfo.put("Key", keyType)
                lastMetaKeyPressedInfo.put("Time", now)
            }

            else -> {
                updateMetaState()
                try {
                    inputConnection.sendKeyEvent(
                        KeyEvent(
                            now,
                            now,
                            KeyEvent.ACTION_DOWN,
                            KeyEvent::class.java.getField("KEYCODE_$keyType").getInt(null),
                            0,
                            metaState
                        )
                    )
                } catch (e: IllegalAccessException) {
                    Log.i(TAG, "onKeyClick: IllegalAccessException found: $e")
                } catch (e: NoSuchFieldException) {
                    Log.i(TAG, "onKeyClick: NoSuchFieldException found: $e")
                }

                if (!ctrlLocked && ctrlPressed) {
                    ctrlPressed = false
                    currentKeyboardView.findViewWithTag<Button>("CTRL").isActivated = false
                }
                if (!altLocked && altPressed) {
                    altPressed = false
                    currentKeyboardView.findViewWithTag<Button>("ALT").isActivated = false
                }
                if (!shiftLocked && shiftPressed) {
                    shiftPressed = false
                    currentKeyboardView.findViewWithTag<Button>("SHIFT").isActivated = false
                }
            }
        }
    }

    private fun updateMetaState() {
        val metaSequence =
            boolToIntString(ctrlPressed) +
                    boolToIntString(altPressed) +
                    boolToIntString(shiftPressed)

        metaState = when (metaSequence) {
            "000" -> 0
            "100" -> ctrlMask
            "010" -> altMask
            "001" -> shiftMask
            "110" -> ctrlMask or altMask
            "101" -> ctrlMask or shiftMask
            "011" -> altMask or shiftMask
            "111" -> ctrlMask or altMask or shiftMask
            else -> 0
        }
    }

    private fun boolToIntString(bool: Boolean): String {
        return if(bool) "1" else "0"
    }
}