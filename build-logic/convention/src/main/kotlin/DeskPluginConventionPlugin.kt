import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * plugins/<이름> 모듈(사용자가 만든 책상 도구) 공통 설정.
 * feature 모듈과 같은 의존을 갖되, 앱 내부 구현(DB·DataStore 등)은 주지 않는다 —
 * 플러그인은 core:tool-api 계약(DeskToolHost·PluginStorage)만으로 동작해야 앱 내부가 바뀌어도 깨지지 않는다.
 */
class DeskPluginConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("desk.android.feature")
    }
}
