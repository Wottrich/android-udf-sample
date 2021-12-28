package github.io.wottrich.composeunidirectionaldataflow.flow

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow


class SingleShotEventBus<T> {
    private val _events = Channel<T>()
    val events = _events.receiveAsFlow() // expose as flow

    suspend fun emit(event: T) {
        _events.send(event) // suspends on buffer overflow
    }
}