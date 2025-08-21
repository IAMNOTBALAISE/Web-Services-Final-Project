package com.example.apigatewayservice.presentationlayer;



import com.example.apigatewayservice.businesslayer.productservicesBusinessLayer.CatalogService;
import com.example.apigatewayservice.presentationlayer.catalogdtos.CatalogRequestModel;
import com.example.apigatewayservice.presentationlayer.catalogdtos.CatalogResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/catalogs")
public class CatalogController {

    private CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping()
    public ResponseEntity<List<CatalogResponseModel>> getCatalogs() {

        List<CatalogResponseModel> list = catalogService.getCatalogs();
        list.forEach(c -> {
            c.add(linkTo(methodOn(CatalogController.class)
                    .getCatalogById(c.getCatalogId())).withSelfRel());
            c.add(linkTo(methodOn(CatalogController.class)
                    .getCatalogs()).withRel("all-catalogs"));
            c.add(linkTo(methodOn(CatalogWatchController.class)
                    .getWatchesInCatalogWithFiltering(c.getCatalogId(), null)).withRel("watches"));
        });
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{catalogId}")
    public ResponseEntity<CatalogResponseModel> getCatalogById(@PathVariable String catalogId) {
        CatalogResponseModel c = catalogService.getCatalogById(catalogId);
        c.add(linkTo(methodOn(CatalogController.class)
                .getCatalogById(catalogId)).withSelfRel());
        c.add(linkTo(methodOn(CatalogController.class)
                .getCatalogs()).withRel("all-catalogs"));
        c.add(linkTo(methodOn(CatalogWatchController.class)
                .getWatchesInCatalogWithFiltering(catalogId, null)).withRel("watches"));
        return ResponseEntity.ok(c);
    }

    @PostMapping()
    public ResponseEntity<CatalogResponseModel> addCatalog(@RequestBody CatalogRequestModel catalogRequestModel) {

        CatalogResponseModel c = catalogService.addCatalog(catalogRequestModel);
        c.add(linkTo(methodOn(CatalogController.class)
                .getCatalogById(c.getCatalogId())).withSelfRel());
        c.add(linkTo(methodOn(CatalogController.class)
                .getCatalogs()).withRel("all-catalogs"));
        return ResponseEntity.status(HttpStatus.CREATED).body(c);
    }

    @PutMapping("/{catalogId}")
    public ResponseEntity<CatalogResponseModel> updateCatalog(@RequestBody CatalogRequestModel catalogRequestModel,@PathVariable String catalogId) {

        CatalogResponseModel c = catalogService.updateCatalog(catalogRequestModel, catalogId);
        c.add(linkTo(methodOn(CatalogController.class)
                .getCatalogById(catalogId)).withSelfRel());
        c.add(linkTo(methodOn(CatalogController.class)
                .getCatalogs()).withRel("all-catalogs"));
        return ResponseEntity.ok(c);
    }

    @DeleteMapping("/{catalogId}")
    public ResponseEntity<String> deleteCatalog(@PathVariable String catalogId) {

        catalogService.deleteCatalog(catalogId);
        Link all = linkTo(methodOn(CatalogController.class).getCatalogs()).withRel("all-catalogs");
        return ResponseEntity
                .noContent()
                .header(HttpHeaders.LINK, all.toUri().toString())
                .build();
    }
}
