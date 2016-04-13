package io.github.notsyncing.lightfur.sql.base;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ReturningQueryBuilder implements SQLPart
{
    protected boolean needReturn = false;
    protected Map<SQLPart, String> returnExpressions = new LinkedHashMap<>();

    protected void appendReturningClause(StringBuilder buf)
    {
        if (needReturn) {
            buf.append("\nRETURNING ");

            if (returnExpressions.isEmpty()) {
                buf.append("*");
            } else {
                buf.append(
                        returnExpressions.entrySet().stream()
                                .map(e -> {
                                    StringBuilder b = new StringBuilder();

                                    b.append(e.getKey());

                                    if (e.getValue() != null) {
                                        b.append(" AS ").append(e.getValue());
                                    }

                                    return b.toString();
                                })
                                .collect(Collectors.joining(", "))
                );
            }
        }
    }
}
