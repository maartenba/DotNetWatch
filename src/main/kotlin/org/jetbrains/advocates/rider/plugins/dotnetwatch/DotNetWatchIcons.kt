@file:Suppress("PackageDirectoryMismatch")

package icons

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

@Suppress("SameParameterValue")
object DotNetWatchIcons {
    @JvmField val RunConfiguration = load("runConfiguration.svg")

    private fun load(path: String): Icon = IconLoader.getIcon("/icons/$path", this::class.java)
}