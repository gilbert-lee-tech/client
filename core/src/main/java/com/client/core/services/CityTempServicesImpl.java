package com.client.core.services;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragment;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Component(service = CityTempServices.class, immediate = true)
public class CityTempServicesImpl implements CityTempServices {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    
    @Reference
    private ResourceResolverFactory resolverFactory;

    @Override
    public String getCityTemp(String city) {
        ResourceResolver resourceResolver = resolverFactory.getThreadResourceResolver();
        Resource cfResource = resourceResolver.getResource("/content/dam/interview/weather-cf/" + city);

        if (cfResource != null) {
            // 2. Adapt the resource to the ContentFragment interface
            ContentFragment fragment = cfResource.adaptTo(ContentFragment.class);
            
            if (fragment != null) {
                ContentElement latitudeElement = fragment.hasElement("latitude") ? fragment.getElement("latitude") : null;
                ContentElement longitudeElement = fragment.hasElement("longitude") ? fragment.getElement("longitude") : null;

                if (latitudeElement == null || longitudeElement == null) {
                    return null;
                }

                String latitudeValue = latitudeElement.getContent();
                String longitudeValue = longitudeElement.getContent();

                return getCurrentTemperature(latitudeValue, longitudeValue);
            }
        }

        return null;
    }

    private String getCurrentTemperature(String latitudeValue, String longitudeValue) {
        if (StringUtils.isEmpty(latitudeValue)|| StringUtils.isEmpty(longitudeValue)) {
            return null;
        }

        JsonObject weatherData = fetchData(latitudeValue, longitudeValue);

        if (weatherData != null && weatherData.has("current")) {
            JsonObject currentWeather = weatherData.getAsJsonObject("current");
            if (currentWeather.has("temperature_2m")) {
                return currentWeather.get("temperature_2m").getAsString();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    protected JsonObject fetchData(String latitude, String longitude) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("https://api.open-meteo.com/v1/forecast?latitude=" + latitude + "&longitude=" + longitude + "&current=temperature_2m");

            request.addHeader("Accept", "application/json");

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String jsonString = EntityUtils.toString(response.getEntity());

                return JsonParser.parseString(jsonString).getAsJsonObject();
            }
        } catch (Exception e) {
            logger.error("Error fetching weather data: {} {}", latitude, longitude, e);
            return null;
        }
    }
}