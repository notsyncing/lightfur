package io.github.notsyncing.lightfur.entity.read

import io.github.notsyncing.lightfur.core.read.ModelReader
import io.github.notsyncing.lightfur.entity.EntityModel
import io.github.notsyncing.lightfur.entity.dsl.EntityRawQueryDSL

interface EntityModelReader<T: EntityModel> : ModelReader<T> {
    fun query(): EntityRawQueryDSL<T>
}