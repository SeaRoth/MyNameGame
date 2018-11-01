package wt.cr.com.mynamegame.infrastructure.repository

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import io.reactivex.Flowable
import wt.cr.com.mynamegame.domain.model.ProfileError
import wt.cr.com.mynamegame.infrastructure.common.utils.left
import wt.cr.com.mynamegame.infrastructure.common.utils.right
import wt.cr.com.mynamegame.infrastructure.network.ProfileLocalDataSource
import wt.cr.com.mynamegame.infrastructure.network.ProfileRemoteDataSource

class HumanRepository<T>(
        private val local: ProfileLocalDataSource<T>,
        private val remote: ProfileRemoteDataSource<T>
) {
    private fun fetchProfilesLocal(): Flowable<Either<ProfileError.EmptyResultsError, List<T>>> =
        local.getAllProfiles()
                .map {
                    when(it.isEmpty()){
                        true -> Either.left(ProfileError.EmptyResultsError)
                        false -> Either.right(it)
                    }
                }

    private fun fetchProfilesRemote(): Flowable<Either<ProfileError, List<T>>> =
            remote.getAllProfiles()
                    .map { right<ProfileError, List<T>>(it) }
                    .onErrorReturn { left<ProfileError, List<T>>(ProfileError.NetworkError) }
                    .flatMapPublisher { saveProfiles(it) }

    private fun saveProfiles(
            results: Either<ProfileError, List<T>>
    ): Flowable<Either<ProfileError, List<T>>> =
            results.fold({
                Flowable.just(results)
            },{
                local.insert(it).toFlowable<Either<ProfileError, List<T>>>()
            })
}