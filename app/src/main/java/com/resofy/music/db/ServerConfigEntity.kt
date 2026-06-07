import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "server_configs")
data class ServerConfigEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val url: String,
    val username: String,
    val password: String,
    val type: String = "subsonic" // extensible
)