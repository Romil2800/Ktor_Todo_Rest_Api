package romilp.data.dao

import romilp.data.User
import java.net.PasswordAuthentication

interface UserDao {

    suspend fun createUser(
        name: String,
        email: String,
        password: String
    ): User?

    suspend fun findUserById(
        userId: Int
    ): User?

    suspend fun findUserByEmail(
        email: String
    ): User?

    suspend fun deleteUser(
        userId: Int
    ): Int

    suspend fun updateAllData(
        id:Int,
        name:String,
        email: String,
        password: String
    ):Int?

    suspend fun updateAnyData(
        id: Int,
        name: String,
        email: String,
        password: String
    ):Int?

}