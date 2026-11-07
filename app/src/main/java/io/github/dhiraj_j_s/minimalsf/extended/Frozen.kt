package io.github.dhiraj_j_s.minimalsf.extended

import android.content.Context

/**
 * Apps frozen from here, so they can be unfrozen again: a frozen app disappears from the launcher,
 * so this list is the only place to bring it back from. Stored as "user|package|label" lines.
 */
object Frozen {
    data class Entry(val user: Int, val pkg: String, val label: String)

    private fun prefs(c: Context) = c.getSharedPreferences("frozen", Context.MODE_PRIVATE)

    fun list(c: Context): List<Entry> = (prefs(c).getString("apps", "") ?: "").lines().mapNotNull { line ->
        val p = line.split('|', limit = 3)
        val user = p.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
        val pkg = p.getOrNull(1)?.takeIf { Actions.validPackage(it) } ?: return@mapNotNull null
        Entry(user, pkg, p.getOrNull(2) ?: pkg)
    }

    private fun save(c: Context, entries: List<Entry>) = prefs(c).edit()
        .putString("apps", entries.joinToString("\n") { "${it.user}|${it.pkg}|${it.label}" }).apply()

    fun add(c: Context, pkg: String, user: Int, label: String) {
        // Labels come from MinimalSF; keep them to one line so they can't add entries
        val clean = label.replace(Regex("[\\r\\n|]"), " ").take(60)
        save(c, list(c).filterNot { it.pkg == pkg && it.user == user } + Entry(user, pkg, clean))
    }

    fun remove(c: Context, pkg: String, user: Int) = save(c, list(c).filterNot { it.pkg == pkg && it.user == user })
}
