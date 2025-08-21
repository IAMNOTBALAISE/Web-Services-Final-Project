package com.example.apigatewayservice.domainclientlayer;

import com.example.apigatewayservice.presentationlayer.catalogdtos.CatalogRequestModel;
import com.example.apigatewayservice.presentationlayer.catalogdtos.CatalogResponseModel;
import com.example.apigatewayservice.presentationlayer.watchdtos.WatchRequestModel;
import com.example.apigatewayservice.presentationlayer.watchdtos.WatchResponseModel;
import com.example.apigatewayservice.utils.HttpErrorInfo;
import com.example.apigatewayservice.utils.InvalidInputException;
import com.example.apigatewayservice.utils.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ProductServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final String CATALOG_BASE_URL;
    private final String WATCH_BASE_URL;
    private final String CATALOG_WATCHES_BASE_URL;

    public ProductServiceClient(RestTemplate restTemplate,
                                ObjectMapper objectMapper,
                                @Value("${app.product-services.host}") String productServicesHost,
                                @Value("${app.product-services.port}") String productServicesPort) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;

        String baseUrl = "http://" + productServicesHost + ":" + productServicesPort;
        this.CATALOG_BASE_URL        = baseUrl + "/api/v1/catalogs";
        this.WATCH_BASE_URL          = baseUrl + "/api/v1/watches";
        this.CATALOG_WATCHES_BASE_URL= baseUrl + "/api/v1/catalogs/{catalogId}/watches";
}

//CatalogServiceImpl ///////////////

    public List<CatalogResponseModel> getCatalogs() {

        try {
            ResponseEntity<List<CatalogResponseModel>> resp = restTemplate.exchange(
                    CATALOG_BASE_URL,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            return resp.getBody();
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public CatalogResponseModel getCatalogById(String catalogId) {
        try {
            return restTemplate.getForObject(
                    CATALOG_BASE_URL + "/" + catalogId,
                    CatalogResponseModel.class
            );
        }
        catch (HttpClientErrorException.NotFound nf) {
            log.debug("Catalog {} not found → returning null", catalogId);
            return null;
        }
        catch (HttpClientErrorException ce) {
            throw handleHttpClientException(ce);
        }
    }

    public CatalogResponseModel addCatalog(CatalogRequestModel catalogRequestModel) {

        try {
            return restTemplate.postForObject(
                    CATALOG_BASE_URL,
                    catalogRequestModel,
                    CatalogResponseModel.class
            );
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public CatalogResponseModel updateCatalog(CatalogRequestModel catalogRequestModel, String catalogId) {
        try {
            restTemplate.put(
                    CATALOG_BASE_URL + "/" + catalogId,
                    catalogRequestModel
            );
            return getCatalogById(catalogId);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public String deleteCatalog(String catalogId) {

        try {
            restTemplate.delete(CATALOG_BASE_URL + "/" + catalogId);
            return "Catalog deleted successfully.";
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    //Watch Endpoint


    public List<WatchResponseModel> getWatchesWithFilter(Map<String,String> queryParams) {
        try {
            UriComponentsBuilder b = UriComponentsBuilder.fromUriString(WATCH_BASE_URL);
            queryParams.forEach(b::queryParam);
            ResponseEntity<List<WatchResponseModel>> resp = restTemplate.exchange(
                    b.toUriString(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            return resp.getBody();
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public WatchResponseModel getCatalogWatchByID(String watchId) {
        try {
            return restTemplate.getForObject(
                    WATCH_BASE_URL + "/" + watchId,
                    WatchResponseModel.class
            );
        }
        catch (HttpClientErrorException.NotFound nf) {
            log.debug("Watch {} not found → returning null", watchId);
            return null;
        }
        catch (HttpClientErrorException ce) {
            throw handleHttpClientException(ce);
        }
    }


    //CatalogWatchServiceImpl ///////////////////

    public List<WatchResponseModel> getWatchesInCatalogWithFiltering(String catalogId, Map<String, String> queryParams) {

        try {
            UriComponentsBuilder b = UriComponentsBuilder
                    .fromUriString(CATALOG_WATCHES_BASE_URL.replace("{catalogId}", catalogId));
            queryParams.forEach(b::queryParam);
            ResponseEntity<List<WatchResponseModel>> resp = restTemplate.exchange(
                    b.toUriString(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            return resp.getBody();
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }

    }


    public WatchResponseModel addWatches(WatchRequestModel watchRequestModel, String catalogId) {

        try {
            String url = CATALOG_WATCHES_BASE_URL.replace("{catalogId}", catalogId);
            return restTemplate.postForObject(
                    url,
                    watchRequestModel,
                    WatchResponseModel.class
            );
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public WatchResponseModel updateWatchInInventory(String catalogId, String watchId, WatchRequestModel watchRequestModel) {

        try {
            String url = CATALOG_WATCHES_BASE_URL.replace("{catalogId}", catalogId) + "/" + watchId;
            restTemplate.put(url, watchRequestModel);
            return getCatalogWatchByID(watchId);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public String removeWatchInCatalog(String catalogId, String watchId) {

        try {
            String url = CATALOG_WATCHES_BASE_URL.replace("{catalogId}", catalogId) + "/" + watchId;
            restTemplate.delete(url);
            return "Watch deleted successfully.";
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }


    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return objectMapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        }catch (IOException ioex){
            return ioex.getMessage();
        }
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException ex){

        HttpStatus statusCode = (HttpStatus) ex.getStatusCode();
        String errorMessage = getErrorMessage(ex);

        if (statusCode == HttpStatus.UNPROCESSABLE_ENTITY) {
            return new InvalidInputException(errorMessage);
        } else if (statusCode == HttpStatus.NOT_FOUND) {
            return new NotFoundException(errorMessage);
        } else if (statusCode == HttpStatus.BAD_REQUEST ||
                statusCode == HttpStatus.CONFLICT) {
            // Handle DuplicateCustomerEmailException or other validation errors
            return new InvalidInputException(errorMessage);
        }

        // Log the unexpected error
        log.warn("Unexpected HTTP error: {}. Error body: {}", statusCode, ex.getResponseBodyAsString());
        return new InvalidInputException(errorMessage); // Convert to InvalidInputException instead of returning ex directly

    }
}
