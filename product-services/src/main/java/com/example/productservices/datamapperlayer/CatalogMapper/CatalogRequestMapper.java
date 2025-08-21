package com.example.productservices.datamapperlayer.CatalogMapper;


import com.example.productservices.dataccesslayer.catalog.Catalog;
import com.example.productservices.presentationlayer.CatalogPresentationLayer.CatalogRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CatalogRequestMapper {

    @Mapping(target = "id",ignore = true)
    @Mapping(target = "catalogIdentifier",ignore = true)
    Catalog requestModelToEntity(CatalogRequestModel catalogRequestModel);
}
