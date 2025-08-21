package com.example.apigatewayservice.businesslayer.productservicesBusinessLayer;




import com.example.apigatewayservice.presentationlayer.catalogdtos.CatalogRequestModel;
import com.example.apigatewayservice.presentationlayer.catalogdtos.CatalogResponseModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CatalogService {


    List<CatalogResponseModel> getCatalogs();

    CatalogResponseModel getCatalogById(String catalogId);

    CatalogResponseModel addCatalog(CatalogRequestModel catalogRequestModel);

    CatalogResponseModel updateCatalog(CatalogRequestModel catalogRequestModel, String catalogId);

    String deleteCatalog(String catalogId);
}
