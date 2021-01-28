package com.woodnoisu.reader.app.kodein

import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.navigation.NavArgs
import androidx.navigation.NavArgsLazy
import org.kodein.di.Kodein
import org.kodein.di.bindings.BindingKodein
import org.kodein.di.bindings.ExternalSource
import org.kodein.di.fullErasedName
import org.kodein.di.jvmType
import kotlin.reflect.KClass

class FragmentArgsExternalSource : ExternalSource {
    override fun getFactory(kodein: BindingKodein<*>, key: Kodein.Key<*, *, *>): ((Any?) -> Any)? {
        val fragment = kodein.context as? Fragment

        if (fragment != null) {
            val deductedArgsClassName = fragment.javaClass.canonicalName + "Args"

            if (deductedArgsClassName == key.type.jvmType.fullErasedName()) {

                val navArgsInstance = getNavArgsInstance(fragment)

                if (navArgsInstance != null) {
                    return { navArgsInstance }
                }
            }
        }

        return null
    }

    // 此函数使用反射来提供适当的[NavArgs]实例以派生
    // 片段类名称中的Args类名称。
    @MainThread
    private fun getNavArgsInstance(fragment: Fragment): NavArgs? {
        val arguments = fragment.arguments ?: return null

        // SafeArgs插件在fragment类中添加“ Agrs”后缀。
        //如果com.abc.MyFragment类在nav_graph.xml中定义了参数，则
        //将生成com.abc.MyFragmentArgs类。
        val safeArgsClassSuffix = "Args"
        val className = "${fragment::class.java.canonicalName}$safeArgsClassSuffix"

        @Suppress("UNCHECKED_CAST")
        val navArgsClass = requireNotNull(getArgNavClass(className)) {
            //当导航图资源未为特定片段定义参数时，可能会发生这种情况
            "Fragment $className has arguments, but corresponding navArgs class $className does not exist."
        }

        // 让我们检查一下Args类是否确实存在
        val navArgs by NavArgsLazy(navArgsClass) { arguments }
        return navArgs
    }

    private fun getArgNavClass(className: String): KClass<NavArgs>? = try {
        @Suppress("UNCHECKED_CAST")
        Class.forName(className).kotlin as KClass<NavArgs>
    } catch (e: ClassNotFoundException) {
        null
    }
}
