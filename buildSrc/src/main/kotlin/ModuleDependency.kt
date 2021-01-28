import kotlin.reflect.full.memberProperties

private const val FEATURE_PREFIX = ":feature_"

// “模块”是指Gradle API术语中的“项目”。 具体来说，每个“ Android模块”都是Gradle的“子项目”
@Suppress("unused")
object ModuleDependency {
    // All consts are accessed via reflection
    const val APP = ":app"
    const val FEATURE_ALBUM = ":feature_album"
    const val FEATURE_PROFILE = ":feature_profile"
    const val FEATURE_FAVOURITE = ":feature_favourite"
    const val FEATURE_READER = ":feature_reader"
    const val LIBRARY_BASE = ":library_base"
    const val LIBRARY_TEST_UTILS = ":library_test_utils"

    // False positive" function can be private"
    // See: https://youtrack.jetbrains.com/issue/KT-33610
    fun getAllModules() = ModuleDependency::class.memberProperties
        .filter { it.isConst }
        .map { it.getter.call().toString() }
        .toSet()

    fun getDynamicFeatureModules() = getAllModules()
        .filter { it.startsWith(FEATURE_PREFIX) }
        .toSet()
}
