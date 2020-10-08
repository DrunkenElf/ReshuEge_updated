package com.ilnur.backend

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.*

//https://mathb-ege.sdamgia.ru/api?type=get_test&id=10687172&protocolVersion=1 getTest tasks id
//https://mathb-ege.sdamgia.ru/api?type=predefined_tests&protocolVersion=1 return id with
//https://mathb-ege.sdamgia.ru/api?type=get_task&data=510693&protocolVersion=1 return task data


interface API {

    @POST("/api")
    suspend fun auth(
            @Query("user") login: String,
            @Query("password") password: String,
            @Query("type") type: String = "login",
            @Query("protocolVersion") protocolVersion: Int = 1,
    ): Response<ResponseRest>

    @GET
    suspend fun getPredefTests(@Url url: String): Response<ResponsePred>
    @GET
    suspend fun getListOfTests(@Url url: String): Response<ResponseListOfTests>

    @GET
    suspend fun getTask(@Url url: String): Response<ResponseTask>

    @GET
    @Streaming
    fun getImage(@Url url: String): Call<ResponseBody>
}

@Keep
data class ResponsePred(
        @SerializedName("data") val data: String?
)
@Keep
data class ResponseListOfTests(
        @SerializedName("data") val data: List<Int>?
)
@Keep
data class ResponseTask(
        @SerializedName("data") val data: TaskResp?
)

@Keep
data class TaskResp(
        @SerializedName("stamp") val stamp: String?,//
        @SerializedName("id") val id: Int,
        @SerializedName("type")  val type: Int,
        @SerializedName("task") val task: Int,
        @SerializedName("category") val category: Int,
        @SerializedName("body")  val body: String?,
        @SerializedName("solution") val solution: String?,
        @SerializedName("base_id")  val base_id: String?,
        @SerializedName("answer")  val answer: String?,
        @SerializedName("likes")  val likes: List<Int>?
)

@Keep
data class ResponseRest(
        @SerializedName("error") val error: String?,
        @SerializedName("data") val data: RespData?
)

@Keep
data class RespData(
        @SerializedName("session") val session: String
)
