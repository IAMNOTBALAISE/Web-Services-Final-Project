package com.example.productservices.datamapperlayer.CatalogMapper;



import com.example.productservices.dataccesslayer.catalog.Catalog;
import com.example.productservices.presentationlayer.CatalogPresentationLayer.CatalogResponseModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CatalogResponseMapper {

    @Mapping(expression = "java(catalog.getCatalogIdentifier().getCatalogId())",target = "catalogId")
    CatalogResponseModel entityToResponseModel(Catalog catalog);

    List<CatalogResponseModel> entityListToResponseModelList(List<Catalog> catalogs);
}
