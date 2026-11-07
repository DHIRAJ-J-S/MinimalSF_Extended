package io.github.dhiraj_j_s.minimalsf.extended

import android.app.Activity
import android.app.ActivityOptions
import android.content.ComponentName
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Bundle
import android.os.UserHandle
import android.text.format.Formatter
import android.widget.Toast
import rikka.shizuku.Shizuku

/**
 * Invisible screen MinimalSF opens to run one action. Only apps signed with the same key as this
 * plugin can open it (the signature permission on it in the manifest), so only MinimalSF can.
 * It runs the action through Shizuku (asking for its permission first if needed) or root mode, shows a
 * short message and closes. Freeform also works without either.
 */
class ActionActivity : Activity() {

    private var permissionListener: Shizuku.OnRequestPermissionResultListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val action = intent.getStringExtra("action") ?: ""
        val pkg = intent.getStringExtra("package") ?: ""
        val component = intent.getStringExtra("component")
        val user = intent.getIntExtra("user", 0)
        val label = intent.getStringExtra("label")?.take(60) ?: pkg

        if (!Guard.intact(this)) return done("MinimalSF Extended is locked: its permission isn't set up as expected")
        if (!Actions.validPackage(pkg)) return done("invalid app")
        Actions.refusal(action, pkg)?.let { return done(it) }
        if (action != Actions.CLEAR_CACHE && Actions.argv(action, pkg, component, user) == null) return done("unknown action")

        // Shizuku if it's running and allowed, otherwise root mode if it's turned on
        Shell.backend(this)?.let { return perform(it, action, pkg, component, user, label) }
        // Freeform doesn't need either: Android's own launch-in-a-window option is used
        if (action == Actions.FREEFORM) return openFreeform(component, user)

        when (Shell.state()) {
            Shell.State.NOT_RUNNING -> openSetup("start Shizuku, or turn on root mode")
            Shell.State.OUTDATED -> openSetup("Shizuku is too old - update it")
            Shell.State.NO_PERMISSION -> {
                val listener = Shizuku.OnRequestPermissionResultListener { _, result ->
                    if (result == PackageManager.PERMISSION_GRANTED) perform(Shell, action, pkg, component, user, label)
                    else done("Shizuku permission denied")
                }
                permissionListener = listener
                Shizuku.addRequestPermissionResultListener(listener)
                Shizuku.requestPermission(REQUEST_CODE)
            }
            Shell.State.READY -> perform(Shell, action, pkg, component, user, label)
        }
    }

    override fun onDestroy() {
        permissionListener?.let { Shizuku.removeRequestPermissionResultListener(it) }
        super.onDestroy()
    }

    private fun perform(backend: Backend, action: String, pkg: String, component: String?, user: Int, label: String) {
        if (action == Actions.CLEAR_CACHE) {
            backend.clearCache(pkg, user) { code, out ->
                done(when (code) {
                    0 -> "cleared ${Formatter.formatShortFileSize(this, out.toLongOrNull() ?: 0)} of cache from $label"
                    2 -> "clear cache needs root: start Shizuku with root, or use root mode"
                    else -> "couldn't clear cache: $out"
                })
            }
            return
        }
        val argv = Actions.argv(action, pkg, component, user) ?: return done("unknown action")
        backend.run(argv) { code, out ->
            // `am start` reports problems in its output but still exits with 0
            val failed = code != 0 || out.contains("Error", ignoreCase = true) || out.contains("Exception")
            if (!failed && action == Actions.FREEZE) Frozen.add(this, pkg, user, label)
            done(when {
                failed && action == Actions.FREEFORM ->
                    "couldn't open in a window - turn on freeform windows in developer options"
                failed -> "failed: ${out.lineSequence().firstOrNull() ?: code}"
                action == Actions.FORCE_STOP -> "force stopped $label"
                action == Actions.CLEAR_DATA -> "cleared data of $label"
                action == Actions.FREEZE -> "froze $label - unfreeze it in MinimalSF Extended"
                action == Actions.UNFREEZE -> "unfroze $label"
                else -> null
            })
        }
    }

    /** Opens the app in a window without Shizuku: launch bounds, which phones with freeform support honour. */
    private fun openFreeform(component: String?, user: Int) {
        val cn = component?.let { ComponentName.unflattenFromString(it) } ?: return done("unknown app")
        val dm = resources.displayMetrics
        val bounds = Rect(dm.widthPixels / 10, dm.heightPixels / 8, dm.widthPixels * 9 / 10, dm.heightPixels * 5 / 8)
        val options = ActivityOptions.makeBasic().setLaunchBounds(bounds).toBundle()
        try {
            // LauncherApps also reaches work profile and dual apps; a user id maps to its handle via its uid range
            getSystemService(LauncherApps::class.java).startMainActivity(cn, UserHandle.getUserHandleForUid(user * 100_000), bounds, options)
            done(if (packageManager.hasSystemFeature(PackageManager.FEATURE_FREEFORM_WINDOW_MANAGEMENT)) null
                 else "this phone has no freeform windows - turn them on in developer options")
        } catch (_: Exception) {
            done("couldn't open in a window")
        }
    }

    private fun openSetup(message: String) {
        startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        done(message)
    }

    private fun done(message: String?) {
        if (message != null) Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        finish()
    }

    private companion object { const val REQUEST_CODE = 1 }
}
