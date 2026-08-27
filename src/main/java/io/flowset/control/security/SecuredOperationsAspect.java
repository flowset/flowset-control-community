/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.security;

import io.jmix.core.AccessManager;
import io.jmix.core.Metadata;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.accesscontext.SpecificOperationAccessContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.security.EntityOp;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.*;

@Slf4j
@Aspect
@Component("control_SecuredOperationsAspect")
@AllArgsConstructor
public class SecuredOperationsAspect {

    protected final AccessManager accessManager;
    protected final Metadata metadata;
    protected final CurrentAuthentication currentAuthentication;

    /**
     * This method acts as an around aspect for secured operations like loading or custom operation related
     * to the data storing in the external system like BPM engine.
     *
     * @param joinPoint the join point representing the intercepted method
     * @return the result of the method execution if access is permitted; otherwise, returns a denied result
     * @throws Throwable if execution of the underlying method or aspect logic fails
     * @see SecuredEntityLoad
     * @see SecuredEntityOperation
     */
    @Around("execution(* io.flowset.control.service..*.*(..))")
    public Object aroundSecuredOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        Method method = sig.getMethod();
        SecuredEntityLoad securedEntityLoad = AnnotationUtils.findAnnotation(method, SecuredEntityLoad.class);

        if (securedEntityLoad != null) {
            if (isReadPermitted(securedEntityLoad.entityClass())) {
                return joinPoint.proceed();
            }

            log.debug("Skipping secured resource load '{}' for resource '{}' because read access is denied for user '{}'",
                    joinPoint.getSignature().toShortString(),
                    securedEntityLoad.entityClass().getSimpleName(),
                    getCurrentUsername());

            return createDeniedResult(joinPoint, securedEntityLoad);
        }

        SecuredEntityOperation securedEntityOperation = AnnotationUtils.findAnnotation(method, SecuredEntityOperation.class);
        if (securedEntityOperation == null || isOperationPermitted(securedEntityOperation)) {
            return joinPoint.proceed();
        }

        log.debug("Skipping secured operation '{}' for resource '{}' because permission '{}' is denied for user '{}'",
                joinPoint.getSignature().toShortString(),
                securedEntityOperation.entityClass().getSimpleName(),
                getPermissionDescription(securedEntityOperation),
                getCurrentUsername());

        return createDeniedResult(joinPoint, securedEntityOperation);
    }

    /**
     * Checks if the current user has read access to the given entity class.
     *
     * @param entityClass the entity class to check
     * @return true if read access is permitted, false otherwise
     */
    protected boolean isReadPermitted(Class<?> entityClass) {
        MetaClass metaClass = metadata.getClass(entityClass);
        CrudEntityContext context = new CrudEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(context);

        return context.isReadPermitted();
    }

    /**
     * Checks if the current user has permission to perform the specified operation.
     *
     * @param securedEntityOperation the operation to check
     * @return true if an operation is permitted, false otherwise
     */
    protected boolean isOperationPermitted(SecuredEntityOperation securedEntityOperation) {
        if (StringUtils.hasText(securedEntityOperation.specificPermission())) {
            return isSpecificOperationPermitted(securedEntityOperation.specificPermission());
        }
        return isEntityOperationPermitted(securedEntityOperation.entityClass(), securedEntityOperation.entityOp());
    }

    protected boolean isEntityOperationPermitted(Class<?> entityClass, EntityOp entityOp) {
        MetaClass metaClass = metadata.getClass(entityClass);
        CrudEntityContext context = new CrudEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(context);

        return switch (entityOp) {
            case CREATE -> context.isCreatePermitted();
            case READ -> context.isReadPermitted();
            case UPDATE -> context.isUpdatePermitted();
            case DELETE -> context.isDeletePermitted();
        };
    }

    protected boolean isSpecificOperationPermitted(String specificPermission) {
        SpecificOperationAccessContext context = new SpecificOperationAccessContext(specificPermission);
        accessManager.applyRegisteredConstraints(context);

        return context.isPermitted();
    }

    protected Object createDeniedResult(ProceedingJoinPoint joinPoint, SecuredEntityLoad securedEntityLoad) {
        return createDeniedResult(
                joinPoint,
                securedEntityLoad.deniedStrategy(),
                new AccessDeniedException("entity", securedEntityLoad.entityClass().getName(), EntityOp.READ.getId())
        );
    }

    protected Object createDeniedResult(ProceedingJoinPoint joinPoint, SecuredEntityOperation securedEntityOperation) {
        AccessDeniedException accessDeniedException;
        if (StringUtils.hasText(securedEntityOperation.specificPermission())) {
            accessDeniedException = new AccessDeniedException("specific", securedEntityOperation.specificPermission(), null);
        } else {
            accessDeniedException = new AccessDeniedException(
                    "entity",
                    securedEntityOperation.entityClass().getName(),
                    securedEntityOperation.entityOp().getId()
            );
        }

        return createDeniedResult(joinPoint, securedEntityOperation.deniedStrategy(), accessDeniedException);
    }

    protected Object createDeniedResult(ProceedingJoinPoint joinPoint, DeniedResultStrategy deniedStrategy,
                                        AccessDeniedException accessDeniedException) {
        Class<?> returnType = resolveReturnType(joinPoint);
        if (deniedStrategy == DeniedResultStrategy.THROW_ACCESS_DENIED) {
            throw accessDeniedException;
        }
        return getTypeDefault(returnType);
    }

    protected String getCurrentUsername() {
        return currentAuthentication.getUser().getUsername();
    }

    protected String getPermissionDescription(SecuredEntityOperation securedEntityOperation) {
        if (StringUtils.hasText(securedEntityOperation.specificPermission())) {
            return securedEntityOperation.specificPermission();
        }
        return securedEntityOperation.entityOp().getId();
    }

    protected Class<?> resolveReturnType(ProceedingJoinPoint joinPoint) {
        if (joinPoint.getSignature() instanceof MethodSignature methodSignature) {
            Method method = methodSignature.getMethod();
            return method.getReturnType();
        }
        return Object.class;
    }

    protected Object getTypeDefault(Class<?> returnType) {
        if (returnType == Void.TYPE) {
            return null;
        }

        if (returnType == Optional.class) {
            return Optional.empty();
        }

        if (returnType == Boolean.TYPE || returnType == Boolean.class) {
            return false;
        }

        if (returnType == Integer.TYPE || returnType == Integer.class) {
            return 0;
        }
        if (returnType == Long.TYPE || returnType == Long.class) {
            return 0L;
        }

        if (returnType == Double.TYPE || returnType == Double.class) {
            return 0D;
        }

        if (Set.class.isAssignableFrom(returnType)) {
            return Collections.emptySet();
        }

        if (Collection.class.isAssignableFrom(returnType)) {
            return Collections.emptyList();
        }

        if (Map.class.isAssignableFrom(returnType)) {
            return Collections.emptyMap();
        }
        return null;
    }
}
