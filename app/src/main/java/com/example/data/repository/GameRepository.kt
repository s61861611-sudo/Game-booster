package com.example.data.repository

import com.example.data.local.GameDao
import com.example.data.model.GameEntity
import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {
    val allGames: Flow<List<GameEntity>> = gameDao.getAllGames()

    suspend fun insertGame(game: GameEntity) {
        gameDao.insertGame(game)
    }

    suspend fun updateGame(game: GameEntity) {
        gameDao.updateGame(game)
    }

    suspend fun deleteGame(game: GameEntity) {
        gameDao.deleteGame(game)
    }

    suspend fun deleteByPackageName(packageName: String) {
        gameDao.deleteByPackageName(packageName)
    }
}
