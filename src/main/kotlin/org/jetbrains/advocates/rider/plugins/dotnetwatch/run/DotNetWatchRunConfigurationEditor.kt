package org.jetbrains.advocates.rider.plugins.dotnetwatch.run

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
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
            ProjectSelector(DotNetWatchBundle.message("run.configuration.project.label"), "Project"),
            StringSelector(DotNetWatchBundle.message("run.configuration.tfm.label"), "Target_framework"),
            ProgramParametersEditor(DotNetWatchBundle.message("run.configuration.programParameters.label"), "Program_arguments", lifetime),
            PathSelector(DotNetWatchBundle.message("run.configuration.workingDirectory.label"), "Working_directory", FileChooserDescriptorFactory.createSingleFolderDescriptor(), lifetime),
            EnvironmentVariablesEditor(DotNetWatchBundle.message("run.configuration.environmentVariables.label"), "Environment_variables"),
            FlagEditor(DotNetWatchBundle.message("run.configuration.useExternalConsole.label"), "Use_external_console"),
            ViewSeparator(DotNetWatchBundle.message("run.configuration.separator.additional.label")),
            EnumSelector<DotNetWatchVerbosity>(DotNetWatchBundle.message("run.configuration.verbosity.label"), "Dotnet_watch_verbosity", EnumSet.allOf(DotNetWatchVerbosity::class.java)) {
                when (it) {
                    DotNetWatchVerbosity.NORMAL -> DotNetWatchBundle.message("run.configuration.verbosity.normal")
                    DotNetWatchVerbosity.QUIET -> DotNetWatchBundle.message("run.configuration.verbosity.quiet")
                    DotNetWatchVerbosity.VERBOSE -> DotNetWatchBundle.message("run.configuration.verbosity.verbose")
                }
            },
            FlagEditor(DotNetWatchBundle.message("run.configuration.isSuppressHotReload.label"), "Dotnet_watch_suppress_hotreload"),
            FlagEditor(DotNetWatchBundle.message("run.configuration.isRestartOnRudeEdit.label"), "Dotnet_watch_restart_on_rude_edit"),
            FlagEditor(DotNetWatchBundle.message("run.configuration.isUsePollingFileWatcher.label"), "Dotnet_watch_use_polling"),
            ViewSeparator(DotNetWatchBundle.message("run.configuration.separator.browser.label")),
            FlagEditor(DotNetWatchBundle.message("run.configuration.isSuppressBrowserLaunch.label"), "Dotnet_watch_suppress_browser_launch"),
            FlagEditor(DotNetWatchBundle.message("run.configuration.isSuppressBrowserRefresh.label"), "Dotnet_watch_suppress_browser_refresh")
        )

        return ControlViewBuilder(lifetime, project).build(viewModel)
    }

    override fun resetEditorFrom(runConfiguration: DotNetWatchRunConfiguration) {
        runConfiguration.watchOptions().apply {
            viewModel.reset(
                projectFilePath,
                trackProjectExePath,
                trackProjectWorkingDirectory,
                projectTfm,
                exePath,
                programParameters,
                workingDirectory,
                envs,
                isPassParentEnvs,
                useExternalConsole,
                isUnloadedProject(project),
                verbosity,
                isSuppressHotReload,
                isRestartOnRudeEditEditor,
                isUsePollingFileWatcher,
                isSuppressBrowserLaunch,
                isSuppressBrowserRefresh
            )
        }
    }

    override fun applyEditorTo(runConfiguration: DotNetWatchRunConfiguration) {
        val selectedProject = viewModel.projectSelector.project.valueOrNull
        val selectedTfm = viewModel.tfmSelector.string.valueOrNull
        if (selectedProject != null && selectedTfm != null) {
            runConfiguration.watchOptions().apply {
                projectFilePath = selectedProject.projectFilePath
                trackProjectExePath = viewModel.trackProjectExePath
                trackProjectWorkingDirectory = viewModel.trackProjectWorkingDirectory
                projectTfm = selectedTfm
                exePath = FileUtil.toSystemIndependentName(viewModel.exePathSelector.path.value)
                programParameters = viewModel.programParametersEditor.parametersString.value
                workingDirectory = FileUtil.toSystemIndependentName(viewModel.workingDirectorySelector.path.value)
                envs = viewModel.environmentVariablesEditor.envs.value
                isPassParentEnvs = viewModel.environmentVariablesEditor.isPassParentEnvs.value
                useExternalConsole = viewModel.useExternalConsoleEditor.isSelected.value
                verbosity = viewModel.verbosityEditor.rawValue.valueOrNull ?: DotNetWatchVerbosity.NORMAL
                isSuppressHotReload = viewModel.isSuppressHotReloadEditor.isSelected.value
                isRestartOnRudeEditEditor = viewModel.isRestartOnRudeEditEditor.isSelected.value
                isUsePollingFileWatcher = viewModel.isUsePollingFileWatcherEditor.isSelected.value
                isSuppressBrowserLaunch = viewModel.isSuppressBrowserLaunchEditor.isSelected.value
                isSuppressBrowserRefresh = viewModel.isSuppressBrowserRefreshEditor.isSelected.value
            }
        }
    }
}
