package com.jetbrains.rider.plugins.dotnetwatch.run

import com.intellij.execution.configurations.ConfigurationType
import com.intellij.icons.AllIcons

@Suppress("DialogTitleCapitalization")
class DotNetWatchRunConfigurationType : ConfigurationType {

    companion object {
        // Do not change this once set
        const val ID = "Plugin_DotNetWatch"
    }

    override fun getId() = ID

    override fun getIcon() = AllIcons.General.InspectionsEye

    override fun getDisplayName() = "dotnet-watch"

    override fun getConfigurationTypeDescription() = "Runs dotnet watch"

    override fun getConfigurationFactories() = arrayOf(DotNetWatchRunConfigurationFactory(this))
}