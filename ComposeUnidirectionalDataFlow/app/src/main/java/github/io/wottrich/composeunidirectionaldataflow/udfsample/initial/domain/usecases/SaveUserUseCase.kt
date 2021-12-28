package github.io.wottrich.composeunidirectionaldataflow.udfsample.initial.domain.usecases

import github.io.wottrich.myapplication.repository.FakeRepository
import github.io.wottrich.myapplication.models.User

class SaveUserUseCase(
    private val repository: FakeRepository = /* Use DI to provide repository*/ FakeRepository()
) {
    suspend operator fun invoke(userName: String): User {
        return repository.saveUser(userName)
    }
}