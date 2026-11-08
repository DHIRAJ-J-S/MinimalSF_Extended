package io.github.dhiraj_j_s.minimalsf.extended

import android.content.Context

/**
 * The "extended menu" in MinimalSF's settings. MinimalSF draws whatever this lists (through
 * [ActionsProvider]), so new options only need a plugin update. Values are kept here; MinimalSF keeps a
 * copy of them so it never has to wake the plugin just to draw the home screen.
 *
 * Kinds: "page" opens the rows whose parent is its id, "toggle" is an on/off value, "open" opens this app.
 * Toggles whose id MinimalSF doesn't know yet are shown and saved but do nothing in older MinimalSF versions.
 */
object Settings {
    const val PAGE = "page"
    const val TOGGLE = "toggle"
    const val OPEN = "open"

    private const val LONG_PRESS = "long_press"
    /** Prefix of the toggles that show or hide one of this plugin's actions in "extended…". */
    const val ACTION_PREFIX = "action_"

    class Row(val id: String, val parent: String, val kind: String, val label: String,
              val icon: String? = null, val default: Boolean = false)

    /** Every row, in order. Pages first list their own rows, at the point where the page is. */
    val ROWS: List<Row> = buildList {
        add(Row(LONG_PRESS, "", PAGE, "long press menu"))
        // MinimalSF's own long press entries (MinimalSF draws their icons)
        add(Row("menu_shortcuts", LONG_PRESS, TOGGLE, "app shortcuts", default = true))
        add(Row("menu_app_info", LONG_PRESS, TOGGLE, "app info", default = true))
        add(Row("menu_uninstall", LONG_PRESS, TOGGLE, "uninstall", default = true))
        add(Row("menu_keyword", LONG_PRESS, TOGGLE, "set keyword", default = true))
        add(Row("menu_hide", LONG_PRESS, TOGGLE, "hide from search", default = true))
        // This plugin's actions, inside "extended…"
        Actions.MENU.forEach { add(Row(ACTION_PREFIX + it.id, LONG_PRESS, TOGGLE, "extended · " + it.label, it.icon, default = true)) }

        add(Row("hide_search", "", TOGGLE, "hide empty search bar"))
        add(Row("colour_icons", "", TOGGLE, "coloured app icons"))
        add(Row("auto_launch", "", TOGGLE, "auto-launch a single match", default = true))
        add(Row("open_plugin", "", OPEN, "open MinimalSF Extended"))
    }

    private fun prefs(c: Context) = c.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun isOn(c: Context, row: Row) = prefs(c).getBoolean(row.id, row.default)

    /** Saves a toggle; false for an unknown id or a row that isn't a toggle. */
    fun set(c: Context, id: String, on: Boolean): Boolean {
        val row = ROWS.firstOrNull { it.id == id && it.kind == TOGGLE } ?: return false
        prefs(c).edit().putBoolean(row.id, on).apply()
        return true
    }

    /** Whether an action from [Actions.MENU] is shown in "extended…". */
    fun actionShown(c: Context, actionId: String) =
        ROWS.firstOrNull { it.id == ACTION_PREFIX + actionId }?.let { isOn(c, it) } ?: true
}
