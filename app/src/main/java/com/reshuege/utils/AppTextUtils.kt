package com.reshuege.utils

import android.text.TextUtils

import org.json.JSONObject

import java.util.regex.Pattern

/**
 * Набор вспомогательных методов для работы с текстом и строками
 */
object AppTextUtils {
    operator fun get(json: JSONObject, target: String): String {
        var result = json.optString(target)
        if (isEmpty(result)) {
            result = ""
        }
        return result
    }

    fun isEmpty(target: String): Boolean {
        return TextUtils.isEmpty(target) || "null".equals(target, ignoreCase = true)
    }

    /**
     * Проверка валидности формата email [FormatValidator.validate]
     *
     * @param email [String]
     * @return true - если формат соответствует формату [FormatValidator.EMAIL_PATTERN]
     */

    /**
     * Класс для работы с регулярными выражениями
     */
    object FormatValidator {
        val EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"

        fun validate(target: String, pattern: String): Boolean {
            return Pattern
                    .compile(pattern)
                    .matcher(target)
                    .matches()
        }

    }



}
