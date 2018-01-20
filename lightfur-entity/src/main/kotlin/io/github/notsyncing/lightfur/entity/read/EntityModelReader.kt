package io.github.notsyncing.lightfur.entity.read

import io.github.notsyncing.lightfur.entity.EntityModel
import io.github.notsyncing.lightfur.entity.dsl.EntityQueryDSL
import io.github.notsyncing.lightfur.read.ModelReader

interface EntityModelReader<T: EntityModel> : ModelReader<T> {
    fun query(): EntityQueryDSL<T>
}