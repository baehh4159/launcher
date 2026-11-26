package com.desklauncher.feature.apps.data

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.os.Handler
import android.net.Uri
import android.os.Looper
import android.os.Process
import android.os.UserHandle
import com.desklauncher.core.common.dispatcher.ApplicationScope
import com.desklauncher.core.common.dispatcher.DeskDispatchers
import com.desklauncher.core.common.dispatcher.Dispatcher
import com.desklauncher.core.database.dao.AppPrefDao
import com.desklauncher.feature.apps.domain.model.InstalledApp
import com.desklauncher.feature.apps.domain.repository.AppsRepository
import com.desklauncher.feature.apps.ui.icon.AppIconLoader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/** LauncherApps 기반 (SHELL-07). 업무 프로필은 P2 — 현재 사용자만. */
internal class LauncherAppsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @Dispatcher(DeskDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    @ApplicationScope private val appScope: CoroutineScope,
    private val iconLoader: AppIconLoader,
    private val appPrefDao: AppPrefDao,
) : AppsRepository {

    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val user: UserHandle = Process.myUserHandle()

    private val installedApps: Flow<List<InstalledApp>> = callbackFlow {
        val callback = object : LauncherApps.Callback() {
            override fun onPackageRemoved(packageName: String?, user: UserHandle?) = changed()
            override fun onPackageAdded(packageName: String?, user: UserHandle?) = changed()
            override fun onPackageChanged(packageName: String?, user: UserHandle?) = changed()
            override fun onPackagesAvailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) = changed()
            override fun onPackagesUnavailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) = changed()
            private fun changed() {
                iconLoader.invalidate()
                trySend(Unit)
            }
        }
        trySend(Unit)
        launcherApps.registerCallback(callback, Handler(Looper.getMainLooper()))
        awaitClose { launcherApps.unregisterCallback(callback) }
    }
        .conflate()
        .map { queryApps() }
        .flowOn(ioDispatcher)

    override val apps: Flow<List<InstalledApp>> = combine(installedApps, appPrefDao.observeAll()) { apps, prefs ->
        val prefByKey = prefs.associateBy { it.componentKey }
        apps.map { app ->
            val pref = prefByKey[app.componentKey]
            app.copy(isFavorite = pref?.favorite == true, isHidden = pref?.hidden == true)
        }
    }

    override fun launch(app: InstalledApp): Boolean {
        val component = ComponentName.unflattenFromString(app.componentKey) ?: return false
        return try {
            launcherApps.startMainActivity(component, user, null, null)
            appScope.launch { appPrefDao.recordLaunch(app.componentKey, System.currentTimeMillis()) }
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }

    override suspend fun setFavorite(app: InstalledApp, favorite: Boolean) = appPrefDao.setFavorite(app.componentKey, favorite)

    override suspend fun setHidden(app: InstalledApp, hidden: Boolean) = appPrefDao.setHidden(app.componentKey, hidden)

    override fun openAppInfo(app: InstalledApp) {
        val component = ComponentName.unflattenFromString(app.componentKey) ?: return
        runCatching { launcherApps.startAppDetailsActivity(component, user, null, null) }
    }

    override fun requestUninstall(app: InstalledApp) {
        val intent = Intent(Intent.ACTION_DELETE, Uri.fromParts("package", app.packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    private fun queryApps(): List<InstalledApp> =
        launcherApps.getActivityList(null, user)
            .filter { it.componentName.packageName != context.packageName }
            .map { info ->
                InstalledApp(
                    componentKey = info.componentName.flattenToString(),
                    packageName = info.componentName.packageName,
                    label = info.label?.toString().orEmpty().ifBlank { info.componentName.packageName },
                    isSystemApp = info.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0,
                )
            }
}
