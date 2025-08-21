package com.example.apigatewayservice.businesslayer.productservicesBusinessLayer;




import com.example.apigatewayservice.domainclientlayer.ProductServiceClient;
import com.example.apigatewayservice.presentationlayer.watchdtos.WatchRequestModel;
import com.example.apigatewayservice.presentationlayer.watchdtos.WatchResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CatalogWatchServiceImpl implements CatalogWatchService {



   private final ProductServiceClient productServiceClient;


    @Autowired
    public CatalogWatchServiceImpl(ProductServiceClient productServiceClient) {

        this.productServiceClient = productServiceClient;

    }

    @Override
    public List<WatchResponseModel> getWatchesInCatalogWithFiltering(String catalogId, Map<String, String> queryParams) {


        return this.productServiceClient.getWatchesInCatalogWithFiltering(catalogId,queryParams);

    }

    @Override
    public List<WatchResponseModel> getWatchesWithFilter(Map<String, String> queryParams) {


        return this.productServiceClient.getWatchesWithFilter(queryParams);
    }

    @Override
    public WatchResponseModel getCatalogWatchByID(String watchId) {

        return this.productServiceClient.getCatalogWatchByID(watchId);

    }

    @Override
    public WatchResponseModel addWatches(WatchRequestModel watchRequestModel, String catalogId) {

        return this.productServiceClient.addWatches(watchRequestModel,catalogId);

    }


    @Override
    public WatchResponseModel updateWatchInInventory(String catalogId, String watchId, WatchRequestModel watchRequestModel) {



        return this.productServiceClient.updateWatchInInventory(catalogId,watchId,watchRequestModel);


        }





    @Override
    public  String removeWatchInCatalog(String catalogId, String watchId){

        return this.productServiceClient.removeWatchInCatalog(catalogId,watchId);

    }

}
