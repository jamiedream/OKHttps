package com.followme.ok

import android.util.Log
import okhttp3.*
import java.io.IOException
import java.io.InputStream
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.*
import okhttp3.OkHttpClient


class OKHttpUtil(inputStream: InputStream) {

    private val TAG = this.javaClass.simpleName
    private lateinit var trustManager: X509TrustManager

    private val CONNECT_TIME_OUT = 5L
    private val READ_TIME_OUT = 5L
    private val WRITE_TIME_OUT = 5L
    private var okHttpClient: OkHttpClient

    init{

        //trust all
        val builder =
                OkHttpClient.Builder()
                        .connectTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS)
                        .readTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
                        .writeTimeout(WRITE_TIME_OUT, TimeUnit.SECONDS)
                        .addInterceptor(HttpInterceptor())

        val factory = getTrustClient(inputStream)
        when(factory != null){
            true -> {
                builder.
                        sslSocketFactory(factory, trustManager)
                        .hostnameVerifier { _, _ -> true }
            }
        }

        okHttpClient = builder.build()

    }

//    private fun createSSLSocketFactory(): SSLSocketFactory?{
//        var sslSocketFactory: SSLSocketFactory? = null
//        try {
//            trustManager = TrustManager()
//            val sc = SSLContext.getInstance("TLS")
//            sc.init(null, arrayOf(trustManager), SecureRandom())
//            sslSocketFactory = sc.socketFactory
//        } catch (ignored: Exception) {
//            ignored.printStackTrace()
//        }
//
//
//        return sslSocketFactory
//    }

    private fun getTrustClient(certificate: InputStream): SSLSocketFactory? {
        var sslSocketFactory: SSLSocketFactory?
        try {
            trustManager = trustManagerForCertificates(certificate)
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, arrayOf(trustManager), null)
            sslSocketFactory = sslContext.socketFactory
        } catch (e: GeneralSecurityException) {
            throw RuntimeException(e)
        }

        return sslSocketFactory
    }


    @Throws(GeneralSecurityException::class)
    private fun trustManagerForCertificates(`in`: InputStream): X509TrustManager {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val certificates = certificateFactory.generateCertificates(`in`)
        if (certificates.isEmpty()) {
            throw IllegalArgumentException("expected non-empty set of trusted certificates")
        }
        val password = "password".toCharArray() // Any password will work.
        val keyStore = newEmptyKeyStore(password)
        var index = 0
        for (certificate in certificates) {
            val certificateAlias = Integer.toString(index++)
            keyStore.setCertificateEntry(certificateAlias, certificate)
        }
        // Use it to build an X509 trust manager.
        val keyManagerFactory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm())
        keyManagerFactory.init(keyStore, password)
        val trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)
        val trustManagers = trustManagerFactory.trustManagers
        if (trustManagers.size != 1 || trustManagers[0] !is X509TrustManager) {
            throw IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers))
        }
        return trustManagers[0] as X509TrustManager
    }

    @Throws(GeneralSecurityException::class)
    private fun newEmptyKeyStore(password: CharArray): KeyStore {
        try {
            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            val `in`: InputStream? = null // By convention, 'null' creates an empty key store.
            keyStore.load(null, password)
            return keyStore
        } catch (e: IOException) {
            throw AssertionError(e)
        }

    }

    /**
     * Method: POST
     * Build parameter
     * */
    private lateinit var formBodyBuilder: FormBody.Builder

    fun setFormBodyBuilder(){
        formBodyBuilder = FormBody.Builder()
    }

    fun buildParameter(key: String, value: String){
        formBodyBuilder.add(key, value)
    }

    fun getFormBodyBuilder(): FormBody.Builder{
        return formBodyBuilder
    }

    /**
     * Async in worker thread
     * */
    fun newAsync(request: Request, listener: ICallbackListener){
        okHttpClient.newCall(request).enqueue(object: Callback{
            override fun onResponse(call: Call, response: Response) {
                Log.i(TAG, response.message())
                Log.i(TAG, response.code().toString() + "")
                listener.onResult(response.isSuccessful, response.body()!!.string())
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.d(TAG, "${call.isCanceled}  ${call.isExecuted} ")
                listener.onResult(false, e.message.toString())
            }

        }
        )
    }





}