package io.github.notsyncing.lightfur.dsl;

import io.github.notsyncing.lightfur.entity.DataModel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Query
{
    private static Map<String, IQueryContext> queryContextMap = new ConcurrentHashMap<>();

    public static void addQueryContextImplementation(final String tag, IQueryContext context)
    {
        queryContextMap.put(tag, context);

        System.out.println("Registered QueryContext instance at " + context.toString() + " for tag " + tag);
    }

    public static void addQueryContextImplementation(IQueryContext context)
    {
        addQueryContextImplementation(context.getTag(), context);
    }

    public static void addQueryContextImplementation(Class<? extends IQueryContext> contextClass)
    {
        try {
            IQueryContext context = contextClass.newInstance();
            addQueryContextImplementation(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T extends DataModel> IQueryContext<T> get(Class<T> modelClass, final String tag)
    {
        if (!queryContextMap.containsKey(tag)) {
            throw new RuntimeException("No QueryContext implementation found with tag " + tag +
                    ", please check if you have enabled lightfur annotation processor, " +
                    "and specified @DataRepository on your class containing Query!");
        }

        return queryContextMap.get(tag);
    }
}
