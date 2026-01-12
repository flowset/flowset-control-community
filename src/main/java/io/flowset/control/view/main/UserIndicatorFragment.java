/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.main;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.combobox.ComboBoxVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.jmix.core.security.UserRepository;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.core.usersubstitution.event.UiUserSubstitutionsChangedEvent;
import io.jmix.flowui.action.security.LogoutAction;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.main.JmixUserIndicator;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Target;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewComponent;
import io.flowset.control.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Objects;

/**
 * Shows details about the logged-in user in the side menu.
 */
@FragmentDescriptor("user-indicator-fragment.xml")
public class UserIndicatorFragment extends Fragment<HorizontalLayout> {
    private static final Logger log = LoggerFactory.getLogger(UserIndicatorFragment.class);

    @Autowired
    protected CurrentUserSubstitution currentUserSubstitution;
    @Autowired
    protected UserRepository userRepository;
    @ViewComponent
    protected Avatar userAvatar;
    @ViewComponent
    protected Span emailField;
    @ViewComponent
    protected JmixUserIndicator userIndicator;
    @ViewComponent
    protected LogoutAction logout;

    @Subscribe(target = Target.HOST_CONTROLLER)
    protected void onHostInit(final View.InitEvent event) {
        logout.setIcon(null);

        updateUserInfo();
        updateUserIndicatorControl();
    }

    protected void updateUserInfo() {
        UserDetails userDetails = currentUserSubstitution.getEffectiveUser();
        try {
            userDetails = userRepository.loadUserByUsername(userDetails.getUsername());
        } catch (UsernameNotFoundException e) {
            log.error("User repository doesn't contain user with username {}", userDetails.getUsername());
        }
        if (userDetails instanceof User user) {
            String abbreviation = getUserAbbreviation(user);
            userAvatar.setAbbreviation(abbreviation.toUpperCase());

            emailField.setText(user.getEmail());
        }
    }


    @EventListener
    public void onUserSubstitutionsChanged(UiUserSubstitutionsChangedEvent event) {
        UserDetails authenticatedUser = currentUserSubstitution.getAuthenticatedUser();
        if (Objects.equals(authenticatedUser.getUsername(), event.getSource())) {
            updateUserInfo();
            updateUserIndicatorControl();
        }
    }

    protected void updateUserIndicatorControl() {
        userIndicator.getContent().getChildren().findFirst()
                .ifPresent(component -> {
                    if (component instanceof JmixComboBox<?> comboBox) {
                        comboBox.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
                    }
                });
    }

    protected String getUserAbbreviation(User user) {
        if (StringUtils.isNoneBlank(user.getFirstName(), user.getLastName())) {
            return user.getFirstName().substring(0, 1) + user.getLastName().substring(0, 1);
        }
        return user.getUsername().substring(0, 1);
    }
}