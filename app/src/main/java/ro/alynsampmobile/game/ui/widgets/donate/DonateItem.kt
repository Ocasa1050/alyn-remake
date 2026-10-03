package ro.alynsampmobile.game.ui.widgets.donate

/**
 * One entry of the donate screen.
 * category: -1 = player's own stash, 1 vehicles, 2 skins, 3 accessories, 4 VIP, 5 other
 * value:    server-side id of the product (sent back on Buy / Info)
 * sqlId:    0 for shop items, > 0 for stash items (sent back on Activate / Sell)
 */
data class DonateItem(
    val category: Int,
    val value: Int,
    val sqlId: Int,
    val price: Int,
    val sprite: String,
    val name: String
)
