package com.example.productservices.presentationlayer.CatalogPresentationLayer;


import com.example.productservices.businesslayer.CatalogService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

        return ResponseEntity.ok().body(
                catalogService.getCatalogs()
        );
    }

    @GetMapping("/{catalogId}")
    public ResponseEntity<CatalogResponseModel> getCatalogById(@PathVariable String catalogId) {
        return ResponseEntity.ok().body(catalogService.getCatalogById(catalogId));
    }

    @PostMapping()
    public ResponseEntity<CatalogResponseModel> addCatalog(@RequestBody CatalogRequestModel catalogRequestModel) {

        CatalogResponseModel rm = catalogService.addCatalog(catalogRequestModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(rm);
    }

    @PutMapping("/{catalogId}")
    public ResponseEntity<CatalogResponseModel> updateCatalog(@RequestBody CatalogRequestModel catalogRequestModel,@PathVariable String catalogId) {

        return ResponseEntity.ok().body(catalogService.updateCatalog(catalogRequestModel,catalogId));
    }

    @DeleteMapping("/{catalogId}")
    public ResponseEntity<String> deleteCatalog(@PathVariable String catalogId) {

        this.catalogService.deleteCatalog(catalogId);
        return ResponseEntity.noContent().build();
    }
}
