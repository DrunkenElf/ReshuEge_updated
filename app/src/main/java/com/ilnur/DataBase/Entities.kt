package com.ilnur.DataBase

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


@Keep
@Entity(tableName = "tasks", primaryKeys = ["variant", "subj"])
data class Task(
        @SerializedName("variant") @ColumnInfo(name = "variant") var variant: Int,
        @SerializedName("subj") @ColumnInfo(name = "subj") val subj: String,
        @SerializedName("stamp") @ColumnInfo(name = "stamp") val stamp: String?,
        @SerializedName("id") @ColumnInfo(name = "id") val id: Int,
        @SerializedName("type") @ColumnInfo(name = "type") val type: Int,
        @SerializedName("task") @ColumnInfo(name = "task") val task: Int,
        @SerializedName("category") @ColumnInfo(name = "category") val category: Int,
        @SerializedName("body") @ColumnInfo(name = "body") val body: String?,
        @SerializedName("solution") @ColumnInfo(name = "solution") val solution: String?,
        @SerializedName("base_id") @ColumnInfo(name = "base_id") val base_id: Int?,
        @SerializedName("answer") @ColumnInfo(name = "answer") val answer: String?,
        @SerializedName("likes") @ColumnInfo(name = "likes") val likes: String?
)

@Keep
@Entity(primaryKeys = ["id", "subj"])
data class Category(
        @SerializedName("id") @ColumnInfo(name = "id") val id: Int,
        @SerializedName("subj") @ColumnInfo(name = "subj") val subj: String,
        @SerializedName("title") @ColumnInfo(name = "title") val title: String?,
        @SerializedName("parent_id") @ColumnInfo(name = "parent_id") val parent_id: Int?,
        @SerializedName("reversible") @ColumnInfo(name = "reversible") val reversible: Int?,
        @SerializedName("order") @ColumnInfo(name = "order") val order: Int?,
        @SerializedName("stamp") @ColumnInfo(name = "stamp") val stamp: String?
)

@Keep
@Entity(primaryKeys = ["id", "subj"])
data class Card(
        @SerializedName("id") @ColumnInfo(name = "id") val id: Int,
        @SerializedName("subj") @ColumnInfo(name = "subj") val subj: String,
        @SerializedName("avers") @ColumnInfo(name = "avers") val avers: String?,
        @SerializedName("revers") @ColumnInfo(name = "revers") val revers: String?,
        @SerializedName("category_id") @ColumnInfo(name = "category_id") val category_id: Int?,
        @SerializedName("result") @ColumnInfo(name = "result") val result: Int?,
        @SerializedName("result_stamp") @ColumnInfo(name = "result_stamp") val result_stamp: String?
)
@Keep
@Entity(primaryKeys = ["href"])
data class Subject(
        @SerializedName("title")@ColumnInfo(name = "title") val title: String,
        @SerializedName("href")@ColumnInfo(name = "href") val href: String,
        @SerializedName("isAdded")@ColumnInfo(name = "isAdded") var isAdded: Boolean = false,
        @SerializedName("timeUpdated")@ColumnInfo(name = "timeUpdated") var timeUpdated: String? = null
)

@Keep
@Entity(tableName = "user")
data class User(
        @SerializedName("login") @PrimaryKey val login: String,
        @SerializedName("password") @ColumnInfo(name = "password") val password: String?,
        @SerializedName("session_id") @ColumnInfo(name = "session_id") val session_id: String?,
        @SerializedName("logged") @ColumnInfo(name = "logged") var logged: Boolean = false
)