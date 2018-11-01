package wt.cr.com.mynamegame.infrastructure.network

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface ProfileLocalDataSource<T> {
    fun getAllProfiles(): Flowable<List<T>>
    fun insert(list: List<T>): Completable
}

interface ProfileRemoteDataSource<T> {
    fun getAllProfiles(): Single<List<T>>
}