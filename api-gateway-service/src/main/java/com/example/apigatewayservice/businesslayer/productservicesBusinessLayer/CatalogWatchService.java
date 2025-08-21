package com.example.apigatewayservice.businesslayer.productservicesBusinessLayer;



import com.example.apigatewayservice.presentationlayer.watchdtos.WatchRequestModel;
import com.example.apigatewayservice.presentationlayer.watchdtos.WatchResponseModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface CatalogWatchService {


    List<WatchResponseModel> getWatchesInCatalogWithFiltering(String catalogId, Map<String, String> queryParams);

    WatchResponseModel getCatalogWatchByID(String watchId);

    WatchResponseModel addWatches(WatchRequestModel watchRequestModel, String catalogId);

    WatchResponseModel updateWatchInInventory(String catalogId, String watchId, WatchRequestModel watchRequestModel);

    String removeWatchInCatalog(String catalogId, String watchId);

    List<WatchResponseModel> getWatchesWithFilter(Map<String, String> queryParams);
}
