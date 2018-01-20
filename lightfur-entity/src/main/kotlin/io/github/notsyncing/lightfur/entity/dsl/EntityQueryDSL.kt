package io.github.notsyncing.lightfur.entity.dsl

import io.github.notsyncing.lightfur.entity.EntityModel

abstract class EntityQueryDSL<F: EntityModel>(finalModel: F?) : EntityBaseDSL<F>(finalModel, isQuery = true) {

}