/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.about;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.shared.Tooltip;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.view.ViewComponent;

@FragmentDescriptor("social-link-fragment.xml")
public class IconLinkFragment extends Fragment<Anchor> {
    @ViewComponent
    protected SvgIcon iconField;

    public void setLink(AboutProductMetadata.ExternalLink link) {
        Anchor anchor = getContent();
        anchor.setHref(link.getUrl());
        anchor.setAriaLabel(link.getLabel());

        iconField.setSrc(link.getIcon());

        Tooltip.forComponent(anchor).setText(link.getLabel());
    }
}
