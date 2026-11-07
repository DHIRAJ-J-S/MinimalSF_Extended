package io.github.dhiraj_j_s.minimalsf.extended

import android.content.Context
import android.os.Process
import kotlin.system.exitProcess

/**
 * Started by Shizuku in its own process, with shell rights (or root when Shizuku runs as root).
 * It only runs the exact `am` / `pm` commands [Actions.isAllowedCommand] allows, with arguments
 * passed straight through (no shell), so nothing in a package name can turn into an extra command.
 */
class ShellService() : IShellService.Stub() {

    // Shizuku uses this constructor when it can pass a Context
    @Suppress("unused")
    constructor(context: Context) : this()

    override fun destroy() {
        exitProcess(0)
    }

    override fun run(argv: Array<String>): String {
        if (!Actions.isAllowedCommand(argv)) return "1\nnot allowed"
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
        val dirs = listOf("/data/user/$userId/$pkg/cache", "/data/user/$userId/$pkg/code_cache",
            "/data/media/$userId/Android/data/$pkg/cache")
        // Running as root, so never follow symlinks: an app could link its cache folder (or something in
        // it) to system files. du and find don't follow links by default, and a cache folder that is
        // itself a link is left alone. Java's File walking would follow them.
        // nsenter: work in the system-wide view of storage. A process started for an app may only see that
        // app's own data folders (app data isolation); harmless when the view is already system-wide.
        val ns = listOf("nsenter", "-t", "1", "-m", "--")
        val kb = exec(ns + listOf("du", "-sk") + dirs).lines().sumOf { it.substringBefore('\t').trim().toLongOrNull() ?: 0L }
        dirs.forEach { exec(ns + listOf("find", it, "-mindepth", "1", "-delete")) }
        return "0\n${kb * 1024}"
    }

    private fun exec(argv: List<String>): String = try {
        val p = ProcessBuilder(argv).redirectErrorStream(true).start()
        p.inputStream.bufferedReader().use { it.readText() }.also { p.waitFor() }
    } catch (_: Exception) { "" }
}
