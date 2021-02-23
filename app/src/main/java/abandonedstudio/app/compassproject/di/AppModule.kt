package abandonedstudio.app.compassproject.di

import abandonedstudio.app.compassproject.model.Destination
import abandonedstudio.app.compassproject.model.DestinationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDestinationRepository() = DestinationRepository(Destination())


}