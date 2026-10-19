package tv.trakt.trakt.common.core.sync.changes.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.core.sync.changes.DataChangesBus

val dataChangesModule = module {
    singleOf(::DataChangesBus) { bind<DataChanges>() }
}
