package io.github.dhiraj_j_s.minimalsf.extended

import android.content.Context
import android.os.Process
import java.io.File
import kotlin.system.exitProcess

/**
 * Started by Shizuku in its own process, with shell rights (or root when Shizuku runs as root).
 * It only runs `am` and `pm`, with arguments passed straight through (no shell), and only after
 * [Actions] has checked them, so nothing in a package name can turn into an extra command.
 */
class ShellService() : IShellService.Stub() {

    // Shizuku uses this constructor when it can pass a Context
    @Suppress("unused")
    constructor(context: Context) : this()

    override fun destroy() {
        exitProcess(0)
    }

    override fun run(argv: Array<String>): String {
        if (argv.isEmpty() || argv[0] !in ALLOWED) return "1\nnot allowed"
        return try {
            val p = ProcessBuilder(*argv).redirectErrorStream(true).start()
            val out = p.inputStream.bufferedReader().use { it.readText() }
            "${p.waitFor()}\n${out.trim()}"
        } catch (e: Exception) {
            "1\n${e.message}"
        }
    }

    override fun clearCache(pkg: String, userId: Int): String {
        if (!Actions.validPackage(pkg) || userId < 0) return "1\ninvalid app"
        // App data folders belong to the app's own user id; the shell user can't touch them
        if (Process.myUid() != 0) return "2\nneeds root"
        var freed = 0L
        listOf("/data/user/$userId/$pkg/cache", "/data/user/$userId/$pkg/code_cache",
            "/data/media/$userId/Android/data/$pkg/cache").forEach { dir ->
            File(dir).listFiles()?.forEach { f ->
                freed += f.walkBottomUp().filter { it.isFile }.sumOf { it.length() }
                f.deleteRecursively()
            }
        }
        return "0\n$freed"
    }

    private companion object {
        val ALLOWED = setOf("am", "pm")
    }
}
