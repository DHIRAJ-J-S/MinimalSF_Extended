package io.github.dhiraj_j_s.minimalsf.extended

import android.app.Application
import rikka.sui.Sui

/**
 * Shizuku's official app and its forks hand the plugin their connection through ShizukuProvider.
 * Sui (the Magisk / KernelSU module) works differently and has to be looked up once at start-up.
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Sui.init(BuildConfig.APPLICATION_ID)
    }
}
