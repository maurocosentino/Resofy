package com.resofy.music.repository


import ServerConfigDao
import ServerConfigEntity


class ServerConfigRepository(private val dao: ServerConfigDao) {

    suspend fun getAll(): List<ServerConfigEntity> = dao.getAll()

    suspend fun insert(server: ServerConfigEntity): Unit = dao.insert(server)

    suspend fun update(id: Int, name: String, url: String, username: String, password: String) =
        dao.update(id, name, url, username, password)

    suspend fun delete(server: ServerConfigEntity) = dao.delete(server)

    suspend fun getById(id: Int): ServerConfigEntity? = dao.getById(id)
}