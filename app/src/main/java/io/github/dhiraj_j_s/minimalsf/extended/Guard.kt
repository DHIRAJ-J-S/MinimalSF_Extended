package io.github.dhiraj_j_s.minimalsf.extended

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo

/**
 * Second line of defence behind the signature permission that guards the action screen and the menu.
 * Android only lets apps signed with our key use that permission, as long as it was defined by us. If it
 * were ever defined by some other app (a bug, or an old Android quirk), it might not protect anything,
 * so the plugin refuses to work unless it's defined by MinimalSF or this plugin, at signature level,
 * and MinimalSF is signed with the same key as this plugin.
 */
object Guard {
    const val PERMISSION = "io.github.dhiraj_j_s.minimalsf.permission.EXTENDED"
    private const val LAUNCHER = "io.github.dhiraj_j_s.minimalsf"

    fun intact(c: Context): Boolean = try {
        val pm = c.packageManager
        val info = pm.getPermissionInfo(PERMISSION, 0)
        @Suppress("DEPRECATION")
        val signature = (info.protectionLevel and PermissionInfo.PROTECTION_MASK_BASE) == PermissionInfo.PROTECTION_SIGNATURE
        val owner = info.packageName == c.packageName || info.packageName == LAUNCHER
        val launcherOk = try {
            pm.getPackageInfo(LAUNCHER, 0)
            pm.checkSignatures(c.packageName, LAUNCHER) == PackageManager.SIGNATURE_MATCH
        } catch (_: PackageManager.NameNotFoundException) {
            true // MinimalSF not installed: nothing can be calling us, and nothing is lost by allowing setup
        }
        signature && owner && launcherOk
    } catch (_: Exception) {
        false
    }
}
