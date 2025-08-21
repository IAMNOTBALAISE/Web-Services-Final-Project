package com.example.apigatewayservice.businesslayer.productservicesBusinessLayer;



import com.example.apigatewayservice.domainclientlayer.ProductServiceClient;
import com.example.apigatewayservice.presentationlayer.catalogdtos.CatalogRequestModel;
import com.example.apigatewayservice.presentationlayer.catalogdtos.CatalogResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogServiceImpl implements CatalogService {


    private final ProductServiceClient productServiceClient;

    @Autowired
    public CatalogServiceImpl(ProductServiceClient productServiceClient) {
        this.productServiceClient = productServiceClient;

    }


    @Override
    public List<CatalogResponseModel> getCatalogs() {

        return this.productServiceClient.getCatalogs();

    }

    @Override
    public CatalogResponseModel getCatalogById(String catalogId) {

        return this.productServiceClient.getCatalogById(catalogId);

    }


    @Override
    public CatalogResponseModel addCatalog(CatalogRequestModel catalogRequestModel) {


        return this.productServiceClient.addCatalog(catalogRequestModel);
    }


    @Override
    public CatalogResponseModel updateCatalog(CatalogRequestModel catalogRequestModel, String catalogId){


        return this.productServiceClient.updateCatalog(catalogRequestModel,catalogId);

        }


    @Override
    public String deleteCatalog(String catalogId){

        return this.productServiceClient.deleteCatalog(catalogId);
    }



    }



