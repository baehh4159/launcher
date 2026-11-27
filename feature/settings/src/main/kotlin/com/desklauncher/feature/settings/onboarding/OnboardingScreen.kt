package com.desklauncher.feature.settings.onboarding

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SwipeVertical
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.desklauncher.core.designsystem.component.DeskSurface
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskLine
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.settings.R
import com.desklauncher.feature.settings.role.rememberHomeRoleState
import kotlinx.coroutines.launch

/** 첫 실행 안내 (SHELL-10). */
@Composable
fun OnboardingRoute(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val homeRole = rememberHomeRoleState()
    // 첫 화면 뒤로가기: 건너뛰기와 같게 취급하지 않고 무시한다(런처가 닫히지 않도록).
    BackHandler { }
    OnboardingScreen(
        isDefaultLauncher = homeRole.isDefault,
        onRequestDefaultLauncher = homeRole.request,
        onFinish = { viewModel.complete(onFinished) },
        modifier = modifier,
    )
}

private enum class OnboardingPage(
    val icon: ImageVector,
    @param:StringRes val title: Int,
    @param:StringRes val body: Int,
) {
    Welcome(Icons.Outlined.Home, R.string.onboarding_welcome_title, R.string.onboarding_welcome_body),
    Strip(Icons.Outlined.SwipeVertical, R.string.onboarding_strip_title, R.string.onboarding_strip_body),
    Gesture(Icons.Outlined.TouchApp, R.string.onboarding_gesture_title, R.string.onboarding_gesture_body),
}

@Composable
fun OnboardingScreen(
    isDefaultLauncher: Boolean,
    onRequestDefaultLauncher: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    pagerState: PagerState = rememberPagerState { OnboardingPage.entries.size },
) {
    val scope = rememberCoroutineScope()
    val pages = OnboardingPage.entries
    DeskSurface(modifier = modifier) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onFinish) { Text(stringResource(R.string.onboarding_skip), color = DeskTheme.colors.onDesk) }
            }
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { index ->
                val page = pages[index]
                OnboardingPageCard(page = page) {
                    if (page == OnboardingPage.Welcome) {
                        if (isDefaultLauncher) {
                            Text(
                                text = stringResource(R.string.settings_launcher_is_default),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            FilledTonalButton(onClick = onRequestDefaultLauncher) {
                                Text(stringResource(R.string.settings_launcher_request))
                            }
                        }
                    }
                }
            }
            PageDots(current = pagerState.currentPage, count = pages.size)
            Spacer(Modifier.height(16.dp))
            val isLast = pagerState.currentPage == pages.lastIndex
            Button(
                onClick = {
                    if (isLast) onFinish() else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text(stringResource(if (isLast) R.string.onboarding_start else R.string.onboarding_next))
            }
        }
    }
}

@Composable
private fun OnboardingPageCard(page: OnboardingPage, extra: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.Center) {
        Surface(
            color = DeskTheme.colors.paper,
            contentColor = DeskTheme.colors.onPaper,
            shape = MaterialTheme.shapes.extraLarge,
            border = BorderStroke(DeskLine.Hairline, DeskTheme.colors.line),
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(page.icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                Text(stringResource(page.title), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                Text(
                    text = stringResource(page.body),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                extra()
            }
        }
    }
}

@Composable
private fun PageDots(current: Int, count: Int) {
    val label = stringResource(R.string.onboarding_page, current + 1, count)
    Row(
        modifier = Modifier.fillMaxWidth().semantics { contentDescription = label },
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        repeat(count) { index ->
            Box(
                Modifier
                    .size(if (index == current) 10.dp else 8.dp)
                    .background(
                        if (index == current) MaterialTheme.colorScheme.primary else DeskTheme.colors.onDesk.copy(alpha = 0.35f),
                        CircleShape,
                    ),
            )
        }
    }
}

@DeskPreviews
@Composable
private fun OnboardingScreenPreview() {
    DeskTheme {
        OnboardingScreen(isDefaultLauncher = false, onRequestDefaultLauncher = {}, onFinish = {})
    }
}

@DeskPreviews
@Composable
private fun OnboardingScreenLastPagePreview() {
    DeskTheme {
        OnboardingScreen(
            isDefaultLauncher = true,
            onRequestDefaultLauncher = {},
            onFinish = {},
            pagerState = rememberPagerState(initialPage = 2) { 3 },
        )
    }
}
