package ro.alynsampmobile.game.ui.widgets.battlepass

/** state: 0 = locked, 1 = available (can be claimed), 2 = claimed */
data class BpMainItem(
    val name: String,
    val sprite: String,
    val rare: Int,
    val state: Int
)

data class BpTaskItem(
    val caption: String,
    val description: String,
    val cur: Int,
    val max: Int,
    val reward: Int
)

data class BpRateItem(
    val nick: String,
    val points: Int
)
