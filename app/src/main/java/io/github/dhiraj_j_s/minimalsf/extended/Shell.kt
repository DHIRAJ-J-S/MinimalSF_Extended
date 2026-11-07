package io.github.dhiraj_j_s.minimalsf.extended

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import rikka.shizuku.Shizuku
import java.util.concurrent.Executors

/** Shizuku state, and the shell service it runs for us. */
object Shell : Backend {

    enum class State { NOT_RUNNING, OUTDATED, NO_PERMISSION, READY }

    fun state(): State = when {
        !Shizuku.pingBinder() -> State.NOT_RUNNING
        Shizuku.isPreV11() -> State.OUTDATED
        Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED -> State.NO_PERMISSION
        else -> State.READY
    }

    /** True when Shizuku was started with root (needed for clear cache). */
    fun isRoot(): Boolean = try { Shizuku.getUid() == 0 } catch (_: Exception) { false }

    private val main = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()
    private var service: IShellService? = null
    private val waiting = mutableListOf<(IShellService?) -> Unit>()

    private val args by lazy {
        Shizuku.UserServiceArgs(ComponentName(BuildConfig.APPLICATION_ID, ShellService::class.java.name))
            .daemon(false)
            .processNameSuffix("shell")
            .debuggable(BuildConfig.DEBUG)
            .version(BuildConfig.VERSION_CODE)
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = if (binder != null && binder.pingBinder()) IShellService.Stub.asInterface(binder) else null
            flush()
        }
        override fun onServiceDisconnected(name: ComponentName?) { service = null }
    }

    private fun flush() {
        val callbacks = waiting.toList(); waiting.clear()
        callbacks.forEach { it(service) }
    }

    /** Hands the shell service to [onReady] on the main thread, or null if it couldn't be started. */
    private fun withService(onReady: (IShellService?) -> Unit) {
        service?.takeIf { it.asBinder().pingBinder() }?.let { onReady(it); return }
        service = null
        waiting += onReady
        if (waiting.size > 1) return
        try { Shizuku.bindUserService(args, connection) } catch (_: Exception) { flush(); return }
        // The first start spawns a process; give up if it never answers
        main.postDelayed({ if (waiting.isNotEmpty()) flush() }, 10_000)
    }

    /** Runs a call on the service off the main thread; [done] gets (exit code, output) on the main thread. */
    private fun call(block: (IShellService) -> String, done: (Int, String) -> Unit) = withService { s ->
        if (s == null) { done(-1, "couldn't start the shell service"); return@withService }
        executor.execute {
            val raw = try { block(s) } catch (e: Exception) { "-1\n${e.message}" }
            val code = raw.substringBefore('\n').toIntOrNull() ?: -1
            val out = raw.substringAfter('\n', "")
            main.post { done(code, out) }
        }
    }

    override fun run(argv: Array<String>, done: (Int, String) -> Unit) { call({ it.run(argv) }, done) }

    override fun clearCache(pkg: String, user: Int, done: (Int, String) -> Unit) { call({ it.clearCache(pkg, user) }, done) }

    /** Who runs commands right now: Shizuku when it's running and allowed, else root mode if it's on. */
    fun backend(c: android.content.Context): Backend? = when {
        state() == State.READY -> this
        Root.enabled(c) -> Root
        else -> null
    }

    /** True when clear cache can work: Shizuku started with root, or root mode. */
    fun canClearCache(c: android.content.Context) = (state() == State.READY && isRoot()) || (state() != State.READY && Root.enabled(c))
}
