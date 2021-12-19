package hu.unideb.randomdog

import retrofit2.http.GET

interface Request {
    @GET("woof.json")
    suspend fun getRandomDog(): Dog
    @GET("v1/images/search")
    suspend fun getRandomCat(): Cat
}