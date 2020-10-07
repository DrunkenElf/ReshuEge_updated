package com.ilnur.viewModel

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ilnur.DataBase.SubjectMain
import com.ilnur.DataBase.User
import com.ilnur.repository.MainRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

enum class NavItemType {
    FRAGMENT_ACTION,
    FRAGMENT_ID,
    ACTIVITY,
    ACTIVITY_ASYNC
}

data class NavItem(
        val name: String,
        val type: NavItemType
)

class MainViewModel @ViewModelInject constructor(private val repository: MainRepository,
                                                 @Assisted private val state: SavedStateHandle) : ViewModel() {

    val _itemSelected = MutableLiveData<Stack<String>>()
    val itemSelected: LiveData<Stack<String>> get() = _itemSelected


    val _title = MutableLiveData<String>()
    val title: LiveData<String> get() = _title

    //fun pushItem(item: NavItem, any: Any)
    fun push(item: String) = _itemSelected.value?.push(item)

    fun peek() = itemSelected.value?.peek()

    fun pop() = _itemSelected.value?.pop()

    fun setTitle(title: String) {
        _title.value = title
    }
    fun getTitle() = title.value

    val _subjects = MutableLiveData<List<SubjectMain>>()
    val subjects: LiveData<List<SubjectMain>> get() = _subjects //список предметов

    val user = MutableLiveData<User?>()

    init {
        CoroutineScope(Dispatchers.IO).launch { _subjects.postValue(repository.getSubjects()) }
    }

    fun launchCheck(){
        CoroutineScope(Dispatchers.IO).launch {repository.launchCheck()}
    }

    val _currentSubj = MutableLiveData<SubjectMain>()
    val currentSubj: LiveData<SubjectMain> get() = _currentSubj

    fun getUserDb() {
        CoroutineScope(Dispatchers.IO).launch { user.postValue(repository.getUserDb()) }
    }

    fun selectSubject(subject: SubjectMain) {
        _currentSubj.postValue(subject)
    }


}