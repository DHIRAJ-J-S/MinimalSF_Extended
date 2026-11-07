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
    /** Shown alone when neither Shizuku nor root mode is available: opens the plugin's setup screen. */
    const val SETUP = "setup"

    /**
     * One entry in MinimalSF's "extended…" menu. MinimalSF reads these from [ActionsProvider] each time
     * it opens the menu, so a new action only needs a plugin update. [icon] is a drawable name in this
     * app; [confirm] (title, message, button) asks before running, with {app} replaced by the app's name.
     */
    class Def(val id: String, val label: String, val icon: String, val confirm: Triple<String, String, String>? = null,
              val rootOnly: Boolean = false, val needsShizuku: Boolean = true)

    /** The menu, in order. Add new actions here (and their command in [argv]). */
    val MENU = listOf(
        Def(FREEFORM, "open in freeform", "ic_window"),
        Def(FORCE_STOP, "force stop", "ic_stop"),
        Def(CLEAR_CACHE, "clear cache", "ic_sweep", rootOnly = true),
        Def(CLEAR_DATA, "clear data", "ic_clear_data", Triple("clear data?",
            "{app} will be reset like a fresh install: its settings, accounts and files are deleted. this can't be undone.",
            "clear data")),
        Def(FREEZE, "freeze", "ic_freeze", Triple("freeze {app}?",
            "it stops running and disappears from the launcher until you unfreeze it in the MinimalSF Extended app.",
            "freeze"))
    )

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

    /**
     * True only for the exact command shapes [argv] builds, with checked values. The privileged side
     * (ShellService in Shizuku's process, and Root) checks every command against this, so even code
     * that somehow reached it could only force stop, clear, freeze / unfreeze or open an app.
     */
    fun isAllowedCommand(argv: Array<String>): Boolean {
        val a = argv.toList()
        fun user(i: Int) = a.getOrNull(i)?.toIntOrNull()?.let { it >= 0 && it.toString() == a[i] } == true
        fun pkg(i: Int) = validPackage(a.getOrNull(i))
        return when {
            a.size == 5 && a[0] == "am" && a[1] == "force-stop" && a[2] == "--user" && user(3) && pkg(4) -> true
            a.size == 5 && a[0] == "pm" && a[1] in setOf("clear", "disable-user", "enable") && a[2] == "--user" && user(3) && pkg(4) -> true
            a.size == 8 && a[0] == "am" && a[1] == "start" && a[2] == "--user" && user(3) && a[4] == "--windowingMode" &&
                a[5] == "5" && a[6] == "-n" -> {
                val (p, cls) = a[7].split('/', limit = 2).let { it[0] to it.getOrElse(1) { "" } }
                validPackage(p) && CLASS.matches(cls) && cls.isNotEmpty()
            }
            else -> false
        }
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
