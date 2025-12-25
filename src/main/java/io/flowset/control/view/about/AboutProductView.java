package io.flowset.control.view.about;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
import io.jmix.core.Resources;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import io.flowset.control.action.CopyComponentValueToClipboardAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;

import java.util.List;
import java.util.Locale;

@Slf4j
@Route(value = "about", layout = DefaultMainViewParent.class)
@ViewController(id = "AboutProductView")
@ViewDescriptor(path = "about-product-view.xml")
public class AboutProductView extends StandardView {
    protected static final String APPLICATION_NAME = "Flowset Control";

    protected static final String RESOURCES_PATH = "/META-INF/resources/etc/";
    protected static final String DEFAULT_METADATA_FILE_PATH = RESOURCES_PATH + "about-product-metadata.json";
    protected static final String METADATA_FILE_PATH_TEMPLATE = RESOURCES_PATH + "about-product-metadata-%s.json";

    @Autowired
    protected Resources resources;
    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected Messages messages;
    @Autowired
    protected BuildProperties buildProperties;
    @Autowired
    protected Fragments fragments;

    @ViewComponent
    protected Span productText;
    @ViewComponent
    protected Span versionText;
    @ViewComponent
    protected Span buildText;

    @ViewComponent
    protected VerticalLayout externalLinksBox;
    @ViewComponent
    protected VerticalLayout productsBox;
    @Autowired
    private UiComponents uiComponents;

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        productText.setText(APPLICATION_NAME);
        versionText.setText(buildProperties.getVersion());
        buildText.setText(buildProperties.get("buildType"));

        AboutProductMetadata contentMetadata = loadContentMetadata();
        if (contentMetadata != null) {
            initExternalLinks(contentMetadata.getExternalLinks());
            initProducts(contentMetadata.getProducts());
        }
    }

    protected void initProducts(List<AboutProductMetadata.Product> products) {
        productsBox.removeAll();

        H3 productsHeader = uiComponents.create(H3.class);
        productsHeader.setText(messages.getMessage(getClass(), "ourProductsHeader.text"));
        productsBox.add(productsHeader);

        if (CollectionUtils.isNotEmpty(products)) {
            for (int i = 0; i < products.size(); i++) {
                AboutProductMetadata.Product product = products.get(i);
                ProductInfoFragment productInfoFragment = fragments.create(this, ProductInfoFragment.class);
                productInfoFragment.setProduct(product);

                productsBox.add(productInfoFragment);
                if (i < products.size() - 1) {
                    productsBox.add(new Hr());
                }
            }
        }
    }

    protected void initExternalLinks(List<AboutProductMetadata.ExternalLink> externalLinksList) {
        externalLinksBox.removeAll();

        H3 externalLinkHeader = uiComponents.create(H3.class);
        externalLinkHeader.setText(messages.getMessage(getClass(), "externalLinksHeader.text"));
        externalLinksBox.add(externalLinkHeader);

        if (CollectionUtils.isNotEmpty(externalLinksList)) {
            externalLinksList.forEach(externalLink -> {
                ExternalLinkFragment externalLinkFragment = fragments.create(this, ExternalLinkFragment.class);
                externalLinkFragment.setLink(externalLink.getLabel(), externalLink.getUrl());
                externalLinksBox.add(externalLinkFragment);
            });
        }
    }

    @Nullable
    protected AboutProductMetadata loadContentMetadata() {
        Locale locale = currentAuthentication.getLocale();
        String contentMetadata = resources.getResourceAsString(METADATA_FILE_PATH_TEMPLATE.formatted(locale.getLanguage()));

        if (contentMetadata == null) {
            contentMetadata = resources.getResourceAsString(DEFAULT_METADATA_FILE_PATH);
        }
        if (contentMetadata != null) {
            try {
                return objectMapper.readValue(contentMetadata, AboutProductMetadata.class);
            } catch (JsonProcessingException e) {
                log.error("Unable to read content metadata with locale '{}'", locale, e);
            }
        }
        return null;
    }

    @Subscribe(id = "copyProductBtn", subject = "clickListener")
    protected void onCopyProductBtnClick(final ClickEvent<JmixButton> event) {
        String productValue = "%s %s (%s)".formatted(productText.getText(), versionText.getText(), buildText.getText());

        UiComponentUtils.copyToClipboard(productValue)
                .then(successResult -> notifications.create(
                                        messages.getMessage(CopyComponentValueToClipboardAction.class, "copyComponentValueAction.copied"))
                                .withPosition(Notification.Position.TOP_END)
                                .withThemeVariant(NotificationVariant.LUMO_SUCCESS)
                                .show(),
                        errorResult -> notifications.create(
                                        messages.getMessage(CopyComponentValueToClipboardAction.class, "copyComponentValueAction.copyFailed"))
                                .withPosition(Notification.Position.TOP_END)
                                .withThemeVariant(NotificationVariant.LUMO_ERROR)
                                .show());
    }
}