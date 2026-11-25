package tv.trakt.trakt.common.model

enum class UserTier {
    Free,
    Vip,
    Pro,
    Veteran,
    Elite,
    Legend,
    ;

    companion object {
        fun from(
            isVip: Boolean,
            veteran: VipVeteran?,
        ): UserTier {
            if (!isVip) return Free

            return when (veteran?.title) {
                null -> Vip
                VipVeteran.Title.Pro -> Pro
                VipVeteran.Title.Veteran -> Veteran
                VipVeteran.Title.Elite -> Elite
                VipVeteran.Title.Legend -> Legend
            }
        }
    }
}
