package wt.cr.com.mynamegame.infrastructure.repository

import io.reactivex.Observable
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import wt.cr.com.mynamegame.domain.model.MyModel
import wt.cr.com.mynamegame.infrastructure.di.WTServiceLocator
import wt.cr.com.mynamegame.infrastructure.network.client.ApiClient

interface HumanRepo {
    suspend fun getProfiles() : Deferred<Observable<MyModel.Query>>
}
class HumanRepository : HumanRepo {
    override suspend fun getProfiles(): Deferred<Observable<MyModel.Query>> {
        return async{WTServiceLocator.resolve(ApiClient::class.java).getProfiles()}
    }
}

