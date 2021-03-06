/*
 * Copyright (c) 2016.
 * Igor Avdeev
 */

package com.tsystems.javaschool.logiweb.service.helper;

import javax.persistence.EntityManager;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Wraps every call in transaction
 *
 * https://jowisoftware.de/wp/2013/09/using-javas-proxy-class-to-transparently-manage-transactions/
 *
 * java.lang.reflect.UndeclaredThrowableException when BussinessLogicException is thrown
 */
public class TransactionProxy {
    private final EntityManager em;

    public TransactionProxy(final EntityManager em) {
        this.em = em;
    }

    public Object createProxy(final Object object)  {
        final Class<?> clazz = object.getClass();
        final Class<?>[] interfaces = clazz.getInterfaces();
        final ClassLoader classLoader = clazz.getClassLoader();

        final Object proxy = Proxy.newProxyInstance(classLoader, interfaces,
                createInvocationHandler(object));
        return proxy;
    }

    private InvocationHandler createInvocationHandler(final Object target) {
        return new InvocationHandler() {
            @Override
            public Object invoke(final Object proxy, final Method method, final Object[] arguments)
                    throws Throwable {

                // Ignore if transaction is alread in progress
                Boolean needTransaction = !em.getTransaction().isActive();

                if (needTransaction) {
                    em.getTransaction().begin();
                }

                final Object result;
                try {
                    result = method.invoke(target, arguments);
                } catch (final InvocationTargetException e) {
                    if (needTransaction) {
                        em.getTransaction().rollback();
                    }
                    throw e.getCause();
                }
                if (needTransaction) {
                    em.getTransaction().commit();
                }
                return result;
            }
        };
    }
}
