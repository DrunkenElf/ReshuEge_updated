package com.ilnur.viewModel

import android.os.Parcelable
import android.util.Log
import androidx.annotation.Keep
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.ilnur.DataBase.User
import com.ilnur.backend.ApiRequestsImp
import com.ilnur.repository.LoginRepository
import com.ilnur.viewModel.LoginState.*
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.launch


class LoginViewModel @ViewModelInject constructor(
    private val repository: LoginRepository,
    var apiRequests: ApiRequestsImp,
    @Assisted private val savedState: SavedStateHandle,
) : ViewModel(), LifecycleObserver {
    //final val
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
    val loginFormState: LiveData<LoginFormState>
        get() = _loginForm

    private val _loginResult = MutableLiveData<LoginState>()
    val loginResult: LiveData<LoginState>
        get() = _loginResult


    init {
        _loginForm.postValue(getLoginState())
        _loginResult.postValue(getLoginResult())
    }

    fun updateLoginState(state: LoginFormState) {
        // _loginForm.value = state.copy()
        _loginForm.postValue(state)
        saveLoginState(state)
    }

    fun updateResultState(state: LoginState) {
        //_loginResult.postValue(state)
        _loginResult.postValue(state)
        saveLoginResult(state)
        updateLoginState(getLoginState().copy(checking = false))
    }

    fun addMainSubjs() {
        repository.addMainSubjs()
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
                            Log.d("success", temp.toString())
                            updateResultState(SUCCESS)
                            repository.addOrUpdateUser(
                                User(
                                    login,
                                    password,
                                    temp.data.session,
                                    true,
                                )
                            )
                        } else {
                            Log.d("response OBS", temp.toString())
                            updateResultState(WRONG_LOG_OR_PAS)
                        }
                    } else {
                        updateResultState(ERROR)
                        //_loginResult.postValue(LoginState.ERROR)
                        //saveLoginResult(ERROR)
                    }
                }
            } else
                updateResultState(NO_INTERNET)
            //_loginResult.postValue(LoginState.NO_INTERNET)
            //saveLoginResult(NO_INTERNET)
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

    private fun getLoginState() = LoginFormState(
        login = savedState.get<String>("login") ?: "",
        password = savedState.get<String>("password") ?: "",
        checking = savedState.get<Boolean>("checking") ?: false,
        formValid = savedState.get<Boolean>("formValid") ?: false,
    )

    private fun saveLoginState(state: LoginFormState? = null) = with(savedState) {
        Log.d("saveLoginState", state.toString())
        set("login", state?.login ?: "")
        set("password", state?.password ?: "")
        set("checking", state?.checking ?: false)
        set("formValid", state?.formValid ?: false)
    }

    private fun saveLoginResult(state: LoginState? = null) {
        val temp = (state ?: DEFAULT).name
        savedState.set("loginResult", temp)
    }

    private fun getLoginResult(): LoginState =
        when (savedState.get<String>("loginResult")) {
            DEFAULT.name -> DEFAULT
            ERROR.name -> ERROR
            NO_INTERNET.name -> NO_INTERNET
            WRONG_LOG_OR_PAS.name -> WRONG_LOG_OR_PAS
            SUCCESS.name -> SUCCESS
            else -> DEFAULT
        }
}

fun toString(state: LoginFormState) = state.toString()

@Keep
@Parcelize
data class LoginFormState(
    val login: String = "",
    val password: String = "",
    val checking: Boolean = false, //when connects to server or check db
    val formValid: Boolean = false //responsible form validity of login and pass
    //val isDataValid: Boolean = false // disable login button unless finish
) : Parcelable

@Keep
@Parcelize
enum class LoginState : Parcelable {
    SUCCESS,
    ERROR,
    NO_INTERNET,
    WRONG_LOG_OR_PAS,
    DEFAULT
}

fun checkLoginState() {}