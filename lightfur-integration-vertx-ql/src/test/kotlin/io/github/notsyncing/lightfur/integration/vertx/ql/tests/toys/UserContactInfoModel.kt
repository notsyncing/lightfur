package io.github.notsyncing.lightfur.integration.vertx.ql.tests.toys

import io.github.notsyncing.lightfur.entity.EntityModel

class UserContactInfoModel : EntityModel(table = "user_contact_infos") {
    var id: Long by field("id", primaryKey = true, autoGenerated = true)

    var userId: Long by field("user_id")

    var mobile: String? by field("mobile")

    var address: String? by field("mobile")

    var default: Boolean by field("default")
}