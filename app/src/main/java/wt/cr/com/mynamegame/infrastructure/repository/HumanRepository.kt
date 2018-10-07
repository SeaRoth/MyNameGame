package wt.cr.com.mynamegame.infrastructure.repository

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import wt.cr.com.mynamegame.domain.model.MyModel
import wt.cr.com.mynamegame.infrastructure.di.WTServiceLocator
import wt.cr.com.mynamegame.infrastructure.network.client.ApiClient

interface HumanRepo {
    suspend fun getProfiles() : Observable<MyModel.Query>?
}
class HumanRepository : HumanRepo {
    override suspend fun getProfiles(): Observable<MyModel.Query>? {
        async {
            WTServiceLocator.resolve(ApiClient::class.java).getProfiles()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { result ->
                                return result
                            },
                            { error -> return null }
                    )
        }
    }
}

