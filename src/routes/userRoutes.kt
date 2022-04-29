package romilp.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import romilp.auth.JwtService
import romilp.auth.MySession
import romilp.repository.TodoRepository
import romilp.repository.UserRepository

fun Route.userRoute(
    userDb: UserRepository,
    todoDb: TodoRepository,
    jwtService: JwtService,
    hash: (String) -> String
) {
    post("/v1/create") {
        val parameter = call.receive<Parameters>()
        val name =
            parameter["name"] ?: return@post call.respondText("Missing Data", status = HttpStatusCode.Unauthorized)
        val email =
            parameter["email"] ?: return@post call.respondText("Missing Data", status = HttpStatusCode.Unauthorized)
        val password =
            parameter["password"] ?: return@post call.respondText("Missing Data", status = HttpStatusCode.Unauthorized)
        val hashPassword = hash(password)

        val currentUser = userDb.createUser(name, email, hashPassword)
        try {
            currentUser?.userId?.let {
                call.sessions.set(MySession(it))
                call.respondText(
                    jwtService.generateToken(currentUser), status = HttpStatusCode.Created
                )
            }
        } catch (e: Throwable) {
            call.respondText("Problem in creating user..")
        }
    }

    post("/v1/login") {
        val parameter = call.receive<Parameters>()

        val email =
            parameter["email"] ?: return@post call.respondText("Missing Data", status = HttpStatusCode.Unauthorized)
        val password =
            parameter["password"] ?: return@post call.respondText("Missing Data", status = HttpStatusCode.Unauthorized)
        val hashPassword = hash(password)

        try {
            val currentUser = userDb.findUserByEmail(email)
            currentUser?.userId?.let {
                if (currentUser.password == hashPassword) {
                    call.sessions.set(MySession(it))
                    call.respondText(
                        jwtService.generateToken(currentUser)
                    )
                }
            }
        } catch (e: Throwable) {
            call.respondText("Problem in creating user..")
        }
    }

    delete("/v1/user") {
        val user = call.sessions.get<MySession>()?.let {
            userDb.findUserById(it.userId)
        }
        if (user == null) {
            call.respondText("Problem to get user", status = HttpStatusCode.BadRequest)
        }
        try {
            user?.userId?.let { it1 -> todoDb.deleteAllTodo(it1) }
            val currentUser = user?.userId?.let { it1 -> userDb.deleteUser(it1) }
            if (currentUser == 1) {
                call.respondText("user deleted..")
            } else {
                call.respond("Getting problem..")
            }
        } catch (e: Throwable) {
            call.respondText("Problem in creating user..")
        }
    }

    put("/v1/user") {
        val parameter = call.receive<Parameters>()

        val name =
            parameter["name"] ?: return@put call.respondText("Missing Data", status = HttpStatusCode.Unauthorized)
        val email =
            parameter["email"] ?: return@put call.respondText("Missing Data", status = HttpStatusCode.Unauthorized)
        val password =
            parameter["password"] ?: return@put call.respondText("Missing Data", status = HttpStatusCode.Unauthorized)
        val hash = hash(password)

        val user = call.sessions.get<MySession>()?.let {
            userDb.findUserById(it.userId)
        }
        if (user == null) {
            call.respondText("Problem to get user", status = HttpStatusCode.BadRequest)
        }

        try {
            val currentUser = user?.userId?.let { it1 -> userDb.updateUser(it1, name, email, password) }
            if (currentUser == 1) {
                call.respondText("Updated successfully")
            } else {
                call.respond("Getting problem..")
            }
        } catch (e: Throwable) {
            call.respondText("Problem in creating user..")
        }
    }
}