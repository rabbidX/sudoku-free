package me.garyanov.sudokufree2

import me.garyanov.sudokufree2.model.KeyResponse
import me.garyanov.sudokufree2.model.SudokuRequest
import me.garyanov.sudokufree2.model.SudokuResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface SudokuApi {
    @POST("api")
    suspend fun getNewGame(
        @Body body: SudokuRequest,
        @Header("x-api-key") apiKey: String
    ): SudokuResponse

    @GET("api/create-key")
    suspend fun createKey(): KeyResponse
}