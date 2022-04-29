package romilp.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import romilp.API_VERSION
import romilp.auth.JwtService
import romilp.auth.MySession
import romilp.repository.TodoRepository
import romilp.repository.UserRepository

const val USER = "$API_VERSION/user"
const val LOGIN = "$API_VERSION/login"
const val CREATE = "$API_VERSION/create"

fun Route.userRoute(
    db: UserRepository,
    todoDb: TodoRepository,
    jwt: JwtService,
    hashFunction: (String) -> String
) {
    post(CREATE){
        val parameter = call.receive<Parameters>()

        val password = parameter["password"] ?: return@post call.respondText(
            "missing field",
            status = HttpStatusCode.Unauthorized
        )

        val name = parameter["name"] ?: return@post call.respondText(
            "missing field",
            status = HttpStatusCode.Unauthorized
        )

        val email = parameter["email"] ?: return@post call.respondText(
            "missing field",
            status = HttpStatusCode.Unauthorized
        )

        val hash = hashFunction(password)

        try {
            val newUser = db.createUser(email,name,hash)
            newUser?.userId?.let {
                call.sessions.set(MySession(it))
                call.respondText(
                    jwt.generateToken(newUser),
                    status = HttpStatusCode.Created
                )
            }
        }catch (e:Throwable){
            application.log.error("Failed to register user", e)
            call.respond(HttpStatusCode.BadRequest, "Problems creating User")
        }
    }

    post(LOGIN) {
        val data = call.receive<Parameters>()

        val password = data["password"] ?: return@post call.respondText(
            "missing field",
            status = HttpStatusCode.Unauthorized
        )

        val email = data["email"] ?: return@post call.respondText(
            "missing field",
            status = HttpStatusCode.Unauthorized
        )
        val hash = hashFunction(password)

        try{

            val currentUser = db.findUserByEmail(email)
            currentUser?.userId?.let {
                if(currentUser.password == hash){
                    call.sessions.set(MySession(it))
                    call.respondText(jwt.generateToken(currentUser))
                }else{
                    call.respond(status = HttpStatusCode.BadRequest,
                        "problem retrieving user.. ")
                }
            }

        }catch (e:Throwable){
            application.log.error("Failed to register user", e)
            call.respond(HttpStatusCode.BadRequest, "Problems creating User")
        }
    }

    delete(USER){
        val user = call.sessions.get<MySession>()?.let {
            db.findUserById(it.userId)
        }

        if(user == null){
            call.respondText("problem getting user",status = HttpStatusCode.BadRequest)
        }

        try {
            user?.userId?.let { it1 -> todoDb.deleteAllTodo(it1) }
            val isDelete = user?.userId?.let { it1 -> db.deleteUser(it1) }
            if(isDelete == 1){
                call.respond(user)
            }else{
                call.respondText("something went wrong..",status = HttpStatusCode.BadRequest)
            }
        }catch (e:Throwable){
            application.log.error("Failed to register user", e)
            call.respond(HttpStatusCode.BadRequest, "Problems deleting User")
        }
    }

    put(USER){
        val parameters = call.receive<Parameters>()
        val user = call.sessions.get<MySession>()?.let {
            db.findUserById(it.userId)
        }

        val name = parameters["name"] ?: return@put call.respondText(
            "missing field",
            status = HttpStatusCode.Unauthorized
        )

        val email = parameters["email"] ?: return@put call.respondText(
            "missing field",
            status = HttpStatusCode.Unauthorized
        )

        val password = parameters["password"] ?: return@put call.respondText(
            "missing fields",
            status = HttpStatusCode.Unauthorized
        )
        val hash = hashFunction(password)

        try {

            val isUpdated = user?.userId?.let { it1 -> db.updateAllData(it1,name,email,hash) }

            if(isUpdated == 1){
                val updated = db.findUserById(user.userId)
                updated?.userId?.let {
                    call.respond(updated)
                }
            }else{
                call.respond("something went wrong..")
            }

        }catch (e:Throwable){
            application.log.error("Failed to register user", e)
            call.respond(HttpStatusCode.BadRequest, "Problems deleting User")
        }
    }

    patch(USER){
        val parameters = call.receive<Parameters>()
        val user = call.sessions.get<MySession>()?.let {
            db.findUserById(it.userId)
        }

        val name = parameters["name"] ?: "${user?.name}"

        val email = parameters["email"] ?: "${user?.email}"

        val password = parameters["password"] ?: "${user?.password}"

        val hash = hashFunction(password)

        try {
            val isUpdated = user?.userId?.let { it1 -> db.updateAllData(it1,name,email,hash) }

            if(isUpdated == 1){
                val updated = db.findUserById(user.userId)
                updated?.userId?.let {
                    call.respond(updated)
                }
            }else{
                call.respond("something went wrong..")
            }
        }catch (e:Throwable){
            application.log.error("Failed to register user", e)
            call.respond(HttpStatusCode.BadRequest, "Problems deleting User")
        }
    }

}