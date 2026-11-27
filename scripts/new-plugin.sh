#!/usr/bin/env bash
# 새 책상 플러그인 뼈대를 만든다 (SHELL-14, docs/PLUGIN_GUIDE.md).
#   사용법: scripts/new-plugin.sh <id> ["도구 이름"]
#   예:     scripts/new-plugin.sh habit "습관 체크"
# 만들어지는 것: plugins/<id>/ — SimpleDeskTool 구현, Hilt 등록, Route/Screen/Preview, strings.xml
# settings.gradle.kts와 app이 plugins/*를 자동으로 포함하므로 다른 파일은 고치지 않아도 된다.
set -euo pipefail

id="${1:-}"
title="${2:-새 도구}"
if [[ ! "$id" =~ ^[a-z][a-z0-9_]{1,31}$ ]]; then
  echo "id는 소문자로 시작하는 소문자·숫자·밑줄 2~32자여야 합니다 (예: habit, reading_log)" >&2
  exit 1
fi

root="$(cd "$(dirname "$0")/.." && pwd)"
dir="$root/plugins/$id"
if [[ -e "$dir" ]]; then
  echo "이미 있습니다: plugins/$id" >&2
  exit 1
fi
for existing in "$root"/feature/*/src/main/kotlin "$root"/plugins/*/src/main/kotlin; do
  [[ -d "$existing" ]] || continue
  if grep -rqE "(override val id: String = |ToolId = )\"$id\"" "$existing"; then
    echo "이미 쓰는 도구 id입니다: $id" >&2
    exit 1
  fi
done

# reading_log → ReadingLog
name="$(echo "$id" | awk -F_ '{for (i = 1; i <= NF; i++) printf toupper(substr($i, 1, 1)) substr($i, 2)}')"
pkg="com.desklauncher.plugin.$id"
src="$dir/src/main/kotlin/com/desklauncher/plugin/$id"
mkdir -p "$src/di" "$src/ui" "$dir/src/main/res/values"

cat > "$dir/build.gradle.kts" <<EOF
plugins {
    alias(libs.plugins.desk.plugin)
}

android {
    namespace = "$pkg"
}
EOF

cat > "$src/${name}Tool.kt" <<EOF
package $pkg

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.toolapi.DeskToolHost
import com.desklauncher.core.toolapi.SimpleDeskTool
import $pkg.ui.${name}Route
import javax.inject.Inject

/** 도구 id — DB 키. 출시 후 변경 금지. */
const val ${name}ToolId = "$id"

class ${name}Tool @Inject constructor() : SimpleDeskTool() {
    override val id: String = ${name}ToolId
    override val titleRes: Int = R.string.${id}_tool_title
    override val descriptionRes: Int = R.string.${id}_tool_description
    override val icon: ImageVector = Icons.Outlined.Extension
    override val defaultTabColor: DeskTabColor = DeskTabColor.Gray
    override val defaultEnabled: Boolean = false
    override val defaultOrder: Int = 100

    @Composable
    override fun Content(host: DeskToolHost, modifier: Modifier) {
        ${name}Route(host = host, modifier = modifier)
    }
}
EOF

cat > "$src/di/${name}ToolModule.kt" <<EOF
package $pkg.di

import com.desklauncher.core.toolapi.DeskTool
import $pkg.${name}Tool
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ${name}ToolModule {
    @Binds
    @IntoSet
    abstract fun bind${name}Tool(tool: ${name}Tool): DeskTool
}
EOF

cat > "$src/ui/${name}Screen.kt" <<EOF
package $pkg.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.core.toolapi.DeskToolHost
import com.desklauncher.core.toolapi.DeskToolScaffold
import com.desklauncher.core.toolapi.PreviewDeskToolHost
import $pkg.R

/** 상태를 붙이는 곳. 저장은 host.storage(플러그인 전용 키-값)를 쓴다 — plugins/water 참고. */
@Composable
fun ${name}Route(host: DeskToolHost, modifier: Modifier = Modifier) {
    ${name}Screen(host = host, modifier = modifier)
}

@Composable
fun ${name}Screen(host: DeskToolHost, modifier: Modifier = Modifier) {
    DeskToolScaffold(host = host, title = stringResource(R.string.${id}_tool_title), modifier = modifier) { padding ->
        Text(
            text = stringResource(R.string.${id}_tool_description),
            modifier = Modifier.fillMaxSize().padding(padding).wrapContentSize(),
        )
    }
}

@DeskPreviews
@Composable
private fun ${name}ScreenPreview() {
    DeskTheme { ${name}Screen(host = PreviewDeskToolHost(toolId = "$id")) }
}
EOF

cat > "$dir/src/main/res/values/strings.xml" <<EOF
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="${id}_tool_title">$title</string>
    <string name="${id}_tool_description">$title 플러그인</string>
</resources>
EOF

echo "plugins/$id 를 만들었습니다. ./gradlew assembleDebug 후 앱의 도구함에서 '$title'을 켜세요."
