package io.github.notsyncing.lightfur.entity.tests

import io.github.notsyncing.lightfur.entity.*
import io.github.notsyncing.lightfur.entity.dsl.EntityDSL

class TestModel : EntityModel(table = "test_table") {
    var id: Int by field(this::id, primaryKey = true)

    var name: String by field(this::name)

    var flag: Int by field(this::flag)
}

val r = TestModel()

fun test() {
    r.F(r::flag) eq r.F(r::name)
    r.F(r::flag) eq 3
    (r.F(r::flag) eq 4) and ((r.F(r::name) eq "test") or (r.F(r::flag) gt 3))
    r.F(r::flag) + 1 gt 3
    r.F(r::flag) + r.F(r::id) gt r.F(r::flag)
    r.F(r::flag) gt r.F(r::id)

    EntityDSL.select(r)
            .from()
            .where { r.F(r::flag) gt 3 }
            .orderBy(r.F(r::flag) desc true, r.F(r::name) desc false)
            .skip(3)
            .take(10)
}