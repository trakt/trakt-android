package tv.trakt.trakt.core.report.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.core.report.ReportViewModel
import tv.trakt.trakt.core.report.model.ReportTarget
import tv.trakt.trakt.core.report.usecases.ReportUseCase

internal val reportModule = module {
    factoryOf(::ReportUseCase)

    viewModel { (target: ReportTarget) ->
        ReportViewModel(
            target = target,
            reportUseCase = get(),
        )
    }
}
