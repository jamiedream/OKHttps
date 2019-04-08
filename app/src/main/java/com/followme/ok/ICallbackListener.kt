package com.followme.ok

interface ICallbackListener {
    fun onResult(isSuccess: Boolean, resultString: String)
}