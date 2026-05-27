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
}