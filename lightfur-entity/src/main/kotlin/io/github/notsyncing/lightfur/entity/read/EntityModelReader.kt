package io.github.notsyncing.lightfur.entity.read

import io.github.notsyncing.lightfur.entity.EntityModel
import io.github.notsyncing.lightfur.entity.dsl.EntityRawQueryDSL
import io.github.notsyncing.lightfur.read.ModelReader

interface EntityModelReader<T: EntityModel> : ModelReader<T> {
    fun query(): EntityRawQueryDSL<T>
}