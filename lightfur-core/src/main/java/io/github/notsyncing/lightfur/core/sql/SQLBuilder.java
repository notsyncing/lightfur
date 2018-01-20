package io.github.notsyncing.lightfur.core.sql;

import io.github.notsyncing.lightfur.core.sql.base.SQLPart;
import io.github.notsyncing.lightfur.core.sql.builders.DeleteQueryBuilder;
import io.github.notsyncing.lightfur.core.sql.builders.InsertQueryBuilder;
import io.github.notsyncing.lightfur.core.sql.builders.SelectQueryBuilder;
import io.github.notsyncing.lightfur.core.sql.builders.UpdateQueryBuilder;
import io.github.notsyncing.lightfur.core.sql.models.TableModel;

public class SQLBuilder
{
    public static SelectQueryBuilder select(SQLPart... columns)
    {
        return new SelectQueryBuilder().select(columns);
    }

    public static InsertQueryBuilder insert()
    {
        return new InsertQueryBuilder();
    }

    public static DeleteQueryBuilder delete()
    {
        return new DeleteQueryBuilder();
    }

    public static UpdateQueryBuilder update(TableModel t)
    {
        return new UpdateQueryBuilder().on(t);
    }
}
