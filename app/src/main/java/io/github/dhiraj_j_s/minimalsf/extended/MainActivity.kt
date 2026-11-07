package io.github.dhiraj_j_s.minimalsf.extended

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import rikka.shizuku.Shizuku

/**
 * The plugin's own screen: whether Shizuku is ready (and a button to fix it if not), whether
 * MinimalSF is installed, how to use it, and the apps frozen from here, with a way to unfreeze them.
 */
class MainActivity : Activity() {

    private lateinit var content: LinearLayout

    private val onBinder = Shizuku.OnBinderReceivedListener { runOnUiThread { render() } }
    private val onBinderDead = Shizuku.OnBinderDeadListener { runOnUiThread { render() } }
    private val onPermission = Shizuku.OnRequestPermissionResultListener { _, _ -> runOnUiThread { render() } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(48), dp(20), dp(32))
        }
        setContentView(ScrollView(this).apply { setBackgroundColor(Color.BLACK); addView(content) })
        Shizuku.addBinderReceivedListenerSticky(onBinder)
        Shizuku.addBinderDeadListener(onBinderDead)
        Shizuku.addRequestPermissionResultListener(onPermission)
    }

    override fun onResume() { super.onResume(); render() }

    override fun onDestroy() {
        Shizuku.removeBinderReceivedListener(onBinder)
        Shizuku.removeBinderDeadListener(onBinderDead)
        Shizuku.removeRequestPermissionResultListener(onPermission)
        super.onDestroy()
    }

    private fun render() {
        content.removeAllViews()
        text("MinimalSF Extended", 22f, Color.WHITE, bold = true)
        text("extra long press actions for MinimalSF", 13f, GREY, top = 4)

        section("shizuku")
        when (Shell.state()) {
            Shell.State.NOT_RUNNING -> {
                // Any Shizuku works: the official app, a fork, or Sui. Only the button needs a known app.
                text("not running - start Shizuku (or a fork), or use Sui. on a rooted phone you can use root mode below instead.", 14f, RED)
                if (shizukuInstalled()) button("open Shizuku") { openShizuku() }
            }
            Shell.State.OUTDATED -> text("too old - update Shizuku", 14f, RED)
            Shell.State.NO_PERMISSION -> {
                text("running, but MinimalSF Extended isn't allowed yet", 14f, RED)
                button("allow") { Shizuku.requestPermission(1) }
            }
            Shell.State.READY -> text(if (Shell.isRoot()) "ready (root) - all actions work" else "ready (shell) - everything except clear cache, which needs root", 14f, GREEN)
        }

        section("root mode")
        val rootOn = Root.enabled(this)
        text(if (rootOn) "on - root is used whenever Shizuku isn't running" else "off - for rooted phones without Shizuku (Magisk, KernelSU, APatch...)", 14f, if (rootOn) GREEN else TEXT)
        button(if (rootOn) "turn off" else "turn on") {
            if (rootOn) { Root.setEnabled(this, false); render() }
            // Your root app asks first; root mode only turns on if it really got root
            else Root.check { ok ->
                if (ok) Root.setEnabled(this, true) else Toast.makeText(this, "no root access", Toast.LENGTH_SHORT).show()
                render()
            }
        }

        section("MinimalSF")
        val launcher = installed(LAUNCHER)
        text(if (launcher) "installed - long press an app, then \"extended…\"" else "not installed - this plugin only works with the MinimalSF launcher", 14f, if (launcher) GREEN else RED)

        section("actions")
        text("all need Shizuku or root mode", 12f, GREY, top = 2)
        listOf(
            "open in freeform" to "open the app in a floating window",
            "force stop" to "close the app completely",
            "clear data" to "reset the app, like a fresh install (asks first)",
            "clear cache" to "delete the app's temporary files (root, or Shizuku started with root)",
            "freeze" to "disable the app without uninstalling it (files and data are kept)"
        ).forEach { (name, what) -> text("$name - $what", 13f, TEXT, top = 6) }

        section("frozen apps")
        val frozen = Frozen.list(this)
        if (frozen.isEmpty()) text("none - apps you freeze show up here, since they disappear from the launcher", 13f, GREY)
        frozen.forEach { e ->
            row(e.label + if (e.user != 0) " · user ${e.user}" else "", "unfreeze") { unfreeze(e) }
        }
    }

    private fun unfreeze(e: Frozen.Entry) {
        val backend = Shell.backend(this) ?: run { Toast.makeText(this, "start Shizuku or turn on root mode first", Toast.LENGTH_SHORT).show(); return }
        val argv = Actions.argv(Actions.UNFREEZE, e.pkg, null, e.user) ?: return
        backend.run(argv) { code, out ->
            if (code == 0 && !out.contains("Error")) {
                Frozen.remove(this, e.pkg, e.user)
                Toast.makeText(this, "unfroze ${e.label}", Toast.LENGTH_SHORT).show()
            } else if (!installed(e.pkg)) {
                Frozen.remove(this, e.pkg, e.user) // uninstalled since
            } else Toast.makeText(this, "failed: $out", Toast.LENGTH_SHORT).show()
            render()
        }
    }

    private fun installed(pkg: String) = try { packageManager.getApplicationInfo(pkg, PackageManager.MATCH_DISABLED_COMPONENTS); true } catch (_: Exception) { false }
    private fun shizukuInstalled() = installed(SHIZUKU)

    private fun openShizuku() {
        packageManager.getLaunchIntentForPackage(SHIZUKU)?.let { startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    // --- Tiny view helpers, in MinimalSF's black and monospace style ---

    private fun dp(v: Int) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()

    private fun text(s: String, sp: Float, color: Int, bold: Boolean = false, top: Int = 0) = TextView(this).apply {
        text = s; textSize = sp; setTextColor(color)
        typeface = Typeface.create(MONO, if (bold) Typeface.BOLD else Typeface.NORMAL)
        setPadding(0, dp(top), 0, 0); setLineSpacing(dp(3).toFloat(), 1f)
    }.also { content.addView(it) }

    private fun section(title: String) {
        content.addView(View(this).apply {
            setBackgroundColor(DIVIDER)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1)).apply { topMargin = dp(24) }
        })
        text(title, 11f, GREY, top = 12).apply { letterSpacing = 0.08f; setPadding(0, dp(12), 0, dp(8)) }
    }

    private fun button(label: String, onClick: () -> Unit) = TextView(this).apply {
        text = label; textSize = 13f; setTextColor(Color.WHITE); typeface = MONO; gravity = Gravity.CENTER
        setPadding(dp(16), dp(10), dp(16), dp(10))
        background = GradientDrawable().apply { setColor(Color.BLACK); setStroke(dp(1), Color.WHITE) }
        isClickable = true; isFocusable = true
        // Ignore taps while another app draws over this screen, so it can't trick you into
        // turning on root mode or allowing Shizuku (tapjacking)
        filterTouchesWhenObscured = true
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(12) }
    }.also { content.addView(it) }

    private fun row(label: String, action: String, onClick: () -> Unit) {
        content.addView(LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(8), 0, dp(8))
            addView(TextView(context).apply {
                text = label; textSize = 14f; setTextColor(TEXT); typeface = MONO
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            addView(TextView(context).apply {
                text = action; textSize = 13f; setTextColor(Color.WHITE); typeface = MONO
                setPadding(dp(12), dp(6), dp(12), dp(6))
                background = GradientDrawable().apply { setColor(Color.BLACK); setStroke(dp(1), GREY) }
                filterTouchesWhenObscured = true
                setOnClickListener { onClick() }
            })
        })
    }

    private companion object {
        /**
         * The same mono font MinimalSF uses, loaded from its file: some ROMs (Samsung font styles,
         * EvolutionX and other custom font overlays) point the system "monospace" at a sans font.
         */
        val MONO: Typeface by lazy {
            listOf("DroidSansMono.ttf", "CutiveMono.ttf").firstNotNullOfOrNull { name ->
                val f = java.io.File("/system/fonts", name)
                if (!f.exists()) null else try { Typeface.Builder(f).build() } catch (_: Exception) { null }
            } ?: Typeface.MONOSPACE
        }
        const val LAUNCHER = "io.github.dhiraj_j_s.minimalsf"
        const val SHIZUKU = "moe.shizuku.privileged.api"
        val TEXT = Color.parseColor("#FFCCCCCC")
        val GREY = Color.parseColor("#FF808080")
        val GREEN = Color.parseColor("#FF66BB6A")
        val RED = Color.parseColor("#FFFF6B6B")
        val DIVIDER = Color.parseColor("#FF222222")
    }
}
