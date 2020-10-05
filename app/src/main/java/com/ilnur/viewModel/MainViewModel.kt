package com.ilnur.viewModel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ilnur.DataBase.SubjectMain
import com.ilnur.DataBase.User
import com.ilnur.repository.MainRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(private val repository: MainRepository) : ViewModel(){

    val _subjects = MutableLiveData<List<SubjectMain>>()
    val subjects: LiveData<List<SubjectMain>> get() = _subjects //список предметов

    val user = MutableLiveData<User?>()

    init {
        CoroutineScope(Dispatchers.IO).launch{ _subjects.postValue(repository.getSubjects())}
    }

    val _currentSubj = MutableLiveData<SubjectMain>()
    val currentSubj: LiveData<SubjectMain> get() = _currentSubj

    fun getUserDb(){
        CoroutineScope(Dispatchers.IO).launch {   user.postValue(repository.getUserDb())}
    }

    fun selectSubject(subject: SubjectMain){
        _currentSubj.postValue(subject)
    }


}