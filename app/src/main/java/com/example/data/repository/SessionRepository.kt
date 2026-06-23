package com.example.data.repository

import com.example.data.local.SessionDao
import com.example.data.model.SessionEntity
import kotlinx.coroutines.flow.Flow

class SessionRepository(private val sessionDao: SessionDao) {
    val allSessions: Flow<List<SessionEntity>> = sessionDao.getAllSessions()

    fun getSessionsForGame(packageName: String): Flow<List<SessionEntity>> {
        return sessionDao.getSessionsForGame(packageName)
    }

    suspend fun insertSession(session: SessionEntity) {
        sessionDao.insertSession(session)
    }

    suspend fun clearAllSessions() {
        sessionDao.clearAllSessions()
    }
}
