package com.reshuege.DataBase

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ThemeDao {

    @Query("SELECT * FROM themes WHERE subj = :href ORDER BY id ASC")
    fun getThemes(href: String): LiveData<List<Theme>>

    @Query("SELECT * FROM themes WHERE subj = :href LIMIT 1")
    fun getTheme(href: String): List<Theme>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(theme: Theme)

    @Update
    fun update(theme: Theme)

    @Delete
    fun delete(theme: Theme)
}

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE subj = :href AND variant = :variant ORDER BY task ASC")
    fun getTasks(href: String, variant: Int): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE subj = :href AND variant = :variant AND task = :taskNum ORDER BY task ASC")
    fun getTask(href: String, variant: Int, taskNum: Int): Task


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(task: Task)


    @Delete
    fun delete(task: Task)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category WHERE subj =:predmet AND parent_id =:parent_id")
    fun getTopics(predmet: String, parent_id: Int = 0): List<Category>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(category: Category)

}

@Dao
interface CardDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(card: Card)
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subject")
    fun getSubjects(): List<Subject>

    @Query("SELECT * FROM subject WHERE href = :href LIMIT 1")
    fun getSubject(href: String): Subject

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(subject: Subject)

    @Delete
    fun delete(subject: Subject)
}

@Dao
interface SubjectMainDao {
    @Query("SELECT * FROM subjectmain")
    fun getSubjects(): List<SubjectMain>

    @Query("SELECT * FROM subjectmain WHERE href = :href LIMIT 1")
    fun getSubject(href: String): SubjectMain

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(subject: SubjectMain)

    @Update
    fun update(subject: SubjectMain)

    @Delete
    fun delete(subject: SubjectMain)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM user WHERE login = :login")
    fun getByLogin(login: String): LiveData<List<User>>

    @Query("SELECT * FROM user WHERE session_id = :session_id")
    fun getBySessionId(session_id: String): LiveData<List<User>>

    @Query("SELECT * FROM user LIMIT 1")
    fun getUser(): LiveData<User>

    @Query("SELECT * FROM user LIMIT 1")
    fun getUserDb(): User?

    @Query("SELECT * FROM user")
    fun getUserList(): List<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)

    @Delete
    fun delete(user: User)
}