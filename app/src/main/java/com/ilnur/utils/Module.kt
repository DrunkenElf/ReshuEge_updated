package com.ilnur.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ilnur.DataBase.*
import com.ilnur.backend.API
import com.ilnur.backend.ApiRequests
import com.ilnur.backend.ApiRequestsImp
import com.ilnur.backend.Downloaders
import com.ilnur.repository.LoginRepository
import com.ilnur.repository.MainRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@InstallIn(ApplicationComponent::class)
@Module
object DbModule {

    @Provides
    fun provideUserDao(@ApplicationContext context: Context): UserDao = AppDatabase(context).userDao()
    @Provides
    fun provideCardDao(@ApplicationContext context: Context): CardDao = AppDatabase(context).cardDao()
    @Provides
    fun provideCategoryDao(@ApplicationContext context: Context): CategoryDao = AppDatabase(context).categoryDao()
    @Provides
    fun provideSubjectDao(@ApplicationContext context: Context): SubjectDao = AppDatabase(context).subjectDao()

    @Provides
    fun provideSubjectMainDao(@ApplicationContext context: Context): SubjectMainDao = AppDatabase(context).subjectMainDao()

    @Provides
    fun provideTaskDao(@ApplicationContext context: Context): TaskDao = AppDatabase(context).taskDao()

    @Singleton
    @Provides
    fun provideDb(@ApplicationContext context: Context): AppDatabase = AppDatabase(context)

}

@InstallIn(ApplicationComponent::class)
@Module
object RetrofitModule {
    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient) : Retrofit = Retrofit.Builder()
            .baseUrl("https://ege.sdamgia.ru")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.NONE
        return OkHttpClient.Builder()
                .addInterceptor(logger)
                .readTimeout(100, TimeUnit.SECONDS)
                .connectTimeout(100, TimeUnit.SECONDS)
                .build()
    }

    @Provides
    fun provideGson(): Gson = GsonBuilder().setLenient().create()

    @Singleton
    @Provides
    fun provideAPI(retrofit: Retrofit): API = retrofit.create(API::class.java)

    @Singleton
    @Provides
    fun provideApiRequests(apiRequests: ApiRequestsImp): ApiRequests = apiRequests


}
@InstallIn(ApplicationComponent::class)
@Module
object SettingsModule{
    @Singleton
    @Provides
    fun provideSettings(@ApplicationContext context: Context): SettingsImp = SettingsImp(context)
}

@InstallIn(ApplicationComponent::class)
@Module
object RepositoryModule {
    @Singleton
    @Provides
    fun provideLoginRepository(@ApplicationContext context: Context, userDao: UserDao) = LoginRepository(context, userDao)

    @Singleton
    @Provides
    fun provideMainRepository(@ApplicationContext context: Context, db: AppDatabase, downloaders: Downloaders) =
            MainRepository(context, db, downloaders)

   /* @Singleton
    @Provides
    fun provideMainRepository(@ApplicationContext context: Context, userDao: UserDao, subjectDao: SubjectDao) =
            MainRepository(context, userDao, subjectDao)

    @Singleton
    @Provides
    fun provideTopicsRepository(@ApplicationContext context: Context, categoryDao: CategoryDao) = TopicsRepository(context, categoryDao)*/
}

@InstallIn(ApplicationComponent::class)
@Module
object NetworkModule {
    @Singleton
    @Provides
    fun provideDownloader(@ApplicationContext context: Context,apiRequests: ApiRequestsImp, db: AppDatabase) = Downloaders(context, apiRequests, db)


   /* @Singleton
    @Provides
    fun provideMainRepository(@ApplicationContext context: Context, userDao: UserDao, subjectDao: SubjectDao) =
            MainRepository(context, userDao, subjectDao)

    @Singleton
    @Provides
    fun provideTopicsRepository(@ApplicationContext context: Context, categoryDao: CategoryDao) = TopicsRepository(context, categoryDao)*/
}

