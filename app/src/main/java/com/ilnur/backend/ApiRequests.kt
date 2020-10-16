package com.ilnur.backend

import com.ilnur.DataBase.AppDatabase
import com.ilnur.DataBase.Task
import okhttp3.ResponseBody
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

//https://mathb-ege.sdamgia.ru/api?type=get_test&id=10687172&protocolVersion=1 getTest tasks id
//https://mathb-ege.sdamgia.ru/api?type=predefined_tests&protocolVersion=1 return id with
//https://mathb-ege.sdamgia.ru/api?type=get_task&data=510693&protocolVersion=1 return task data

interface ApiRequests {
    suspend fun auth(
            login: String, password: String, type: String = "login", protocolVersion: Int = 1,
    ): Response<ResponseRest>
}

class ApiRequestsImp @Inject constructor(val api: API, val db: AppDatabase) : ApiRequests {

    override suspend fun auth(login: String, password: String,
                              type: String, protocolVersion: Int): Response<ResponseRest> =
            api.auth(login, password, type, protocolVersion)

    suspend fun getPredefTests(href: String): Response<ResponsePred> =
            api.getPredefTests(get_predefined(href))

    suspend fun getTestKeys(href: String, id: Int): Response<ResponseListOfTests> =
            api.getListOfTests(get_testsKeys(href = href, id = id))

    suspend fun getTask(href: String, data: Int): Response<ResponseTask> =
            api.getTask(get_task(href = href, data = data))

     fun getImage(url: String)= api.getImage(url).execute()

}

fun get_predefined(href: String, type: String = "predefined_tests") =
        "https://$href-ege.sdamgia.ru/api?type=$type&protocolVersion=1"

fun get_testsKeys(href: String, type: String = "get_test", id: Int) =
        "https://$href-ege.sdamgia.ru/api?type=$type&id=$id&protocolVersion=1"

fun get_task(href: String, type: String = "get_task", data: Int) =
        "https://$href-ege.sdamgia.ru/api?type=$type&data=$data&protocolVersion=1"

/*fun TaskResp.toTask(href: String, variant: Int) = Task(
        variant = variant,
        subj = href,
        stamp = stamp,
        id = id,
        type = type,
        task = task,
        category = category,
        body = body, solution = solution, base_id = base_id, answer = answer,
        likes = convertLikes(likes!!)
)*/

fun convertLikes(likes: List<Int>): String {
    val builder = StringBuilder()
    likes.forEach { builder.append("$it&") }
    return builder.dropLast(1).toString()
}