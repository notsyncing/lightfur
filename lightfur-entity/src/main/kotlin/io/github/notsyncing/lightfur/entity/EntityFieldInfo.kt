package io.github.notsyncing.lightfur.entity

import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder
import io.github.notsyncing.lightfur.sql.models.ColumnModel

data class EntityFieldInfo(val entity: EntityModel, val name: String, val dbColumn: String, val dbType: String,
                           val dbLength: Int, val dbNullable: Boolean, val dbDefaultValue: String,
                           val dbPrimaryKey: Boolean, val dbAutoGenerated: Boolean)