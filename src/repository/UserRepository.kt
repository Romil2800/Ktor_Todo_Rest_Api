package romilp.repository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import romilp.data.User
import romilp.data.dao.UserDao
import romilp.data.table.UserTable
import javax.xml.crypto.Data

class UserRepository : UserDao {

    override suspend fun createUser(name: String, email: String, password: String): User? {
        var statement: InsertStatement<Number>? = null
        DatabaseFactory.dbQuery {
            statement = UserTable.insert { user ->
                user[UserTable.name] = name
                user[UserTable.email] = email
                user[UserTable.password] = password
            }
        }
        return rowToUser(statement?.resultedValues?.get(0))
    }

    override suspend fun findUserById(userId: Int): User? = DatabaseFactory.dbQuery {
        UserTable.select { UserTable.userId.eq(userId) }
            .map {
                rowToUser(it)
            }.singleOrNull()
    }

    override suspend fun findUserByEmail(email: String): User? = DatabaseFactory.dbQuery {
        UserTable.select { UserTable.email.eq(email) }
            .map {
                rowToUser(it)
            }.singleOrNull()
    }

    override suspend fun deleteUser(userId: Int): Int = DatabaseFactory.dbQuery {
        UserTable.deleteWhere { UserTable.userId.eq(userId) }
    }


    override suspend fun updateUser(userId: Int, name: String, email: String, password: String): Int =
        DatabaseFactory.dbQuery {
            UserTable.update({ UserTable.userId.eq(userId) }) { user ->
                user[UserTable.name] = name
                user[UserTable.email] = email
                user[UserTable.password] = password
            }
        }

    private fun rowToUser(row: ResultRow?): User? {
        if (row == null)
            return null
        return User(
            name = row[UserTable.name],
            userId = row[UserTable.userId],
            email = row[UserTable.email],
            password = row[UserTable.password]
        )
    }
}