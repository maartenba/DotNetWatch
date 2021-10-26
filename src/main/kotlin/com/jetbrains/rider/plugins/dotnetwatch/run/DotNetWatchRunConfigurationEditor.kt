package com.jetbrains.rider.plugins.dotnetwatch.run

import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.layout.panel
import com.jetbrains.rider.plugins.dotnetwatch.DotNetWatchBundle

class DotNetWatchRunConfigurationEditor : SettingsEditor<DotNetWatchRunConfiguration>() {

    private val projectEditor = TextFieldWithBrowseButton()

    override fun createEditor() =
        panel {
            row {
                cell { label(DotNetWatchBundle.message("run.configuration.projectEditor.label")) }
                cell { component(projectEditor) }
            }
        }

    override fun resetEditorFrom(runConfiguration: DotNetWatchRunConfiguration) {
        projectEditor.text = runConfiguration.watchOptions().projectName ?: ""
    }

    override fun applyEditorTo(runConfiguration: DotNetWatchRunConfiguration) {
        runConfiguration.watchOptions().projectName = projectEditor.text
    }
}