package com.client.core.models;

import javax.annotation.PostConstruct;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.day.cq.wcm.api.Template;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Model(adaptables = Resource.class)
public class VehicleFeatureTile {

    @SlingObject
    private Resource currentResource;

    @SlingObject
    private ResourceResolver resourceResolver;

    @ValueMapValue(injectionStrategy=InjectionStrategy.OPTIONAL)
    @Default(values = "")
    private String title;

    @ValueMapValue(injectionStrategy=InjectionStrategy.OPTIONAL)
    @Default(values = "")
    private String description;

    @ValueMapValue(injectionStrategy=InjectionStrategy.OPTIONAL)
    @Default(values = "")
    private String iconPath;

    @ValueMapValue(injectionStrategy=InjectionStrategy.OPTIONAL)
    @Default(values = "")
    private String ctaLabel;

    @ValueMapValue(injectionStrategy=InjectionStrategy.OPTIONAL)
    @Default(values = "")
    private String ctaUrl;
    
    @ValueMapValue(injectionStrategy=InjectionStrategy.OPTIONAL)
    @Default(booleanValues = false)
    private boolean ctaNewWindow;

    private String brand;

    @PostConstruct
    protected void init() {
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        String templatePath = Optional.ofNullable(pageManager)
                .map(pm -> pm.getContainingPage(currentResource))
                .map(Page::getTemplate)
                .map(Template::getPath).orElse("");

        this.brand = findProjectFromTemplatePath(templatePath);
    }

    public String getTitle() {
        return title;
    }
    
    public String getDescription() {
        return description;
    }

    public String getIconPath() {
        return iconPath;
    }   

    public String getCtaLabel() {
        return ctaLabel;
    }

    public String getCtaUrl() {
        return ctaUrl;
    }

    public boolean isCtaNewWindow() {
        return ctaNewWindow;
    }

    public String getBrand() {
        return brand;
    }

    public String getThemeClass() {
        return "feature-tile--" + brand.toLowerCase();
    }

    public boolean hasCta() {
        return ctaLabel != null && !ctaLabel.isEmpty() && ctaUrl != null && !ctaUrl.isEmpty();
    }

    // Utility method to extract project name from template path
    public String findProjectFromTemplatePath(String templatePath) {
        // Regex explanation:
        // ^/(?:conf|apps)/  -> Start with /conf/ or /apps/
        // ([^/]+)          -> Group 1: Capture everything up to the next slash (Project Name)
        // /settings/wcm/templates -> Match standard template path structure
        String regex = "^/(?:conf|apps)/([^/]+)/settings/wcm/templates";
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(templatePath);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return ""; 
        }
    }
}
