package com.jetbrains.rider.plugins.dotnetwatch.run

import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.util.ui.FormBuilder
import javax.swing.JPanel

class DotNetWatchRunConfigurationEditor : SettingsEditor<DotNetWatchRunConfiguration>() {

    private val scriptNameEditor = TextFieldWithBrowseButton()

    override fun createEditor(): JPanel = FormBuilder()
        .addLabeledComponent("Script name:", scriptNameEditor)
        .panel

    override fun resetEditorFrom(runConfiguration: DotNetWatchRunConfiguration) {
        scriptNameEditor.text = runConfiguration.watchOptions().scriptName ?: ""
    }

    override fun applyEditorTo(runConfiguration: DotNetWatchRunConfiguration) {
        runConfiguration.watchOptions().scriptName = scriptNameEditor.text
    }

//This may be the future! 🔥
    //    val y = panel {
    //        row {
    //
    //        }
    //    }
}