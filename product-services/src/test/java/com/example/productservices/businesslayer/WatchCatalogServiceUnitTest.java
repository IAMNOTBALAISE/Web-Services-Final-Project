package com.example.productservices.businesslayer;

import com.example.productservices.dataccesslayer.catalog.Catalog;
import com.example.productservices.dataccesslayer.catalog.CatalogIdentifier;
import com.example.productservices.dataccesslayer.catalog.CatalogRepository;
import com.example.productservices.dataccesslayer.watch.*;
import com.example.productservices.datamapperlayer.WatchMapper.WatchRequestMapper;
import com.example.productservices.datamapperlayer.WatchMapper.WatchResponseMapper;
import com.example.productservices.presentationlayer.WatchPresentationLayer.WatchRequestModel;
import com.example.productservices.presentationlayer.WatchPresentationLayer.WatchResponseModel;
import com.example.productservices.utils.ResourceNotFoundException;
import com.example.productservices.utils.exceptions.DuplicateWatchModelException;
import com.example.productservices.utils.exceptions.InvalidInputException;
import com.example.productservices.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.productservices.dataccesslayer.watch.WatchBrand;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WatchCatalogServiceUnitTest {

    @Mock
    private CatalogRepository catalogRepo;

    @Mock
    private WatchRepository watchRepo;

    @Spy
    private WatchRequestMapper reqMapper = Mappers.getMapper(WatchRequestMapper.class);
    @Spy
    private WatchResponseMapper resMapper = Mappers.getMapper(WatchResponseMapper.class);

    @InjectMocks
    private CatalogWatchServiceImpl service;

    private Catalog makeCatalog(String id) {
        Catalog c = new Catalog();
        c.setCatalogIdentifier(new CatalogIdentifier(id));
        return c;
    }

    private Watch makeWatch(String watchId, String catalogId) {
        Watch w = new Watch();
        w.setWatchIdentifier(new WatchIdentifier(watchId));
        w.setCatalogIdentifier(new CatalogIdentifier(catalogId));
        w.setQuantity(10);
//        w.setWatchStatus(WatchStatus.AVAILABLE);
        w.setUsageType(UsageType.NEW);
        w.setModel("Model");
        w.setMaterial("Steel");
        w.setAccessories(Collections.emptyList());
        WatchBrand brand = new WatchBrand();
        brand.setBrandName("Brand");
        brand.setBrandCountry("Country");
        w.setWatchBrand(brand);
        w.setPrice(new Price(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        return w;
    }

    private WatchRequestModel makeRequest() {
        WatchRequestModel r = new WatchRequestModel();
        r.setModel("M1");
        r.setMaterial("Mat");
        r.setQuantity(5);
//        r.setWatchStatus(WatchStatus.AVAILABLE);
        r.setUsageType(UsageType.NEW);
        r.setAccessories(Collections.emptyList());
        WatchBrand brand = new WatchBrand();
        brand.setBrandName("Brand");
        brand.setBrandCountry("Country");
        r.setWatchBrand(brand);
        // DTO Price (msrp, cost)
        r.setPrice(new Price(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        return r;
    }

    // ─── Positive test: getWatchesWithFilter returns mapped list and uses response mapper ─────
    @Test
    public void getWatchesWithFilter_returnsMappedList_and_usesResponseMapper() {
        Watch w = makeWatch("W1", "C1");
        when(watchRepo.findAll()).thenReturn(List.of(w));

        List<WatchResponseModel> out = service.getWatchesWithFilter(Collections.emptyMap());

        assertEquals(1, out.size());
        assertEquals("W1", out.get(0).getWatchId());
        verify(watchRepo).findAll();
        // verify that the spy response mapper was called
        verify(resMapper).entityListToResponseModelList(List.of(w));
    }

    // ─── Positive test: getCatalogWatchByID existing returns model and uses response mapper ──
    @Test
    public void getCatalogWatchByID_existing_returnsModel_and_usesResponseMapper() {
        Watch w = makeWatch("W2", "C2");
        when(watchRepo.findByWatchIdentifier_WatchId("W2")).thenReturn(w);

        WatchResponseModel resp = service.getCatalogWatchByID("W2");

        assertEquals("W2", resp.getWatchId());
        verify(watchRepo).findByWatchIdentifier_WatchId("W2");
        // verify mapper invocation
        verify(resMapper).entityToResponseModel(w);
    }

    // ─── Negative test: getCatalogWatchByID missing throws and does NOT call response mapper ─
    @Test
    public void getCatalogWatchByID_notFound_throwsAndNoMapper() {
        when(watchRepo.findByWatchIdentifier_WatchId("XX")).thenReturn(null);
        assertThrows(InvalidInputException.class,
                () -> service.getCatalogWatchByID("XX"));
        verify(resMapper, org.mockito.Mockito.never()).entityToResponseModel(any());
    }


    // ─── Positive test: addWatches success uses both mappers and saves twice ─────────────────
    @Test
    public void addWatches_success_saves_and_usesBothMappers() {
        when(catalogRepo.findByCatalogIdentifier_CatalogId("C2"))
                .thenReturn(makeCatalog("C2"));
        when(watchRepo.existsByModelAndCatalogIdentifier_CatalogId("M1", "C2"))
                .thenReturn(false);
        when(watchRepo.save(any(Watch.class)))
                .thenAnswer(i -> i.getArgument(0));

        WatchResponseModel resp = service.addWatches(makeRequest(), "C2");

        assertEquals("M1", resp.getModel());
        // verify we went through the request->entity mapper
        verify(reqMapper).requestModelToEntity(any(WatchRequestModel.class));
        // verify we then went through the entity->response mapper
        verify(resMapper).entityToResponseModel(any(Watch.class));
        // expect exactly one save (the service only calls save once)
        verify(watchRepo, times(1)).save(any(Watch.class));
    }
    // ─── Negative test: addWatches duplicate model throws and no mapping to save ─────────────
    @Test
    public void addWatches_duplicateModel_throwsAndNoMapper() {
        when(catalogRepo.findByCatalogIdentifier_CatalogId("C3")).thenReturn(makeCatalog("C3"));
        when(watchRepo.existsByModelAndCatalogIdentifier_CatalogId("M1", "C3")).thenReturn(true);
        assertThrows(DuplicateWatchModelException.class,
                () -> service.addWatches(makeRequest(), "C3"));
        verify(reqMapper, org.mockito.Mockito.never()).requestModelToEntity(any());
        verify(watchRepo, org.mockito.Mockito.never()).save(any());
    }

    // Positive test: removeWatchInCatalog returns the full success message
    @Test
    public void removeWatchInCatalog_existing_returnsSuccessMessage() {
        when(catalogRepo.findByCatalogIdentifier_CatalogId("C4")).thenReturn(makeCatalog("C4"));
        Watch w = makeWatch("W4", "C4");
        when(watchRepo.findByWatchIdentifier_WatchId("W4")).thenReturn(w);

        String msg = service.removeWatchInCatalog("C4", "W4");

        assertEquals("Watch with IDW4 was successfully removed", msg);
        verify(watchRepo).delete(w);
    }

    // ─── Negative test: removeWatchInCatalog missing catalog throws ────────────────────────
    @Test
    public void removeWatchInCatalog_noCatalog_throws() {
        when(catalogRepo.findByCatalogIdentifier_CatalogId("XX")).thenReturn(null);
        assertThrows(InvalidInputException.class,
                () -> service.removeWatchInCatalog("XX", "W"));
        verify(watchRepo, org.mockito.Mockito.never()).delete(any());
    }

    // ─── Negative test: removeWatchInCatalog missing watch throws ──────────────────────────
    @Test
    public void removeWatchInCatalog_noWatch_throws() {
        when(catalogRepo.findByCatalogIdentifier_CatalogId("C5")).thenReturn(makeCatalog("C5"));
        when(watchRepo.findByWatchIdentifier_WatchId("WY")).thenReturn(null);
        assertThrows(NotFoundException.class,
                () -> service.removeWatchInCatalog("C5", "WY"));
    }

    // ─── Direct mapper tests ──────────────────────────────────────────────────────────────

    // Positive: request mapper returns null when given null input
    @Test
    public void requestMapper_nullRequest_returnsNull() {
        assertNull(reqMapper.requestModelToEntity(null));
    }
    // Positive: response mapper returns null when given null
    @Test
    public void responseMapper_nullEntity_returnsNull() {
        assertNull(resMapper.entityToResponseModel(null));
        assertNull(resMapper.entityListToResponseModelList(null));
    }

    // Positive Test: Verifies custom message is correctly set
    @Test
    void testConstructorWithMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Test error message");
        assertEquals("Test error message", ex.getMessage());
    }

    //Positive Test: Verifies static factory method includes correct text and ID
    @Test
    void testStaticSaleNotFoundMethod() {
        UUID saleId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        ResourceNotFoundException ex = ResourceNotFoundException.saleNotFound(saleId);
        assertTrue(ex.getMessage().contains("Sale not found with ID: "));
        assertTrue(ex.getMessage().contains(saleId.toString()));
    }
//Positive Test: Default constructor should result in null message
    @Test
    void testDefaultConstructor() {
        NotFoundException ex = new NotFoundException();
        assertNull(ex.getMessage());
    }
// Positive Test: Verifies constructor sets message correctly
    @Test
    void testMessageConstructor() {
        NotFoundException ex = new NotFoundException("Resource not found");
        assertEquals("Resource not found", ex.getMessage());
    }
//Positive Test: Verifies cause is stored properly
    @Test
    void testCauseConstructor() {
        Throwable cause = new RuntimeException("Root cause");
        NotFoundException ex = new NotFoundException(cause);
        assertEquals(cause, ex.getCause());
    }
//Positive Test: Verifies both message and cause are correctly set
    @Test
    void testMessageAndCauseConstructor() {
        Throwable cause = new RuntimeException("Root cause");
        NotFoundException ex = new NotFoundException("Error occurred", cause);
        assertEquals("Error occurred", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}
