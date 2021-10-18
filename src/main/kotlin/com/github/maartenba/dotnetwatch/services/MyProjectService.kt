package com.github.maartenba.dotnetwatch.services

import com.intellij.openapi.project.Project
import com.github.maartenba.dotnetwatch.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
