package com.ap.watchtogive.di

import com.ap.watchtogive.data.repository.AdsRepository
import com.ap.watchtogive.data.repository.AdsRepositoryImpl
import com.ap.watchtogive.data.repository.AuthRepository
import com.ap.watchtogive.data.repository.AuthRepositoryImpl
import com.ap.watchtogive.data.repository.CharitiesRepository
import com.ap.watchtogive.data.repository.CharitiesRepositoryImpl
import com.ap.watchtogive.data.repository.LocationRepository
import com.ap.watchtogive.data.repository.LocationRepositoryImpl
import com.ap.watchtogive.data.repository.UserRepository
import com.ap.watchtogive.data.repository.UserRepositoryImpl
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
    abstract fun bindCharitiesRepository(impl: CharitiesRepositoryImpl): CharitiesRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository

    @Binds
    @Singleton
    abstract fun bindAdsRepository(impl: AdsRepositoryImpl): AdsRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
