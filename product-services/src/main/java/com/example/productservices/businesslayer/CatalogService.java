package com.example.productservices.businesslayer;



import com.example.productservices.presentationlayer.CatalogPresentationLayer.CatalogRequestModel;
import com.example.productservices.presentationlayer.CatalogPresentationLayer.CatalogResponseModel;
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
