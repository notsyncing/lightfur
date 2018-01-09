package io.github.notsyncing.lightfur.entity.tests.toys

import io.github.notsyncing.lightfur.entity.EntityModel

class A : EntityModel(table = "a") {
    var id: Long by field(primaryKey = true)

    var b: B? by reference(B::aId)
}

class B : EntityModel(table = "b") {
    var id: Long by field(primaryKey = true)

    var aId: Long by field("a_id")
}

class A2 : EntityModel(table = "c") {
    var id: Long by field(primaryKey = true)

    val b: MutableList<B> by referenceMany(B::aId)
}