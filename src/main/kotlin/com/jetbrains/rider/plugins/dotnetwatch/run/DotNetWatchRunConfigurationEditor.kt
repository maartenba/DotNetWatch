package com.jetbrains.rider.plugins.dotnetwatch.run

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.jetbrains.rd.util.lifetime.Lifetime
import com.jetbrains.rdclient.protocol.IPermittedModalities
import com.jetbrains.rider.model.runnableProjectsModel
import com.jetbrains.rider.plugins.dotnetwatch.DotNetWatchBundle
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.run.configurations.LifetimedSettingsEditor
import com.jetbrains.rider.run.configurations.controls.*
import javax.swing.JComponent

class DotNetWatchRunConfigurationEditor(private val project: Project)
    : LifetimedSettingsEditor<DotNetWatchRunConfiguration>() {

    lateinit var viewModel: DotNetWatchRunConfigurationViewModel

    override fun createEditor(lifetime: Lifetime) : JComponent {
        IPermittedModalities.getInstance().allowPumpProtocolUnderCurrentModality()

        viewModel = DotNetWatchRunConfigurationViewModel(
            lifetime,
            project,
            project.solution.runnableProjectsModel,
            ProjectSelector(DotNetWatchBundle.message("run.configuration.project.label")),
            StringSelector(DotNetWatchBundle.message("run.configuration.tfm.label")),
            ProgramParametersEditor(DotNetWatchBundle.message("run.configuration.programParameters.label"), lifetime),
            EnvironmentVariablesEditor(DotNetWatchBundle.message("run.configuration.environmentVariables.label")),
            FlagEditor(DotNetWatchBundle.message("run.configuration.useExternalConsole.label")),
            ViewSeparator(DotNetWatchBundle.message("run.configuration.separator.label")),
            FlagEditor(DotNetWatchBundle.message("run.configuration.isVerbose.label"))
        )

        return ControlViewBuilder(lifetime, project).build(viewModel)
    }

    override fun resetEditorFrom(runConfiguration: DotNetWatchRunConfiguration) {
        runConfiguration.watchOptions().apply {
            viewModel.reset(
                projectFilePath,
                projectTfm,
                exePath,
                programParameters,
                workingDirectory,
                envs,
                isPassParentEnvs,
                useExternalConsole,
                isUnloadedProject(project),
                isVerbose
            )
        }
    }

    override fun applyEditorTo(runConfiguration: DotNetWatchRunConfiguration) {
        val selectedProject = viewModel.projectSelector.project.valueOrNull
        val selectedTfm = viewModel.tfmSelector.string.valueOrNull
        if (selectedProject != null) {
            if (selectedTfm != null) {
                runConfiguration.watchOptions().apply {
                    projectFilePath = selectedProject.projectFilePath
                    projectTfm = selectedTfm
                    exePath = FileUtil.toSystemIndependentName(viewModel.exePathSelector.path.value)
                    programParameters = viewModel.programParametersEditor.parametersString.value
                    workingDirectory = FileUtil.toSystemIndependentName(viewModel.workingDirectorySelector.path.value)
                    envs = viewModel.environmentVariablesEditor.envs.value
                    isPassParentEnvs = viewModel.environmentVariablesEditor.isPassParentEnvs.value
                    useExternalConsole = viewModel.useExternalConsoleEditor.isSelected.value
                    isVerbose = viewModel.isVerboseEditor.isSelected.value
                }
            }
        }
    }
}