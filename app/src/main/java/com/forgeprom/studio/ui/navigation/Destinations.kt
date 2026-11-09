package com.forgeprom.studio.ui.navigation

/**
 * مسارات التنقل — ثوابت مركزية.
 */
object Destinations {
    const val DASHBOARD = "dashboard"
    const val CREATE = "create"
    const val PROJECTS = "projects"
    const val MODELS = "models"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val ANALYSIS = "analysis"
    const val RESULT = "result"
    const val IMAGE = "image"

    const val PROJECT_DETAIL = "project/{id}"
    fun projectDetail(id: Long) = "project/" + id

    const val EDITOR = "editor/{promptId}"
    fun editor(promptId: Long) = "editor/" + promptId

    val bottomTabs = listOf(DASHBOARD, CREATE, PROJECTS, MODELS, SETTINGS)
}
