package wt.cr.com.mynamegame.infrastructure.network

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import wt.cr.com.mynamegame.domain.model.Headshot
import wt.cr.com.mynamegame.domain.model.Profile
import wt.cr.com.mynamegame.domain.model.Social
import wt.cr.com.mynamegame.infrastructure.common.ComicSchedulers
import wt.cr.com.mynamegame.infrastructure.network.storage.ProfileDatabase

class SearchRemoteDataSource(
        private val api: ApiClient
) : ProfileRemoteDataSource<Profile> {
    override fun getAllProfiles(): Single<List<Profile>> = api.getProfilesNew()
            .subscribeOn(ComicSchedulers.network)
            .map{response ->
                response.data
                        .map{ wtProfile ->
                            Profile(id = wtProfile.id,
                                    firstName = wtProfile.firstName,
                                    lastName = wtProfile.lastName,
                                    headshot = Headshot(url = wtProfile.headshot.url),
                                    socialLinks = wtProfile.socialLinks.map{
                                        Social(
                                                it.type,
                                                it.callToAction,
                                                it.url
                                        )
                                    }
                                    )
                        }
            }
}

class SearchLocalDataSource(
        private val database: ProfileDatabase
) : ProfileLocalDataSource<Profile> {
    override fun getAllProfiles(): Flowable<List<Profile>> = database.profileDao()
            .returnAllProfiles()
            .subscribeOn(ComicSchedulers.database)
            .subscribeOn(Schedulers.io())
            .map { profileList ->
                profileList.map {
                    Profile(
                            id = it.id,
                            firstName = it.firstName,
                            lastName = it.lastName,
                            headshot = Headshot(it.url),
                            socialLinks = emptyList()) } }

    override fun insert(list: List<Profile>): Completable =
            Completable.fromAction {
                database.profileDao().insert(list)
            }.subscribeOn(ComicSchedulers.database)

}