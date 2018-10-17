package wt.cr.com.mynamegame.infrastructure.common.utils

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.support.annotation.MainThread

class LiveDataAction {

    private val liveData = MutableLiveData<Boolean>()

    @MainThread
    fun observe(owner: LifecycleOwner, observer: () -> Unit) {
        liveData.observe(owner, Observer<Boolean> { value ->
            value?.let {
                observer()
            }
        })
    }

    /**
     * This function allows easy testing without needing a LifecycleOwner.
     */
    @MainThread
    fun observeForever(observer: () -> Unit) {
        liveData.observeForever { value ->
            value?.let {
                observer()
            }
        }
    }

    @MainThread
    fun actionOccurred() {
        liveData.value = true
    }

    fun getValue(): Boolean? {
        return liveData.value
    }
}