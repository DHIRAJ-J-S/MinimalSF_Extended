package io.github.dhiraj_j_s.minimalsf.extended

import android.content.Context
import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/** Something that can run the plugin's commands with elevated rights: Shizuku ([Shell]) or root ([Root]). */
interface Backend {
    /** Runs a checked `am` / `pm` command; [done] gets (exit code, output) on the main thread. */
    fun run(argv: Array<String>, done: (Int, String) -> Unit)
    /** Deletes an app's cache; [done] gets (0, bytes freed), (2, _) without root, or an error. */
    fun clearCache(pkg: String, user: Int, done: (Int, String) -> Unit)
}

/**
 * Root mode, for rooted phones without Shizuku (Magisk, KernelSU, APatch...): the same commands,
 * run through `su`. It's off until turned on in the plugin, so `su` is never asked for by surprise.
 * Shizuku is still used first whenever it's running.
 */
object Root : Backend {

    private val main = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()

    private fun prefs(c: Context) = c.getSharedPreferences("root", Context.MODE_PRIVATE)
    fun enabled(c: Context) = prefs(c).getBoolean("enabled", false)
    fun setEnabled(c: Context, on: Boolean) = prefs(c).edit().putBoolean("enabled", on).apply()

    // `su -c` takes one shell line, so every argument is single-quoted. Arguments are already checked
    // by Actions (letters, digits, dots, _ and $), and a quote inside one is escaped anyway.
    private fun quote(arg: String) = "'" + arg.replace("'", "'\\''") + "'"

    private fun su(line: String): Pair<Int, String> = try {
        val p = ProcessBuilder("su", "-c", line).redirectErrorStream(true).start()
        val out = p.inputStream.bufferedReader().use { it.readText() }
        if (!p.waitFor(20, TimeUnit.SECONDS)) { p.destroy(); -1 to "timed out" } else p.exitValue() to out.trim()
    } catch (e: Exception) {
        -1 to (e.message ?: "no su")
    }

    private fun async(work: () -> Pair<Int, String>, done: (Int, String) -> Unit) = executor.execute {
        val (code, out) = work()
        main.post { done(code, out) }
    }

    /** Asks for root (the root app shows its prompt) and checks it really is root. */
    fun check(done: (Boolean) -> Unit) = async({ su("id -u") }) { code, out -> done(code == 0 && out.trim() == "0") }

    override fun run(argv: Array<String>, done: (Int, String) -> Unit) {
        if (!Actions.isAllowedCommand(argv)) { done(1, "not allowed"); return }
        async({ su(argv.joinToString(" ") { quote(it) }) }, done)
    }

    override fun clearCache(pkg: String, user: Int, done: (Int, String) -> Unit) {
        if (!Actions.validPackage(pkg) || user < 0) { done(1, "invalid app"); return }
        val dirs = listOf("/data/user/$user/$pkg/cache", "/data/user/$user/$pkg/code_cache",
            "/data/media/$user/Android/data/$pkg/cache").joinToString(" ") { quote(it) }
        // Size first (in KB), then delete what's inside the folders but keep the folders themselves
        async({ su("du -sk $dirs 2>/dev/null; find $dirs -mindepth 1 -delete 2>/dev/null; true") }) { code, out ->
            val kb = out.lines().sumOf { it.substringBefore('\t').trim().toLongOrNull() ?: 0L }
            if (code == 0) done(0, (kb * 1024).toString()) else done(code, out)
        }
    }
}
