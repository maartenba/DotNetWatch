package org.jetbrains.advocates.rider.plugins.dotnetwatch.run

import com.intellij.execution.configurations.ConfigurationType
import icons.DotNetWatchIcons

@Suppress("DialogTitleCapitalization")
class DotNetWatchRunConfigurationType : ConfigurationType {

    companion object {
        // Do not change this once set
        const val ID = "RunDotNetWatch"
    }

    override fun getId() = ID

    override fun getIcon() = DotNetWatchIcons.RunConfiguration

    override fun getDisplayName() = "dotnet-watch"

    override fun getConfigurationTypeDescription() = "Runs dotnet watch"

    override fun getConfigurationFactories() = arrayOf(DotNetWatchRunConfigurationFactory(this))
}