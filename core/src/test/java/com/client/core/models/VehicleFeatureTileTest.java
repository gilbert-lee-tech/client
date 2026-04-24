package com.client.core.models;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import com.client.core.testcontext.AppAemContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(AemContextExtension.class)
public class VehicleFeatureTileTest {
 
    private final AemContext context = AppAemContext.newAemContext();

    private VehicleFeatureTile model;

    @BeforeEach
    void setup() {
        Resource resource = context.create().resource("/content/test/title",
                "title", "All-Wheel Drive",
                "description", "All-Wheel Drive",
                "iconPath", "/content/dam/brand-a/icons/awd.svg",
                "ctaLabel", "Learn More",
                "ctaUrl", "/content/brand-a/en/technology/awd.html",
                "ctaNewWindow", true);

        model = resource.adaptTo(VehicleFeatureTile.class);
    }

    @Test
    void testModelNotNull() {
        assertNotNull(model);
    }

    @Test
    void testGetTitle() {
        assertEquals("All-Wheel Drive", model.getTitle());
    }

    @Test
    void testGetDescription() {
        assertEquals("All-Wheel Drive", model.getDescription());
    }

    @Test
    void testGetIconPath() {
        assertEquals("/content/dam/brand-a/icons/awd.svg", model.getIconPath());
    }

    @Test
    void testGetCtaLabel() {
        assertEquals("Learn More", model.getCtaLabel());
    }

    @Test
    void testGetCtaUrl() {
        assertEquals("/content/brand-a/en/technology/awd.html", model.getCtaUrl());
    }

    @Test
    void testIsCtaNewWindow() {
        assertTrue(model.isCtaNewWindow());
    }

    @Test
    void testDefaultsWhenPropertiesMissing() {
        Resource emptyResource = context.create().resource("/content/test/title-empty");
        VehicleFeatureTile emptyModel = emptyResource.adaptTo(VehicleFeatureTile.class);

        assertNotNull(emptyModel);
        assertEquals("", emptyModel.getTitle());
        assertEquals("", emptyModel.getDescription());
        assertEquals("", emptyModel.getIconPath());
        assertEquals("", emptyModel.getCtaLabel());
        assertEquals("", emptyModel.getCtaUrl());
        assertFalse(emptyModel.isCtaNewWindow());
    }

    @Test
    void testFindProjectFromTemplatePath() {
        assertEquals("brand-a", model.findProjectFromTemplatePath("/conf/brand-a/settings/wcm/templates/page-content"));
        assertEquals("brand-a", model.findProjectFromTemplatePath("/apps/brand-a/settings/wcm/templates/page-content"));
        assertEquals("", model.findProjectFromTemplatePath("/apps/brand-a/components/content-page"));
        assertEquals("", model.findProjectFromTemplatePath("/apps/brand-a/templates/"));
        assertEquals("", model.findProjectFromTemplatePath("/apps/brand-a/templates/feature-tile/subtemplate"));
    }
}
        
