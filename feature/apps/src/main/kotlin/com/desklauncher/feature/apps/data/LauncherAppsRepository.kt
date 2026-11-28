package com.desklauncher.feature.apps.data

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.UserHandle
import com.desklauncher.core.common.IoDispatcher
import com.desklauncher.feature.apps.domain.AppsRepository
import com.desklauncher.feature.apps.domain.model.InstalledApp
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** LauncherApps 기반 (APPS-01). 현재 사용자만 — 업무 프로필은 후속. */
internal class LauncherAppsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : AppsRepository {

    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val user: UserHandle = Process.myUserHandle()

    override val apps: Flow<List<InstalledApp>> = callbackFlow {
        val callback = object : LauncherApps.Callback() {
            override fun onPackageRemoved(packageName: String?, user: UserHandle?) = changed()
            override fun onPackageAdded(packageName: String?, user: UserHandle?) = changed()
            override fun onPackageChanged(packageName: String?, user: UserHandle?) = changed()
            override fun onPackagesAvailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) = changed()
            override fun onPackagesUnavailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) = changed()
            private fun changed() {
                trySend(Unit)
            }
        }
        trySend(Unit)
        launcherApps.registerCallback(callback, Handler(Looper.getMainLooper()))
        awaitClose { launcherApps.unregisterCallback(callback) }
    }
        .conflate()
        .map { query() }
        .flowOn(ioDispatcher)

    override fun launch(app: InstalledApp): Boolean {
        val component = ComponentName.unflattenFromString(app.key) ?: return false
        return try {
            launcherApps.startMainActivity(component, user, null, null)
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }

    private fun query(): List<InstalledApp> =
        launcherApps.getActivityList(null, user)
            .filter { it.componentName.packageName != context.packageName }
            .map { info ->
                InstalledApp(
                    key = info.componentName.flattenToString(),
                    packageName = info.componentName.packageName,
                    label = info.label?.toString().orEmpty().ifBlank { info.componentName.packageName },
                )
            }
}
