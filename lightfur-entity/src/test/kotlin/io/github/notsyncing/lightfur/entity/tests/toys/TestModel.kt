package io.github.notsyncing.lightfur.entity.tests.toys

import io.github.notsyncing.lightfur.entity.*
import io.github.notsyncing.lightfur.entity.dsl.EntityDSL
import io.github.notsyncing.lightfur.entity.functions.coalesce
import io.github.notsyncing.lightfur.entity.functions.count
import io.github.notsyncing.lightfur.entity.functions.now
import io.github.notsyncing.lightfur.entity.functions.sum

class TestModel : EntityModel(table = "test_table") {
    var id: Int by field(primaryKey = true, autoGenerated = true)

    var name: String by field()

    var flag: Int by field()
}

class TestModelMultiPK : EntityModel(table = "test_table") {
    var id: Int by field(primaryKey = true, autoGenerated = true)

    var name: String by field(primaryKey = true, autoGenerated = true)

    var flag: Int by field()
}

class TestModel2 : EntityModel(table = "test_table2") {
    var id: Int by field(primaryKey = true)

    var details: String by field()
}

val r = TestModel()

val r2 = TestModel2()

fun test() {
    r.F(r::flag) eq r.F(r::name)
    r.F(r::flag) eq 3
    (r.F(r::flag) eq 4) and ((r.F(r::name) eq "test") or (r.F(r::flag) gt 3))
    r.F(r::flag) + 1 gt 3
    r.F(r::flag) + r.F(r::id) gt r.F(r::flag)
    r.F(r::flag) gt r.F(r::id)
    r.F(r::flag) eq (case().on(r.F(r::id) gt 3).then(5).on(r.F(r::id) gt 5).then(8).otherwise(7))
    coalesce(r.F(r::flag), 0) + 2
    coalesce(r.F(r::flag), now())

    EntityDSL.select(r)
            .from()
            .where { r.F(r::flag) gt 3 }
            .orderBy(r.F(r::flag) desc true, r.F(r::name) desc false)
            .having { sum(r.F(r::id)) gt 2 or count(r.F(r::id)) lt 4 }
            .skip(3)
            .take(10)

    EntityDSL.select(r)
            .map(sum(r.F(r2::id)), r.F(r::id))
            .from(EntityDSL.select(r2).from())
            .where { r2.F(r2::id) gt 4 }
}