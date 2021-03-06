package romilp

import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.locations.*
import io.ktor.sessions.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.gson.*
import io.ktor.features.*
import io.ktor.response.*
import romilp.auth.JwtService
import romilp.auth.MySession
import romilp.repository.DatabaseFactory
import romilp.repository.TodoRepository
import romilp.repository.UserRepository
import romilp.routes.todoRoute
import romilp.routes.userRoute

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    DatabaseFactory.init()

    val todoDb = TodoRepository()
    val userDb = UserRepository()
    val jwt = JwtService()
    val hash = { s: String -> s }

    install(Locations) {
    }

    install(Sessions) {
        cookie<MySession>("MY_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }

    install(Authentication) {
        jwt("jwt") {
            verifier(jwt.verifier)
            realm = "Todo Server"
            validate {
                val payload = it.payload
                val claim = payload.getClaim("userId")
                val claimInt = claim.asInt()
                val user = userDb.findUserById(claimInt)
                user
            }
        }
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    routing {
        get("/"){
            call.respondText("Hello world")
        }
        userRoute(userDb, todoDb, jwt, hash)
        todoRoute(userDb,todoDb)
    }
}


const val API_VERSION = "/v1"