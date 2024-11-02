package com.alexvinov.movingpics.data.di

import android.content.Context
import com.alexvinov.movingpics.data.PictureStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun providePictureRepository(
        @ApplicationContext appContext: Context,
    ): PictureStorage = PictureStorage(appContext)

}
