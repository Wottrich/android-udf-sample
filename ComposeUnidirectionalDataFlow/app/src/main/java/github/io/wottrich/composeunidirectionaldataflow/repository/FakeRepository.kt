package github.io.wottrich.myapplication.repository

import github.io.wottrich.myapplication.models.User
import kotlinx.coroutines.delay

class FakeRepository {
    suspend fun saveUser(userName: String): User {
        //fake api delay
        delay(2000)
        return User(userName)
    }
}