package io.github.notsyncing.lightfur.entity;

import io.vertx.ext.sql.ResultSet;

import java.util.List;

/**
 * Created by grasp on 2016-10-10.
 */
public interface DataMapper
{
    <T> T map(Class<T> clazz, ResultSet results) throws IllegalAccessException, InstantiationException;

    <T> List<T> mapToList(Class<T> clazz, ResultSet results) throws InstantiationException, IllegalAccessException;
}
