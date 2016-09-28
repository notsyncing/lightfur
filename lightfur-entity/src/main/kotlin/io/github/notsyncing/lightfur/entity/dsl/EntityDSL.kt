package io.github.notsyncing.lightfur.entity.dsl

import io.github.notsyncing.lightfur.entity.EntityModel
import io.github.notsyncing.lightfur.sql.builders.SelectQueryBuilder

object EntityDSL {
    fun select(finalModel: EntityModel? = null) = EntitySelectDSL(finalModel)

    fun insert() {

    }

    fun update() {

    }

    fun delete() {

    }

}