package com.ilnur.viewModel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.ilnur.DataBase.User
import com.ilnur.backend.ApiRequestsImp
import com.ilnur.repository.LoginRepository
import kotlinx.coroutines.launch


class LoginViewModel @ViewModelInject constructor (private val repository: LoginRepository,
                                                   var apiRequests: ApiRequestsImp) : ViewModel() {
    //val api: API by inject(API::class.java)
    //@Inject lateinit var apiRequests: ApiRequestsImp

    /*fun getUser(): LiveData<User> = liveData {
        val res = repository.getUserDb().value
        res?.let {
            emit(res)
        }
    }*/
    //this will return list of users

    fun addOrUpdateUser(user: User) = repository.addOrUpdateUser(user) // adds or replace old row

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> get() = _loginForm

    private val _loginResult = MutableLiveData<LoginState>()
    val loginResult: LiveData<LoginState> get() = _loginResult

    init {
        _loginForm.value = LoginFormState()
        _loginResult.value = LoginState.DEFAULT
    }

    fun updateLoginState(state: LoginFormState) {
        _loginForm.value = state.copy()
    }

    fun updateResultState(state: LoginState) {
        _loginResult.postValue(state)
    }


    fun login(login: String, password: String) {
        //проверяем пароль и логин
        viewModelScope.launch {
            //тут проверяем на подключение кинету
            if (repository.isNetworkConnected()) {
                apiRequests.auth(login, password).let {
                    if (it.isSuccessful) {
                        val temp = it.body()
                        if (temp?.data != null && temp.data.session != "") {
                            updateResultState(LoginState.SUCCESS)
                            repository.addOrUpdateUser(User(login, password, temp.data.session, true))
                        }
                    } else {
                        _loginResult.postValue(LoginState.ERROR)
                    }
                }
            } else _loginResult.postValue(LoginState.NO_INTERNET)
        }
    }


    fun loginDataChanged(login: String, password: String) {

    }

    private fun isLoginValid(login: String): Boolean {
        return true
    }

    fun isLogged(): LiveData<Boolean> = liveData {
        val res = repository.isLogged()
        emit(res)
    }

}

fun toString(state: LoginFormState) = state.toString()

data class LoginFormState(
        val login: String = "",
        val password: String = "",
        val checking: Boolean = false, //when connects to server or check db
        val formValid: Boolean = false //responsible form validity of login and pass
        //val isDataValid: Boolean = false // disable login button unless finish
)


enum class LoginState {
    SUCCESS,
    ERROR,
    NO_INTERNET,
    WRONG_LOG_OR_PAS,
    DEFAULT
}

fun checkLoginState() {}