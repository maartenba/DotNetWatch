package com.jetbrains.rider.plugins.dotnetwatch.run

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.io.systemIndependentPath
import com.intellij.psi.PsiElement
import com.jetbrains.rider.model.runnableProjectsModel
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.projectView.workspace.getFile
import com.jetbrains.rider.run.configurations.getSelectedProject

// TODO this one overrides the default context menu | Run ..., which should probably be improved
class DotNetWatchRunConfigurationProducer
    : LazyRunConfigurationProducer<DotNetWatchRunConfiguration>() {

    override fun getConfigurationFactory() = ConfigurationTypeUtil.findConfigurationType(DotNetWatchRunConfigurationType::class.java)
        .configurationFactories
        .single()

    override fun isConfigurationFromContext(
        configuration: DotNetWatchRunConfiguration,
        context: ConfigurationContext
    ) : Boolean {
        val selectedProjectFilePathInvariant = context.getSelectedProject()?.getFile()?.systemIndependentPath ?: return false

        val projects = context.project.solution.runnableProjectsModel.projects.valueOrNull ?: return false
        val runnableProject = projects.firstOrNull {
            FileUtil.toSystemIndependentName(it.projectFilePath) == selectedProjectFilePathInvariant &&
                    (configuration.watchOptions().projectName ?: "") == it.name
        }

        return runnableProject != null
    }

    override fun setupConfigurationFromContext(
        configuration: DotNetWatchRunConfiguration,
        context: ConfigurationContext,
        psiElement: Ref<PsiElement>
    ): Boolean {
        // TODO do we need project file or project name?
        val selectedProjectFilePathInvariant = context.getSelectedProject()?.getFile()?.systemIndependentPath ?: return false

        val projects = context.project.solution.runnableProjectsModel.projects.valueOrNull ?: return false
        val runnableProject = projects.firstOrNull {
            FileUtil.toSystemIndependentName(it.projectFilePath) == selectedProjectFilePathInvariant
        } ?: return false

        if (configuration.name.isEmpty()) {
            configuration.name = runnableProject.name
        }
        configuration.watchOptions().projectName = runnableProject.name

        return true
    }

}