package com.example.droponairdemo.data

import com.example.droponairdemo.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * HTTP client for demo backend calls (login + auth/me).
 * The DropOnAir SDK handles token exchange and key directory internally.
 */
class BackendService {

    private val http = OkHttpClient()
    private val json = "application/json".toMediaType()

    var storedJwt: String? = null
    var storedUserId: String? = null

    suspend fun login(userId: String, displayName: String = userId): LoginResult {
        val body = JSONObject(mapOf("userId" to userId, "displayName" to displayName))
            .toString()
            .toRequestBody(json)

        val req = Request.Builder()
            .url("${BuildConfig.BACKEND_URL}/api/auth/login")
            .post(body)
            .build()

        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val resp = http.newCall(req).execute()
            if (!resp.isSuccessful) throw RuntimeException("Login failed: ${resp.code}")
            val obj  = JSONObject(resp.body!!.string())
            val jwt  = obj.getString("jwt")
            val uid  = obj.getString("userId")
            val name = obj.getString("displayName")
            storedJwt    = jwt
            storedUserId = uid
            LoginResult(jwt, uid, name)
        }
    }

    fun getJwt(): String = storedJwt ?: throw IllegalStateException("Not authenticated")

    data class LoginResult(val jwt: String, val userId: String, val displayName: String)
}
