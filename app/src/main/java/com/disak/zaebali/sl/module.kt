package com.disak.zaebali.sl

import com.disak.zaebali.repository.FileRepository
import com.disak.zaebali.repository.ResourceRepository
import com.disak.zaebali.repository.SpacesRepository
import com.disak.zaebali.repository.datasource.FileDataSource
import com.disak.zaebali.repository.datasource.ResourceDataSource
import com.disak.zaebali.repository.datasource.SpacesDataSource
import com.disak.zaebali.ui.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModules = module {

    factory { SpacesDataSource() }

    single { SpacesRepository(spacesDataSource = get()) }

    factory { ResourceDataSource(context = androidContext()) }

    single { ResourceRepository(resourceDataSource = get()) }

    factory { FileDataSource(context = androidContext()) }

    single { FileRepository(fileDataSource = get()) }

    viewModel {
        MainViewModel(
            spacesRepository = get(),
            resourceRepository = get(),
            fileRepository = get()
        )
    }
}