package com.example.model

enum class VirtualKeyType {
    TEXT,
    SPECIAL,
    MODIFIER,
    ARROW
}

data class VirtualKey(
    val id: String,
    val label: String,
    val sendValue: String = label,
    val type: VirtualKeyType = VirtualKeyType.TEXT,
    val description: String = label
) {
    companion object {
        val DEFAULT_TOP_KEYS = listOf(
            VirtualKey(id = "esc", label = "ESC", sendValue = "\u001b", type = VirtualKeyType.SPECIAL),
            VirtualKey(id = "tab", label = "TAB", sendValue = "\t", type = VirtualKeyType.SPECIAL),
            VirtualKey(id = "ctrl", label = "CTRL", sendValue = "", type = VirtualKeyType.MODIFIER),
            VirtualKey(id = "alt", label = "ALT", sendValue = "", type = VirtualKeyType.MODIFIER),
            VirtualKey(id = "up", label = "▲", sendValue = "\u001b[A", type = VirtualKeyType.ARROW),
            VirtualKey(id = "down", label = "▼", sendValue = "\u001b[B", type = VirtualKeyType.ARROW),
            VirtualKey(id = "left", label = "◀", sendValue = "\u001b[D", type = VirtualKeyType.ARROW),
            VirtualKey(id = "right", label = "▶", sendValue = "\u001b[C", type = VirtualKeyType.ARROW),
            VirtualKey(id = "pipe", label = "|", sendValue = "|"),
            VirtualKey(id = "slash", label = "/", sendValue = "/"),
            VirtualKey(id = "dash", label = "-", sendValue = "-"),
            VirtualKey(id = "tilde", label = "~", sendValue = "~")
        )

        val EXTENDED_KEYS = listOf(
            VirtualKey(id = "home", label = "HOME", sendValue = "\u001b[H", type = VirtualKeyType.SPECIAL),
            VirtualKey(id = "end", label = "END", sendValue = "\u001b[F", type = VirtualKeyType.SPECIAL),
            VirtualKey(id = "pgup", label = "PGUP", sendValue = "\u001b[5~", type = VirtualKeyType.SPECIAL),
            VirtualKey(id = "pgdn", label = "PGDN", sendValue = "\u001b[6~", type = VirtualKeyType.SPECIAL),
            VirtualKey(id = "dollar", label = "$", sendValue = "$"),
            VirtualKey(id = "amp", label = "&", sendValue = "&"),
            VirtualKey(id = "semi", label = ";", sendValue = ";"),
            VirtualKey(id = "quote", label = "\"", sendValue = "\""),
            VirtualKey(id = "squote", label = "'", sendValue = "'"),
            VirtualKey(id = "bslash", label = "\\", sendValue = "\\"),
            VirtualKey(id = "underscore", label = "_", sendValue = "_"),
            VirtualKey(id = "asterisk", label = "*", sendValue = "*"),
            VirtualKey(id = "question", label = "?", sendValue = "?"),
            VirtualKey(id = "gt", label = ">", sendValue = ">"),
            VirtualKey(id = "lt", label = "<", sendValue = "<"),
            VirtualKey(id = "equal", label = "=", sendValue = "="),
            VirtualKey(id = "c_c", label = "^C", sendValue = "\u0003", type = VirtualKeyType.SPECIAL, description = "SIGINT (Ctrl+C)"),
            VirtualKey(id = "c_d", label = "^D", sendValue = "\u0004", type = VirtualKeyType.SPECIAL, description = "EOF (Ctrl+D)"),
            VirtualKey(id = "c_z", label = "^Z", sendValue = "\u001a", type = VirtualKeyType.SPECIAL, description = "Suspend (Ctrl+Z)"),
            VirtualKey(id = "c_l", label = "^L", sendValue = "\u000c", type = VirtualKeyType.SPECIAL, description = "Clear (Ctrl+L)")
        )
    }
}
