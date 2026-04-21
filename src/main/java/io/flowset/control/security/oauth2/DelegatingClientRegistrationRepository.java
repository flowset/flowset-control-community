/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.security.oauth2;

import com.google.common.collect.Iterators;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Delegates to multiple {@link ClientRegistrationRepository} instances.
 */
@Primary
@Component("control_DelegatingClientRegistrationRepository")
public class DelegatingClientRegistrationRepository implements ClientRegistrationRepository, Iterable<ClientRegistration> {
    protected final List<ClientRegistrationRepository> delegates;

    public DelegatingClientRegistrationRepository(List<ClientRegistrationRepository> delegates) {
        this.delegates = List.copyOf(delegates);
    }

    @Nullable
    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        for (ClientRegistrationRepository repo : delegates) {
            ClientRegistration r = repo.findByRegistrationId(registrationId);
            if (r != null) {
                return r;
            }
        }
        return null;
    }

    @Override
    public @NonNull Iterator<ClientRegistration> iterator() {
        return Iterators.concat(
                delegates.stream()
                        .filter(repo -> repo instanceof Iterable<?>)
                        .map(repo -> ((Iterable<ClientRegistration>) repo).iterator()) // Iterable -> Iterator
                        .iterator()
        );
    }

    @Override
    public void forEach(Consumer<? super ClientRegistration> action) {
        for (ClientRegistrationRepository repo : delegates) {
            if (repo instanceof Iterable<?> iterable) {
                Iterable<ClientRegistration> cr = (Iterable<ClientRegistration>) iterable;
                cr.forEach(action);
            }
        }
    }

    @Override
    public Spliterator<ClientRegistration> spliterator() {

        return delegates.stream()
                .filter(repo -> repo instanceof Iterable<?>)
                .flatMap(repo -> java.util.stream.StreamSupport.stream(
                        ((Iterable<ClientRegistration>) repo).spliterator(),
                        false
                ))
                .spliterator();
    }
}
