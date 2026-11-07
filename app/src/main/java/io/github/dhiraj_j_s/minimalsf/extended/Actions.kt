package io.github.dhiraj_j_s.minimalsf.extended

/**
 * What MinimalSF can ask for, and the exact `am` / `pm` command for each.
 *
 * The request (an Intent to [ActionActivity]) carries:
 *   action     one of the constants below
 *   package    the app's package name
 *   component  "package/Class" of the activity to open (freeform only)
 *   user       Android user id (0 = main, others = work profile, dual apps...)
 *   label      the app's name, for messages
 */
object Actions {
    const val INTENT_ACTION = "io.github.dhiraj_j_s.minimalsf.extended.ACTION"

    const val FORCE_STOP = "force_stop"
    const val CLEAR_DATA = "clear_data"
    const val CLEAR_CACHE = "clear_cache"
    const val FREEZE = "freeze"
    const val UNFREEZE = "unfreeze"
    const val FREEFORM = "freeform"

    private val PACKAGE = Regex("^[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+$")
    private val CLASS = Regex("^[A-Za-z0-9_.$]+$")

    fun validPackage(pkg: String?) = pkg != null && pkg.length <= 255 && PACKAGE.matches(pkg)

    /** Apps that must never be stopped, cleared or frozen from here: the system, Shizuku and MinimalSF itself. */
    private fun isProtected(pkg: String) = pkg == "android" || pkg == "com.android.systemui" ||
        pkg == "com.android.settings" || pkg == "com.android.phone" ||
        pkg == "moe.shizuku.privileged.api" || pkg.startsWith("io.github.dhiraj_j_s.minimalsf")

    /** Why [action] can't run on [pkg], or null if it can. */
    fun refusal(action: String, pkg: String): String? = when {
        action != FREEFORM && action != UNFREEZE && isProtected(pkg) -> "not allowed for this app"
        else -> null
    }

    /** The command for [action], or null if the request is invalid. Clear cache has no command (see ShellService). */
    fun argv(action: String, pkg: String, component: String?, user: Int): Array<String>? {
        if (!validPackage(pkg) || user < 0) return null
        val u = user.toString()
        return when (action) {
            FORCE_STOP -> arrayOf("am", "force-stop", "--user", u, pkg)
            CLEAR_DATA -> arrayOf("pm", "clear", "--user", u, pkg)
            FREEZE -> arrayOf("pm", "disable-user", "--user", u, pkg)
            UNFREEZE -> arrayOf("pm", "enable", "--user", u, pkg)
            FREEFORM -> {
                val cls = component?.substringAfter('/', "") ?: return null
                if (component.substringBefore('/') != pkg || !CLASS.matches(cls)) return null
                // Windowing mode 5 = freeform
                arrayOf("am", "start", "--user", u, "--windowingMode", "5", "-n", "$pkg/$cls")
            }
            else -> null
        }
    }
}
