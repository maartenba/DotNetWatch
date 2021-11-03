package org.jetbrains.advocates.rider.plugins.dotnetwatch.run

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.jetbrains.rd.util.lifetime.Lifetime
import com.jetbrains.rdclient.protocol.IPermittedModalities
import com.jetbrains.rider.model.runnableProjectsModel
import org.jetbrains.advocates.rider.plugins.dotnetwatch.DotNetWatchBundle
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.run.configurations.LifetimedSettingsEditor
import com.jetbrains.rider.run.configurations.controls.*
import java.util.*
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
            EnumSelector<DotNetWatchVerbosity>(DotNetWatchBundle.message("run.configuration.verbosity.label"), EnumSet.allOf(DotNetWatchVerbosity::class.java)) {
                when (it) {
                    DotNetWatchVerbosity.NORMAL -> DotNetWatchBundle.message("run.configuration.verbosity.normal")
                    DotNetWatchVerbosity.QUIET -> DotNetWatchBundle.message("run.configuration.verbosity.quiet")
                    DotNetWatchVerbosity.VERBOSE -> DotNetWatchBundle.message("run.configuration.verbosity.verbose")
                }
            },
            FlagEditor(DotNetWatchBundle.message("run.configuration.isSuppressHotReload.label"))
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
                verbosity,
                isSuppressHotReload
            )
        }
    }

    override fun applyEditorTo(runConfiguration: DotNetWatchRunConfiguration) {
        val selectedProject = viewModel.projectSelector.project.valueOrNull
        val selectedTfm = viewModel.tfmSelector.string.valueOrNull
        if (selectedProject != null && selectedTfm != null) {
            runConfiguration.watchOptions().apply {
                projectFilePath = selectedProject.projectFilePath
                projectTfm = selectedTfm
                exePath = FileUtil.toSystemIndependentName(viewModel.exePathSelector.path.value)
                programParameters = viewModel.programParametersEditor.parametersString.value
                workingDirectory = FileUtil.toSystemIndependentName(viewModel.workingDirectorySelector.path.value)
                envs = viewModel.environmentVariablesEditor.envs.value
                isPassParentEnvs = viewModel.environmentVariablesEditor.isPassParentEnvs.value
                useExternalConsole = viewModel.useExternalConsoleEditor.isSelected.value
                verbosity = viewModel.verbosityEditor.rawValue.valueOrNull ?: DotNetWatchVerbosity.NORMAL
                isSuppressHotReload = viewModel.isSuppressHotReloadEditor.isSelected.value
            }
        }
    }
}