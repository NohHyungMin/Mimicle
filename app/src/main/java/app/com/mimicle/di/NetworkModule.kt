package app.com.mimicle.di

import app.com.mimicle.api.ApiInterface
import app.com.mimicle.data.push.PushRepository
import app.com.mimicle.data.push.PushRepositoryImpl
import app.com.mimicle.data.splash.AppMetaRepository
import app.com.mimicle.data.splash.AppMetaRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {
    @Singleton
    @Provides
    fun providePushRepository(apiInterface: ApiInterface): PushRepository {
        return PushRepositoryImpl(apiInterface)
    }

    @Singleton
    @Provides
    fun provideAppMetaRepository(apiInterface: ApiInterface): AppMetaRepository {
        return AppMetaRepositoryImpl(apiInterface)
    }
}