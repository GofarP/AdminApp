package com.gopro.AdminApp.utils

import com.google.gson.Gson
import com.gopro.AdminApp.model.dto.response.error.ErrorResponse
import retrofit2.Response

data class ApiErrorResult(
    val generalErrorMessage: String?,
    val fieldErrors: Map<String, String>
)

object ApiErrorHandler{
    fun <T> parseError(response: Response<T>, defaultMessage: String): ApiErrorResult {
        val errorJson=response.errorBody()?.string()

        if(errorJson!=null){
            try{
                val errorResponse = Gson().fromJson(errorJson, ErrorResponse::class.java)
                val fieldErrors = mutableMapOf<String, String>()
                errorResponse.errors?.forEach { (field, messages) ->
                    if (messages.isNotEmpty()) {
                        fieldErrors[field] = messages.first()
                    }
                }
                var generalMessage: String? = null
                if (fieldErrors.isEmpty()) {
                    generalMessage = errorResponse.message ?: defaultMessage
                }
                return ApiErrorResult(generalMessage, fieldErrors)
            }catch (e: Exception){

            }
        }
        return ApiErrorResult("$defaultMessage: Format tidak valid", emptyMap())
    }
}