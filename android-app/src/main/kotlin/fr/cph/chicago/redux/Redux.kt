package fr.cph.chicago.redux

import org.rekotlin.Action
import org.rekotlin.StateType
import org.rekotlin.Store

data class AppState(val counter: Int = 0) : StateType

data class CounterActionIncrease(val unit: Unit = Unit) : Action
data class CounterActionDecrease(val unit: Unit = Unit) : Action

val mainStore = Store(
    reducer = ::counterReducer,
    state = null
)

fun counterReducer(action: Action, state: AppState?): AppState {
    // if no state has been provided, create the default state
    var state = state ?: AppState()

    when (action) {
        is CounterActionIncrease -> {
            state = state.copy(counter = state.counter + 1)
        }
        is CounterActionDecrease -> {
            state = state.copy(counter = state.counter - 1)
        }
    }

    return state
}
