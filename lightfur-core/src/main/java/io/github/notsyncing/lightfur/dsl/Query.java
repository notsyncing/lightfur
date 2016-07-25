package io.github.notsyncing.lightfur.dsl;

import io.github.notsyncing.lightfur.entity.DataModel;
import io.github.notsyncing.lightfur.entity.TableDefineModel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Query
{
    private static Map<String, IQueryContext> queryContextMap = new ConcurrentHashMap<>();
    private static Map<String, IUpdateContext> updateContextMap = new ConcurrentHashMap<>();

    public static void addDataContextImplementation(final String tag, DataContext context)
    {
        if (context instanceof IQueryContext) {
            queryContextMap.put(tag, (IQueryContext) context);
        } else if (context instanceof IUpdateContext) {
            updateContextMap.put(tag, (IUpdateContext) context);
        }

        System.out.println("Registered QueryContext instance at " + context.toString() + " for tag " + tag);
    }

    public static void addDataContextImplementation(DataContext context)
    {
        addDataContextImplementation(context.getTag(), context);
    }

    public static void addDataContextImplementation(Class<? extends DataContext> contextClass)
    {
        try {
            DataContext context = contextClass.newInstance();
            addDataContextImplementation(context);
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

    public static <T extends TableDefineModel> IUpdateContext<T> update(Class<T> modelClass, final String tag)
    {
        if (!updateContextMap.containsKey(tag)) {
            throw new RuntimeException("No UpdateContext implementation found with tag " + tag +
                    ", please check if you have enabled lightfur annotation processor, " +
                    "and specified @DataRepository on your class containing Query!");
        }

        return updateContextMap.get(tag);
    }
}
