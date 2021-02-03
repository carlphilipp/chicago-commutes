/**
 * Copyright 2021 Carl-Philipp Harmant
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.rx

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.Callable
import timber.log.Timber

object RxUtil {

    fun <T> singleFromCallable(supplier: Callable<T>, subscribeOn: Scheduler = Schedulers.io(), observeOn: Scheduler = AndroidSchedulers.mainThread()): Single<T> {
        return Single.fromCallable(supplier)
            .subscribeOn(subscribeOn)
            .observeOn(observeOn)
    }

    fun <T> handleListError(): (Throwable) -> List<T> = { throwable ->
        Timber.e(throwable)
        ArrayList()
    }

    fun <T, R> handleMapError(): (Throwable) -> Map<T, R> = { throwable ->
        Timber.e(throwable)
        emptyMap()
    }
}
