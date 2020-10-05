package com.ilnur.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.ilnur.DataBase.Subject

val subjects: List<Subject> = arrayOf(
        Subject("Русский язык","rus"),
        Subject("Физика","phys"),
        Subject("Математика","math"),
        Subject("Английский язык","en"),
        Subject("История","hist")
).toList()

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}