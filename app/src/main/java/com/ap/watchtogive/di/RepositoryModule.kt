package com.ap.watchtogive.di
import com.ap.watchtogive.data.repository.CharitiesRepository
import com.ap.watchtogive.data.repository.CharitiesRepositoryImpl
import com.ap.watchtogive.data.repository.LocationRepository
import com.ap.watchtogive.data.repository.LocationRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCharitiesRepository(
        impl: CharitiesRepositoryImpl,
    ): CharitiesRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        impl: LocationRepositoryImpl,
    ): LocationRepository
}
