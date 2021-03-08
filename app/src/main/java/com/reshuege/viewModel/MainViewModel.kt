package com.reshuege.viewModel

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.reshuege.Adapters.DwnCurr
import com.reshuege.DataBase.SubjectMain
import com.reshuege.DataBase.User
import com.reshuege.repository.MainRepository
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

class MainViewModel @ViewModelInject constructor(
    private val repository: MainRepository,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    private val _itemSelected = MutableLiveData<Stack<String>>()

    val itemSelected: LiveData<Stack<String>> get() = _itemSelected

    init {
        _itemSelected.postValue(getItemSelectedStack())
        _itemSelected.observeForever {
            Log.d("MAIN", "stack observeForever")
            updateStackState()
        }

    }

    private fun updateStackState(){
        if (!_itemSelected.value.isNullOrEmpty())
            state.set("stack", _stackToString())
    }

    private fun getItemSelectedStack(): Stack<String>{
        val temp = (state.get<String>("stack") ?: "РЕШУ ЕГЭ").split(", ")
        Log.d("MAIN viewModel", "stack from state $temp")
        val stack = Stack<String>()
        temp.forEach { stack.push(it.trim { (it == '[' || it == ']') }) }
        Log.d("MAIN", "stack asd")
        return stack
    }


    private fun _stackToString(): String{
        val str = _itemSelected.value.toString()
        Log.d("MAIN","stackToString $str")
        return str
    }


    val _title = MutableLiveData<String>()
    val title: LiveData<String> get() = _title

    val _curr_download = MutableLiveData<DwnCurr>()
    val curr_download: LiveData<DwnCurr> get() = _curr_download

    fun updateCurrDwn(curr: DwnCurr) {
        _curr_download.postValue(curr)
    }

    fun push(item: String) {
        Log.d("MAIN viewModel", "push; prevVal ${_itemSelected.value?.peek()}")
        _itemSelected.value?.push(item)
    }

    fun peek() = itemSelected.value?.peek()

    fun pop() : String{
        Log.d("MAIN", "pop ${_itemSelected.value?.peek()}")
        return _itemSelected.value?.pop().toString()
    }

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

    fun updateSubjects() =
        CoroutineScope(Dispatchers.IO).launch { _subjects.postValue(repository.getSubjects()) }


    fun dwnSubject(name: String, href: String) {
        repository.getSubject(href, name)
    }

    fun getAllSubjects() {
        CoroutineScope(Dispatchers.IO).launch {
            repository.getAllSubjects()
        }
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