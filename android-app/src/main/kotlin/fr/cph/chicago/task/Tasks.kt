package fr.cph.chicago.task

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

fun refreshTask(): Observable<Long> {
    return Observable.interval(1, 10000, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.computation())
}
