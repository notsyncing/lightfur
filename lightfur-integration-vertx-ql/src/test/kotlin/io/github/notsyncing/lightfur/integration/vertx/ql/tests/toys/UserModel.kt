package io.github.notsyncing.lightfur.integration.vertx.ql.tests.toys

import io.github.notsyncing.lightfur.entity.EntityModel
import java.time.LocalDateTime

class UserModel : EntityModel(table = "users") {
    var id: Long by field("id", primaryKey = true, autoGenerated = true)

    var username: String by field("username")

    var mobile: String? by field("mobile")

    var email: String? by field("email")

    var lastLoginTime: LocalDateTime? by field("last_login_time")

    var status: Int by field("status")
}