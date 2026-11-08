package io.github.dhiraj_j_s.minimalsf.extended

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

/**
 * Tells MinimalSF which actions to show in its "extended…" menu: content://<authority>/actions, and what
 * goes in the "extended menu" in its settings: content://<authority>/settings (see [Settings]).
 * Only apps holding the EXTENDED signature permission (MinimalSF) can read or write it (see the manifest).
 *
 * Columns: id, label, icon (drawable name in this app), confirm_title, confirm_message, confirm_button
 * (the last three are null when the action runs straight away; {app} is replaced by the app's name),
 * api (the format version, so MinimalSF can tell newer formats apart later).
 *
 * settings columns: id, parent ("" = top level, else a page's id), kind (page / toggle / open), label,
 * icon (drawable name in this app, or null), value (1 / 0 for toggles, null otherwise), api.
 * MinimalSF changes a toggle with update(settings, {id, value}).
 */
class ActionsProvider : ContentProvider() {

    override fun onCreate() = true

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?,
                       selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        if (uri.lastPathSegment == "settings") return settings()
        if (uri.lastPathSegment != "actions") return null
        val cursor = MatrixCursor(COLUMNS)
        // Shizuku (or root mode) is optional: without either, only the actions that don't need it are
        // offered. Clear cache needs root. (Shizuku not yet allowed still lists them: tapping one asks.)
        val ctx = context ?: return cursor
        if (!Guard.intact(ctx)) return cursor
        val state = Shell.state()
        val shizuku = state == Shell.State.READY || state == Shell.State.NO_PERMISSION || Root.enabled(ctx)
        val root = Shell.canClearCache(ctx)
        val usable = Actions.MENU.filter { (!it.needsShizuku || shizuku) && (!it.rootOnly || root) }
        // Each action can be turned off in MinimalSF's settings > extended menu > long press menu
        usable.filter { Settings.actionShown(ctx, it.id) }.forEach { d ->
            cursor.addRow(arrayOf(d.id, d.label, d.icon, d.confirm?.first, d.confirm?.second, d.confirm?.third, API))
        }
        // Nothing works yet: offer the way to set it up instead of an empty menu
        if (usable.isEmpty()) cursor.addRow(arrayOf(Actions.SETUP, "set up Shizuku or root…", "ic_setup", null, null, null, API))
        return cursor
    }

    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        val ctx = context ?: return 0
        if (uri.lastPathSegment != "settings" || values == null || !Guard.intact(ctx)) return 0
        val id = values.getAsString("id") ?: return 0
        val on = values.getAsInteger("value") ?: return 0
        return if (Settings.set(ctx, id, on != 0)) 1 else 0
    }

    private fun settings(): Cursor {
        val cursor = MatrixCursor(SETTINGS_COLUMNS)
        val ctx = context ?: return cursor
        if (!Guard.intact(ctx)) return cursor
        Settings.ROWS.forEach { r ->
            val value = if (r.kind == Settings.TOGGLE) (if (Settings.isOn(ctx, r)) 1 else 0) else null
            cursor.addRow(arrayOf(r.id, r.parent, r.kind, r.label, r.icon, value, API))
        }
        return cursor
    }

    private companion object {
        const val API = 2
        val SETTINGS_COLUMNS = arrayOf("id", "parent", "kind", "label", "icon", "value", "api")
        val COLUMNS = arrayOf("id", "label", "icon", "confirm_title", "confirm_message", "confirm_button", "api")
    }
}
