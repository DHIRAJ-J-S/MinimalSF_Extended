package io.github.dhiraj_j_s.minimalsf.extended

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

/**
 * Tells MinimalSF which actions to show in its "extended…" menu: content://<authority>/actions.
 * Only apps holding the EXTENDED signature permission (MinimalSF) can read it (see the manifest).
 *
 * Columns: id, label, icon (drawable name in this app), confirm_title, confirm_message, confirm_button
 * (the last three are null when the action runs straight away; {app} is replaced by the app's name),
 * api (the format version, so MinimalSF can tell newer formats apart later).
 */
class ActionsProvider : ContentProvider() {

    override fun onCreate() = true

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?,
                       selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        if (uri.lastPathSegment != "actions") return null
        val cursor = MatrixCursor(COLUMNS)
        // Shizuku (or root mode) is optional: without either, only the actions that don't need it are
        // offered. Clear cache needs root. (Shizuku not yet allowed still lists them: tapping one asks.)
        val ctx = context ?: return cursor
        if (!Guard.intact(ctx)) return cursor
        val state = Shell.state()
        val shizuku = state == Shell.State.READY || state == Shell.State.NO_PERMISSION || Root.enabled(ctx)
        val root = Shell.canClearCache(ctx)
        Actions.MENU.filter { (!it.needsShizuku || shizuku) && (!it.rootOnly || root) }.forEach { d ->
            cursor.addRow(arrayOf(d.id, d.label, d.icon, d.confirm?.first, d.confirm?.second, d.confirm?.third, API))
        }
        return cursor
    }

    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) = 0

    private companion object {
        const val API = 1
        val COLUMNS = arrayOf("id", "label", "icon", "confirm_title", "confirm_message", "confirm_button", "api")
    }
}
