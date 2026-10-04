package org.larder.grandmaavatar;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/** Applies the {@code @Transactional} annotations of a bean the way Spring does, without a context. */
public final class Transactions {

    private Transactions() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T transactional(T target, PlatformTransactionManager transactionManager) {
        ProxyFactory proxy = new ProxyFactory(target);
        proxy.setProxyTargetClass(true);
        proxy.addAdvice(new TransactionInterceptor(transactionManager, new AnnotationTransactionAttributeSource()));
        return (T) proxy.getProxy();
    }
}
