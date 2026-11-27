package com.desklauncher.core.toolapi

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import kotlinx.serialization.Serializable

/**
 * 화면 하나짜리 플러그인용 기반 클래스. 정보(id·이름·아이콘·색)와 [Content]만 쓰면 된다.
 * 내비게이션 라우트는 앱이 [SimpleToolDestination] 하나로 모든 단순 플러그인을 대신 띄운다.
 *
 * ```
 * class WaterTool @Inject constructor() : SimpleDeskTool() {
 *     override val id = "water"
 *     …
 *     @Composable override fun Content(host: DeskToolHost, modifier: Modifier) {
 *         DeskToolScaffold(host, title = stringResource(titleRes)) { padding -> … }
 *     }
 * }
 * ```
 */
abstract class SimpleDeskTool : DeskTool {

    final override val startDestination: Any get() = SimpleToolDestination(id)

    /** 앱이 공용 라우트로 띄우므로 따로 등록하지 않는다 */
    final override fun NavGraphBuilder.toolGraph(navController: NavController) = Unit

    @Composable
    abstract fun Content(host: DeskToolHost, modifier: Modifier)
}

/** 단순 플러그인 공용 라우트 — 앱이 한 번만 등록한다 */
@Serializable
data class SimpleToolDestination(val toolId: String)
