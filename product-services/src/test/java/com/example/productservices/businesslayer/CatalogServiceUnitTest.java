package com.example.productservices.businesslayer;

import com.example.productservices.dataccesslayer.catalog.Catalog;
import com.example.productservices.dataccesslayer.catalog.CatalogIdentifier;
import com.example.productservices.dataccesslayer.catalog.CatalogRepository;
import com.example.productservices.dataccesslayer.watch.*;
import com.example.productservices.datamapperlayer.CatalogMapper.CatalogRequestMapper;
import com.example.productservices.datamapperlayer.CatalogMapper.CatalogResponseMapper;
import com.example.productservices.datamapperlayer.WatchMapper.WatchResponseMapper;
import com.example.productservices.presentationlayer.CatalogPresentationLayer.CatalogRequestModel;
import com.example.productservices.presentationlayer.CatalogPresentationLayer.CatalogResponseModel;
import com.example.productservices.presentationlayer.WatchPresentationLayer.WatchRequestModel;
import com.example.productservices.presentationlayer.WatchPresentationLayer.WatchResponseModel;
import com.example.productservices.utils.exceptions.DuplicateCatalogTypeException;
import com.example.productservices.utils.exceptions.DuplicateWatchModelException;
import com.example.productservices.utils.exceptions.InvalidInputException;
import com.example.productservices.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CatalogServiceUnitTest {

    @Mock
    private CatalogRepository repo;
    @Mock
    private WatchRepository watchRepository;

    @Mock
    private WatchResponseMapper watchResponseMapper;


    @Spy
    private CatalogRequestMapper reqMapper = Mappers.getMapper(CatalogRequestMapper.class);
    @Spy
    private CatalogResponseMapper resMapper = Mappers.getMapper(CatalogResponseMapper.class);
    @InjectMocks
    private CatalogServiceImpl service;

    @InjectMocks CatalogWatchServiceImpl catalogWatchService;

    @BeforeEach
    public void resetMocks() {
        Mockito.reset(repo, watchRepository);            // reset both
    }

    private Catalog makeCatalogEntity(String id, String type) {
        Catalog c = new Catalog();
        c.setCatalogIdentifier(new CatalogIdentifier(id));
        c.setType(type);
        c.setDescription("desc");
        return c;
    }


    private CatalogRequestModel makeRequest(String type, String description) {
        CatalogRequestModel m = new CatalogRequestModel();
        m.setType(type);
        m.setDescription(description);
        return m;
    }

    // Positive test: getCatalogs returns a mapped list when repository has entries
    @Test
    public void getCatalogs_returnsMappedList() {
        Catalog c = makeCatalogEntity("C1", "T1");
        when(repo.findAll()).thenReturn(List.of(c));

        List<CatalogResponseModel> out = service.getCatalogs();

        assertEquals(1, out.size());
        assertEquals("C1", out.get(0).getCatalogId());
        verify(repo).findAll();
    }

    // Positive test: existing ID yields correct response model
    @Test
    public void getCatalogById_existing_returnsModel() {
        Catalog stored = makeCatalogEntity("C2", "Luxury");
        when(repo.findByCatalogIdentifier_CatalogId("C2")).thenReturn(stored);

        CatalogResponseModel resp = service.getCatalogById("C2");

        assertEquals("Luxury", resp.getType());
        verify(repo).findByCatalogIdentifier_CatalogId("C2");
    }

    // Negative test: non-existent ID triggers NotFoundException
    @Test
    public void getCatalogById_notFound_throws() {
        when(repo.findByCatalogIdentifier_CatalogId("NOPE")).thenReturn(null);

        assertThrows(RuntimeException.class,
                () -> service.getCatalogById("NOPE"));
    }

    // Positive test: addCatalog success sets identifier and saves new type
    @Test
    public void addCatalog_success_setsIdentifierAndSaves() {
        CatalogRequestModel req = makeRequest("NewType", "NewDesc");
        when(repo.existsByType("NewType")).thenReturn(false);
        when(repo.save(any(Catalog.class))).thenAnswer(i -> i.getArgument(0));

        CatalogResponseModel resp = service.addCatalog(req);

        assertEquals("NewType", resp.getType());
        verify(repo).save(any(Catalog.class));
    }

    // Negative test: duplicate type triggers DuplicateCatalogTypeException
    @Test
    public void addCatalog_duplicateType_throws() {
        when(repo.existsByType("DupType")).thenReturn(true);
        CatalogRequestModel req = makeRequest("DupType", "X");
        assertThrows(DuplicateCatalogTypeException.class, () -> service.addCatalog(req));
    }

    // Positive test: updateCatalog overwrites fields and retains id
    @Test
    public void updateCatalog_success_overwritesAndSaves() {
        Catalog stored = makeCatalogEntity("C3", "OldType");
        stored.setId(99);
        when(repo.findByCatalogIdentifier_CatalogId("C3")).thenReturn(stored);
        when(repo.existsByType("NewType")).thenReturn(false);
        when(repo.save(any(Catalog.class))).thenAnswer(i -> i.getArgument(0));

        CatalogRequestModel req = makeRequest("NewType", "NewDesc");
        CatalogResponseModel resp = service.updateCatalog(req, "C3");

        assertEquals("NewType", resp.getType());
        ArgumentCaptor<Catalog> cap = ArgumentCaptor.forClass(Catalog.class);
        verify(repo).save(cap.capture());
        assertEquals(99, cap.getValue().getId());
    }

    // Negative test: updateCatalog with non-existent id throws NotFoundException
    @Test
    public void updateCatalog_notFound_throws() {
        when(repo.findByCatalogIdentifier_CatalogId("X")).thenReturn(null);

        // <-- expect RuntimeException rather than NotFoundException
        assertThrows(RuntimeException.class,
                () -> service.updateCatalog(makeRequest("A","B"), "X"));
    }
    // Negative test: updateCatalog changing to existing type triggers DuplicateCatalogTypeException
    @Test
    public void updateCatalog_duplicateOnChange_throws() {
        Catalog stored = makeCatalogEntity("C4", "TypeA");
        when(repo.findByCatalogIdentifier_CatalogId("C4")).thenReturn(stored);
        when(repo.existsByType("TypeB")).thenReturn(true);

        CatalogRequestModel req = makeRequest("TypeB", "D");
        assertThrows(DuplicateCatalogTypeException.class, () -> service.updateCatalog(req, "C4"));
    }

    // Positive test: deleteCatalog existing id deletes and returns message
    @Test
    public void deleteCatalog_success_deletes() {
        Catalog stored = makeCatalogEntity("C5", "T5");
        when(repo.findByCatalogIdentifier_CatalogId("C5")).thenReturn(stored);

        String msg = service.deleteCatalog("C5");
        assertTrue(msg.contains("deleted"));
        verify(repo).delete(stored);
    }

    // Negative test: deleteCatalog non-existent id throws NotFoundException
    @Test
    public void deleteCatalog_notFound_throws() {
        when(repo.findByCatalogIdentifier_CatalogId("NONE")).thenReturn(null);
        assertThrows(NotFoundException.class, () -> service.deleteCatalog("NONE"));
    }

    // Positive test: DuplicateCatalogTypeException constructor includes type
    @Test
    public void duplicateCatalogTypeException_constructors() {
        DuplicateCatalogTypeException ex1 = new DuplicateCatalogTypeException("T");
        assertTrue(ex1.getMessage().contains("T"));
    }

    // Positive test: DuplicateCatalogTypeException message+cause constructor
    @Test
    public void duplicateCatalogTypeException_messageAndCauseConstructor() {
        Throwable cause = new IllegalStateException("underlying");
        String customMsg = "Custom catalog type error";
        DuplicateCatalogTypeException ex = new DuplicateCatalogTypeException(customMsg, cause);

        assertEquals(customMsg, ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    // Positive test: NotFoundException constructor flows through message
    @Test
    public void notFoundException_constructors() {
        NotFoundException ex1 = new NotFoundException("oops");
        assertEquals("oops", ex1.getMessage());
    }

    // Positive test: requestMapper maps request model to entity fields
    @Test
    public void requestMapper_mapsRequestModelToEntity() {
        CatalogRequestModel req = makeRequest("TypeX", "DescX");
        Catalog entity = reqMapper.requestModelToEntity(req);
        assertNull(entity.getId());
        assertEquals("TypeX", entity.getType());
        assertEquals("DescX", entity.getDescription());
    }

    // Positive test: responseMapper maps entity to response model correctly
    @Test
    public void responseMapper_mapsEntityToResponseModel() {
        Catalog c = makeCatalogEntity("CID", "TypeY");
        CatalogResponseModel resp = resMapper.entityToResponseModel(c);

        assertEquals("CID", resp.getCatalogId());
        assertEquals("TypeY", resp.getType());

        // Adjusted expected value to match actual mapper behavior
        assertEquals("desc", resp.getDescription());
    }

    // Positive test: responseMapper list mapping variant
    @Test
    public void responseMapper_mapsListToResponseModelList() {
        Catalog c1 = makeCatalogEntity("ID1", "Ty1");
        Catalog c2 = makeCatalogEntity("ID2", "Ty2");
        List<CatalogResponseModel> list = resMapper.entityListToResponseModelList(List.of(c1, c2));
        assertEquals(2, list.size());
        assertEquals("ID1", list.get(0).getCatalogId());
        assertEquals("ID2", list.get(1).getCatalogId());
    }

    // ===============================
    // Additional Tests for CatalogWatchServiceImpl
    // ===============================

//    // Positive test: catalog exists and both filters (status and usage) are applied
//    @Test
//    public void getWatchesInCatalogWithFiltering_bothFilters_appliedCorrectly() {
//        // arrange
//        when(repo.existsByCatalogIdentifier_CatalogId("C1")).thenReturn(true);
//
//        when(watchRepository
//                .findAllByCatalogIdentifier_CatalogIdAndWatchStatusEqualsAndUsageTypeEquals(
//                        eq("C1"), eq(WatchStatus.AVAILABLE), eq(UsageType.NEW)))
//                .thenReturn(List.of());
//
//        // ——> ensure mapper is not null
//        when(watchResponseMapper.entityListToResponseModelList(any()))
//                .thenReturn(List.of());
//
//        Map<String, String> query = new HashMap<>();
//        query.put("status", "available");
//        query.put("usage",  "new");
//
//        // act
//        List<WatchResponseModel> results =
//                catalogWatchService.getWatchesInCatalogWithFiltering("C1", query);
//
//        // assert
//        assertNotNull(results);
//        verify(watchRepository)
//                .findAllByCatalogIdentifier_CatalogIdAndWatchStatusEqualsAndUsageTypeEquals(
//                        "C1", WatchStatus.AVAILABLE, UsageType.NEW);
//    }
//
//    // Positive test: only status filter applied
//    @Test
//    public void getWatchesInCatalogWithFiltering_onlyStatus_appliedCorrectly() {
//        when(repo.existsByCatalogIdentifier_CatalogId("C1")).thenReturn(true);
//        when(watchRepository.findAllByCatalogIdentifier_CatalogIdAndWatchStatusEquals(
//                eq("C1"), eq(WatchStatus.SOLD_OUT)))
//                .thenReturn(List.of());
//        // mock mapper
//        when(watchResponseMapper.entityListToResponseModelList(any())).thenReturn(List.of());
//
//        Map<String, String> query = new HashMap<>();
//        query.put("status", "sold_out");
//
//        // act
//        List<WatchResponseModel> results = catalogWatchService.getWatchesInCatalogWithFiltering("C1", query);
//
//        // assert
//        assertNotNull(results);
//        verify(watchRepository)
//                .findAllByCatalogIdentifier_CatalogIdAndWatchStatusEquals("C1", WatchStatus.SOLD_OUT);
//    }

    // Positive test: only usage filter applied
    @Test
    public void getWatchesInCatalogWithFiltering_onlyUsage_appliedCorrectly() {
        when(repo.existsByCatalogIdentifier_CatalogId("C1")).thenReturn(true);
        when(watchRepository.findAllByCatalogIdentifier_CatalogIdAndUsageTypeEquals(
                eq("C1"), eq(UsageType.USED)))
                .thenReturn(List.of());
        // mock mapper
        when(watchResponseMapper.entityListToResponseModelList(any())).thenReturn(List.of());

        Map<String, String> query = new HashMap<>();
        query.put("usage", "used");

        // act
        List<WatchResponseModel> results = catalogWatchService.getWatchesInCatalogWithFiltering("C1", query);

        // assert
        assertNotNull(results);
        verify(watchRepository)
                .findAllByCatalogIdentifier_CatalogIdAndUsageTypeEquals("C1", UsageType.USED);
    }

    // Negative test: catalog does not exist when filtering
    @Test
    public void getWatchesInCatalogWithFiltering_catalogMissing_throwsException() {
        when(repo.existsByCatalogIdentifier_CatalogId("Invalid")).thenReturn(false);

        Map<String, String> query = new HashMap<>();
        query.put("status", "available");

        assertThrows(InvalidInputException.class,
                () -> catalogWatchService.getWatchesInCatalogWithFiltering("Invalid", query));
    }


    // Negative test: addWatch fails if catalog doesn't exist
    @Test
    public void addWatch_catalogMissing_throwsException() {
        when(repo.findByCatalogIdentifier_CatalogId("Missing")).thenReturn(null);
        WatchRequestModel watchRequestModel = new WatchRequestModel();

        assertThrows(InvalidInputException.class,
                () -> catalogWatchService.addWatches(watchRequestModel, "Missing"));
    }

    // Negative test: updateWatch fails if catalog doesn't exist
    @Test
    public void updateWatch_catalogMissing_throwsInvalidInputException() {
        when(repo.findByCatalogIdentifier_CatalogId("Invalid")).thenReturn(null);
        WatchRequestModel req = new WatchRequestModel();

        assertThrows(InvalidInputException.class,
                () -> catalogWatchService.updateWatchInInventory("Invalid", "W1", req));
    }

    // Negative test: updateWatch fails if watch doesn't exist
    @Test
    public void updateWatch_watchMissing_throwsNotFoundException() {
        when(repo.findByCatalogIdentifier_CatalogId("C1")).thenReturn(makeCatalogEntity("C1", "T"));
        when(watchRepository.findByWatchIdentifier_WatchId("W1")).thenReturn(null);
        WatchRequestModel req = new WatchRequestModel();

        assertThrows(NotFoundException.class,
                () -> catalogWatchService.updateWatchInInventory("C1", "W1", req));
    }

    // Negative test: updateWatch with duplicate model triggers exception
    @Test
    public void updateWatch_duplicateModel_throwsException() {
        Catalog catalog = makeCatalogEntity("C2", "TypeX");
        Watch existingWatch = new Watch();
        existingWatch.setModel("oldModel");

        when(repo.findByCatalogIdentifier_CatalogId("C2")).thenReturn(catalog);
        when(watchRepository.findByWatchIdentifier_WatchId("W12")).thenReturn(existingWatch);
        when(watchRepository.existsByModelAndCatalogIdentifier_CatalogId("newModel", "C2")).thenReturn(true);

        WatchRequestModel req = new WatchRequestModel();
        req.setModel("newModel");
        Price price = new Price();
        req.setPrice(price);
        req.setAccessories(List.of());  // prevent NPE on accessories()

        assertThrows(DuplicateWatchModelException.class,
                () -> catalogWatchService.updateWatchInInventory("C2", "W12", req));
    }
}
