import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ServerConfigDao {
    @Query("SELECT * FROM server_configs")
    suspend fun getAll(): List<ServerConfigEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(server: ServerConfigEntity)

    @Delete
    suspend fun delete(server: ServerConfigEntity)

    @Query("SELECT * FROM server_configs WHERE id = :id")
    suspend fun getById(id: Int): ServerConfigEntity?

    @Query("UPDATE server_configs SET name=:name, url=:url, username=:username, password=:password WHERE id=:id")
    suspend fun update(id: Int, name: String, url: String, username: String, password: String)
}