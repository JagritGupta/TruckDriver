//package com.example.skillbee
//
//import kotlinx.coroutines.CoroutineExceptionHandler
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//
///**
// * Copyright (C) Dailyrounds., 2022
// * All rights reserved.
// * Created by Jagrit Gupta on 08/08/23.
// * jagrit@dailyrounds.org
// */
//
//fun CoroutineScope.safeLaunch(
//    launchBody: suspend () -> Unit,
//    errorBody: (errorCode: Int, message: String) -> Unit
//): Job {
//    val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
//        if (throwable is ResponseErrorException) {
//            errorBody.invoke(throwable.error.errorCode, throwable.error.errorMessage)
//        } else if (NetworkUtils.isUnReachIssue(throwable)) {
//            errorBody.invoke(
//                INTERNET_NOT_FOUND,
//                NetworkUtils.wrapUnReachIssue(throwable).error.errorMessage
//            )
//        } else {
//            FirebaseCrashlytics.getInstance().recordException(throwable)
//            AnalyticsAPI.getInstance().push(
//                "crash_record",
//                throwableToMap(throwable),
//                channels = listOf(AnalyticsChannel.CHANNEL_FIREBASE)
//            )
//            errorBody.invoke(UNKNOWN_ERROR, "Something Went wrong")
//        }
//        DebugLog.d("SAFE_LAUNCH", throwable.stackTraceToString())
//    }
//    return this.launch(coroutineExceptionHandler) {
//        launchBody.invoke()
//    }
//}
//
//suspend inline fun <T> safeApiCall(
//    crossinline body: suspend () -> T
//): ResponseResult<T> {
//    return try {
//        // blocking block
//        val users = withContext(Dispatchers.IO) {
//            body()
//        }
//        ResponseResult.Success(users)
//    } catch (e: Exception) {
//        ResponseResult.Failure(e)
//    }
//}
//
//sealed class ResponseResult<out H> {
//    data class Success<out T>(val data: T)
//        : ResponseResult<T>()
//    data class Failure(val error: Throwable)
//        : ResponseResult<Nothing>()
//    object Pending : ResponseResult<Nothing>()
//    object Complete : ResponseResult<Nothing>()
//}