package com.example.productservices.presentationlayer.WatchPresentationLayer;


import com.example.productservices.businesslayer.CatalogWatchService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/v1/watches")
public class WatchController {

    private final CatalogWatchService catalogWatchService;

    public WatchController(CatalogWatchService catalogWatchService) {
        this.catalogWatchService = catalogWatchService;
    }

    @GetMapping()
    public ResponseEntity<List<WatchResponseModel>> getWatchesWithFilter(@RequestParam Map <String,String> queryParams){

        return ResponseEntity.ok().body(catalogWatchService.getWatchesWithFilter(queryParams));
    }

    @GetMapping("/{watchId}")
    public ResponseEntity<WatchResponseModel> getWatchInCatalogByID(@PathVariable String watchId){

        return ResponseEntity.ok().body(catalogWatchService.getCatalogWatchByID(watchId));
    }
}



