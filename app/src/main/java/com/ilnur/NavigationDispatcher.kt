package com.ilnur

import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

/*
@ActivityRetainedScoped
class NavigationDispatcher @Inject constructor() {
    private val navigationEmitter: EventEmitter<NavigationCommand> = EventEmitter()
    val navigationCommands: EventSource<NavigationCommand> = navigationEmitter

    fun emit(navigationCommand: NavigationCommand) {
        navigationEmitter.emit(navigationCommand)
    }
}*/