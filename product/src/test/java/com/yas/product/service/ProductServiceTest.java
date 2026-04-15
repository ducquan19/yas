package com.yas.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.BadRequestException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.Brand;
import com.yas.product.model.Category;
import com.yas.product.model.Product;
import com.yas.product.model.ProductCategory;
import com.yas.product.model.ProductImage;
import com.yas.product.model.ProductOption;
import com.yas.product.model.ProductOptionCombination;
import com.yas.product.model.ProductRelated;
import com.yas.product.model.enumeration.DimensionUnit;
import com.yas.product.model.enumeration.FilterExistInWhSelection;
import com.yas.product.repository.BrandRepository;
import com.yas.product.repository.CategoryRepository;
import com.yas.product.repository.ProductCategoryRepository;
import com.yas.product.repository.ProductImageRepository;
import com.yas.product.repository.ProductOptionCombinationRepository;
import com.yas.product.repository.ProductOptionRepository;
import com.yas.product.repository.ProductOptionValueRepository;
import com.yas.product.repository.ProductRelatedRepository;
import com.yas.product.repository.ProductRepository;
import com.yas.product.viewmodel.NoFileMediaVm;
import com.yas.product.viewmodel.product.ProductDetailVm;
import com.yas.product.viewmodel.product.ProductEsDetailVm;
import com.yas.product.viewmodel.product.ProductFeatureGetVm;
import com.yas.product.viewmodel.product.ProductInfoVm;
import com.yas.product.viewmodel.product.ProductListGetVm;
import com.yas.product.viewmodel.product.ProductListVm;
import com.yas.product.viewmodel.product.ProductQuantityPostVm;
import com.yas.product.viewmodel.product.ProductQuantityPutVm;
import com.yas.product.viewmodel.product.ProductGetCheckoutListVm;
import com.yas.product.viewmodel.product.ProductGetDetailVm;
import com.yas.product.viewmodel.product.ProductOptionValueDisplay;
import com.yas.product.viewmodel.product.ProductPostVm;
import com.yas.product.viewmodel.product.ProductPutVm;
import com.yas.product.viewmodel.product.ProductSlugGetVm;
import com.yas.product.viewmodel.product.ProductThumbnailGetVm;
import com.yas.product.viewmodel.product.ProductThumbnailVm;
import com.yas.product.viewmodel.product.ProductVariationGetVm;
import com.yas.product.viewmodel.product.ProductVariationPostVm;
import com.yas.product.viewmodel.product.ProductVariationPutVm;
import com.yas.product.viewmodel.product.ProductsGetVm;
import com.yas.product.viewmodel.productoption.ProductOptionValuePostVm;
import com.yas.product.viewmodel.productoption.ProductOptionValuePutVm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 * Unit tests for {@link ProductService}.
 *
 * <p>All repository and service dependencies are mocked with Mockito so that
 * each test exercises only the business logic within {@code ProductService}.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private MediaService mediaService;
    @Mock private BrandRepository brandRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductCategoryRepository productCategoryRepository;
    @Mock private ProductImageRepository productImageRepository;
    @Mock private ProductOptionRepository productOptionRepository;
    @Mock private ProductOptionValueRepository productOptionValueRepository;
    @Mock private ProductOptionCombinationRepository productOptionCombinationRepository;
    @Mock private ProductRelatedRepository productRelatedRepository;

    @InjectMocks
    private ProductService productService;

    // ─── Helper builders ───────────────────────────────────────────────────

    private Product buildProduct(Long id, String name, String slug) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setSlug(slug);
        p.setPublished(true);
        p.setPrice(100.0);
        p.setProductImages(new ArrayList<>());
        p.setProductCategories(new ArrayList<>());
        p.setAttributeValues(new ArrayList<>());
        p.setRelatedProducts(new ArrayList<>());
        p.setProducts(new ArrayList<>());
        return p;
    }

    private NoFileMediaVm noMedia() {
        return new NoFileMediaVm(null, "", "", "", "");
    }

    private ProductPostVm buildProductPostVm(
        List<ProductVariationPostVm> variations,
        List<ProductOptionValuePostVm> optionValues,
        List<ProductOptionValueDisplay> optionValueDisplays
    ) {
        return new ProductPostVm(
            "Main Product",
            "MAIN-SLUG",
            1L,
            new ArrayList<>(List.of(100L)),
            "Short",
            "Description",
            "Specification",
            "SKU-MAIN",
            "GTIN-MAIN",
            2.5,
            DimensionUnit.CM,
            20.0,
            10.0,
            5.0,
            199.99,
            true,
            true,
            false,
            true,
            true,
            "Meta Title",
            "Meta Keyword",
            "Meta Description",
            11L,
            List.of(21L, 22L),
            variations,
            optionValues,
            optionValueDisplays,
            List.of(),
            3L
        );
    }

    private ProductPutVm buildProductPutVm(List<ProductVariationPutVm> variations) {
        return new ProductPutVm(
            "Updated Product",
            "UPDATED-SLUG",
            299.99,
            true,
            true,
            true,
            true,
            true,
            null,
            List.of(),
            "Updated Short",
            "Updated Desc",
            "Updated Spec",
            "SKU-UPD",
            "GTIN-UPD",
            3.0,
            DimensionUnit.CM,
            25.0,
            10.0,
            6.0,
            "Updated Meta",
            "Updated Key",
            "Updated Meta Desc",
            12L,
            List.of(),
            variations,
            List.of(new ProductOptionValuePutVm(501L, "TEXT", 1, List.of("Red"))),
            List.of(new ProductOptionValueDisplay(501L, "TEXT", 1, "Red")),
            List.of(),
            9L
        );
    }

    // ═══════════════════════════════════════════════════════════════════════
    // getLatestProducts
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    class GetLatestProducts {

        @Test
        void whenCountIsZero_thenReturnEmptyList() {
            List<ProductListVm> result = productService.getLatestProducts(0);
            assertThat(result).isEmpty();
            verify(productRepository, never()).getLatestProducts(any());
        }

        @Test
        void whenCountIsNegative_thenReturnEmptyList() {
            List<ProductListVm> result = productService.getLatestProducts(-5);
            assertThat(result).isEmpty();
        }

        @Test
        void whenRepositoryReturnsEmpty_thenReturnEmptyList() {
            when(productRepository.getLatestProducts(any(Pageable.class)))
                .thenReturn(Collections.emptyList());

            List<ProductListVm> result = productService.getLatestProducts(5);
            assertThat(result).isEmpty();
        }

        @Test
        void whenRepositoryReturnsProducts_thenReturnMappedList() {
            Product p = buildProduct(1L, "Phone", "phone");
            when(productRepository.getLatestProducts(any(Pageable.class)))
                .thenReturn(List.of(p));

            List<ProductListVm> result = productService.getLatestProducts(3);
            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("Phone");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // getProductsWithFilter
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    class GetProductsWithFilter {

        @Test
        void whenFilterApplied_thenReturnPagedResult() {
            Product p = buildProduct(1L, "Laptop", "laptop");
            Page<Product> page = new PageImpl<>(List.of(p));
            when(productRepository.getProductsWithFilter(anyString(), anyString(), any(Pageable.class)))
                .thenReturn(page);

            ProductListGetVm result = productService.getProductsWithFilter(0, 10, "Laptop", "");
            assertThat(result.productContent()).hasSize(1);
            assertThat(result.pageNo()).isEqualTo(0);
        }

        @Test
        void whenNoProductMatchesFilter_thenReturnEmptyPage() {
            Page<Product> emptyPage = new PageImpl<>(Collections.emptyList());
            when(productRepository.getProductsWithFilter(anyString(), anyString(), any(Pageable.class)))
                .thenReturn(emptyPage);

            ProductListGetVm result = productService.getProductsWithFilter(0, 10, "X", "Y");
            assertThat(result.productContent()).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // getProductById
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    class GetProductById {

        @Test
        void whenProductExists_thenReturnDetailVm() {
            Product p = buildProduct(1L, "Phone", "phone");
            when(productRepository.findById(1L)).thenReturn(Optional.of(p));

            ProductDetailVm result = productService.getProductById(1L);
            assertThat(result.name()).isEqualTo("Phone");
        }

        @Test
        void whenProductHasThumbnail_thenFetchMediaUrl() {
            Product p = buildProduct(1L, "Phone", "phone");
            p.setThumbnailMediaId(10L);
            when(productRepository.findById(1L)).thenReturn(Optional.of(p));
            when(mediaService.getMedia(10L)).thenReturn(new NoFileMediaVm(10L, "", "", "", "http://img/10"));

            ProductDetailVm result = productService.getProductById(1L);
            assertThat(result.thumbnailMedia().url()).isEqualTo("http://img/10");
        }

        @Test
        void whenProductNotFound_thenThrowNotFoundException() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        void whenProductHasBrand_thenBrandIdIsPopulated() {
            Brand brand = new Brand();
            brand.setId(5L);
            Product p = buildProduct(1L, "Phone", "phone");
            p.setBrand(brand);
            when(productRepository.findById(1L)).thenReturn(Optional.of(p));

            ProductDetailVm result = productService.getProductById(1L);
            assertThat(result.brandId()).isEqualTo(5L);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // deleteProduct
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    class DeleteProduct {

        @Test
        void whenProductIsMainProduct_thenSetPublishedFalseAndSave() {
            Product p = buildProduct(1L, "Phone", "phone");
            when(productRepository.findById(1L)).thenReturn(Optional.of(p));

            productService.deleteProduct(1L);

            assertThat(p.isPublished()).isFalse();
            verify(productRepository).save(p);
        }

        @Test
        void whenProductIsVariant_thenDeleteCombinationsAndSave() {
            Product parent = buildProduct(2L, "Parent", "parent");
            Product variant = buildProduct(1L, "Variant", "variant");
            variant.setParent(parent);

            ProductOptionCombination combo = new ProductOptionCombination();
            when(productRepository.findById(1L)).thenReturn(Optional.of(variant));
            when(productOptionCombinationRepository.findAllByProduct(variant))
                .thenReturn(List.of(combo));

            productService.deleteProduct(1L);

            verify(productOptionCombinationRepository).deleteAll(List.of(combo));
            verify(productRepository).save(variant);
        }

        @Test
        void whenProductNotFound_thenThrowNotFoundException() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.deleteProduct(99L))
                .isInstanceOf(NotFoundException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // getProductsByBrand
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    class GetProductsByBrand {

        @Test
        void whenBrandExists_thenReturnProductThumbnails() {
            Brand brand = new Brand();
            brand.setId(1L);
            brand.setSlug("samsung");
            Product p = buildProduct(1L, "Galaxy", "galaxy");
            p.setThumbnailMediaId(5L);

            when(brandRepository.findBySlug("samsung")).thenReturn(Optional.of(brand));
            when(productRepository.findAllByBrandAndIsPublishedTrueOrderByIdAsc(brand))
                .thenReturn(List.of(p));
            when(mediaService.getMedia(5L)).thenReturn(new NoFileMediaVm(5L, "", "", "", "http://img"));

            List<ProductThumbnailVm> result = productService.getProductsByBrand("samsung");
            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("Galaxy");
        }

        @Test
        void whenBrandNotFound_thenThrowNotFoundException() {
            when(brandRepository.findBySlug("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProductsByBrand("unknown"))
                .isInstanceOf(NotFoundException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // getProductsFromCategory
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    class GetProductsFromCategory {

        @Test
        void whenCategoryExists_thenReturnPagedResult() {
            Category cat = new Category();
            cat.setId(1L);
            cat.setSlug("phones");

            Product p = buildProduct(1L, "Phone", "phone");
            p.setThumbnailMediaId(10L);

            ProductCategory pc = new ProductCategory();
            pc.setProduct(p);
            pc.setCategory(cat);

            Page<ProductCategory> page = new PageImpl<>(List.of(pc));
            when(categoryRepository.findBySlug("phones")).thenReturn(Optional.of(cat));
            when(productCategoryRepository.findAllByCategory(any(Pageable.class), any(Category.class)))
                .thenReturn(page);
            when(mediaService.getMedia(10L)).thenReturn(new NoFileMediaVm(10L, "", "", "", "http://img"));

            var result = productService.getProductsFromCategory(0, 10, "phones");
            assertThat(result.productContent()).hasSize(1);
        }

        @Test
        void whenCategoryNotFound_thenThrowNotFoundException() {
            when(categoryRepository.findBySlug("nope")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProductsFromCategory(0, 10, "nope"))
                .isInstanceOf(NotFoundException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // exportProducts
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    class ExportProducts {

        @Test
        void whenProductsExist_thenReturnExportingDetails() {
            Brand brand = new Brand();
            brand.setId(1L);
            brand.setName("Apple");

            Product p = buildProduct(1L, "iPhone", "iphone");
            p.setBrand(brand);

            when(productRepository.getExportingProducts(anyString(), anyString()))
                .thenReturn(List.of(p));

            var result = productService.exportProducts("iPhone", "Apple");
            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("iPhone");
            assertThat(result.get(0).brandName()).isEqualTo("Apple");
        }

        @Test
        void whenNoProductsMatch_thenReturnEmptyList() {
            when(productRepository.getExportingProducts(anyString(), anyString()))
                .thenReturn(Collections.emptyList());

            var result = productService.exportProducts("X", "Y");
            assertThat(result).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // getProductSlug
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    class GetProductSlug {

        @Test
        void whenProductIsMainProduct_thenReturnOwnSlug() {
            Product p = buildProduct(1L, "Phone", "phone");
            when(productRepository.findById(1L)).thenReturn(Optional.of(p));

            ProductSlugGetVm result = productService.getProductSlug(1L);
            assertThat(result.slug()).isEqualTo("phone");
            assertThat(result.productVariantId()).isNull();
        }

        @Test
        void whenProductIsVariant_thenReturnParentSlug() {
            Product parent = buildProduct(2L, "Parent", "parent-phone");
            Product variant = buildProduct(1L, "Variant", "variant-red");
            variant.setParent(parent);
            when(productRepository.findById(1L)).thenReturn(Optional.of(variant));

            ProductSlugGetVm result = productService.getProductSlug(1L);
            assertThat(result.slug()).isEqualTo("parent-phone");
            assertThat(result.productVariantId()).isEqualTo(1L);
        }

        @Test
        void whenProductNotFound_thenThrowNotFoundException() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProductSlug(99L))
                .isInstanceOf(NotFoundException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // getProductEsDetailById
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    class GetProductEsDetailById {

        @Test
        void whenProductHasBrandAndThumbnail_thenReturnFullDetail() {
            Brand brand = new Brand();
            brand.setName("Samsung");
            Product p = buildProduct(1L, "Galaxy", "galaxy");
            p.setBrand(brand);
            p.setThumbnailMediaId(5L);
            when(productRepository.findById(1L)).thenReturn(Optional.of(p));

            ProductEsDetailVm result = productService.getProductEsDetailById(1L);
            assertThat(result.name()).isEqualTo("Galaxy");
            assertThat(result.brand()).isEqualTo("Samsung");
            assertThat(result.thumbnailMediaId()).isEqualTo(5L);
        }

        @Test
        void whenProductHasNoBrand_thenBrandNameIsNull() {
            Product p = buildProduct(1L, "NoName", "no-name");
            when(productRepository.findById(1L)).thenReturn(Optional.of(p));

            ProductEsDetailVm result = productService.getProductEsDetailById(1L);
            assertThat(result.brand()).isNull();
        }

        @Test
        void whenProductNotFound_thenThrowNotFoundException() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProductEsDetailById(99L))
                .isInstanceOf(NotFoundException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // getRelatedProductsBackoffice
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    class GetRelatedProductsBackoffice {

        @Test
        void whenProductHasRelatedProducts_thenReturnMappedList() {
            Product relProd = buildProduct(2L, "RelatedPhone", "related-phone");
            ProductRelated related = new ProductRelated();
            related.setRelatedProduct(relProd);

            Product p = buildProduct(1L, "Phone", "phone");
            p.setRelatedProducts(List.of(related));

            when(productRepository.findById(1L)).thenReturn(Optional.of(p));

            List<ProductListVm> result = productService.getRelatedProductsBackoffice(1L);
            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("RelatedPhone");
        }

        @Test
        void whenProductNotFound_thenThrowNotFoundException() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getRelatedProductsBackoffice(99L))
                .isInstanceOf(NotFoundException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // getProductByIds / getProductByCategoryIds / getProductByBrandIds
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    class GetProductsByIds {

        @Test
        void getProductByIds_returnsCorrectList() {
            Product p1 = buildProduct(1L, "A", "a");
            Product p2 = buildProduct(2L, "B", "b");
            when(productRepository.findAllByIdIn(List.of(1L, 2L))).thenReturn(List.of(p1, p2));

            List<ProductListVm> result = productService.getProductByIds(List.of(1L, 2L));
            assertThat(result).hasSize(2);
        }

        @Test
        void getProductByCategoryIds_returnsCorrectList() {
            Product p = buildProduct(1L, "Phone", "phone");
            when(productRepository.findByCategoryIdsIn(List.of(10L))).thenReturn(List.of(p));

            List<ProductListVm> result = productService.getProductByCategoryIds(List.of(10L));
            assertThat(result).hasSize(1);
        }

        @Test
        void getProductByBrandIds_returnsCorrectList() {
            Product p = buildProduct(1L, "Phone", "phone");
            when(productRepository.findByBrandIdsIn(List.of(5L))).thenReturn(List.of(p));

            List<ProductListVm> result = productService.getProductByBrandIds(List.of(5L));
            assertThat(result).hasSize(1);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // getProductsForWarehouse
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    class GetProductsForWarehouse {

        @Test
        void whenProductsFound_thenReturnInfoVmList() {
            Product p = buildProduct(1L, "Widget", "widget");
            p.setSku("SKU-001");
            p.setStockQuantity(50L);

            when(productRepository.findProductForWarehouse("widget", "SKU-001", List.of(1L), "ALL"))
                .thenReturn(List.of(p));

            List<ProductInfoVm> result = productService.getProductsForWarehouse(
                "widget", "SKU-001", List.of(1L), FilterExistInWhSelection.ALL);
            assertThat(result).hasSize(1);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // updateProductQuantity
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    class UpdateProductQuantity {

        @Test
        void whenProductsFound_thenUpdateStockQuantityAndSave() {
            Product p = buildProduct(1L, "Phone", "phone");
            p.setStockQuantity(10L);

            when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(p));

            productService.updateProductQuantity(List.of(new ProductQuantityPostVm(1L, 50L)));

            assertThat(p.getStockQuantity()).isEqualTo(50L);
            verify(productRepository).saveAll(anyList());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // subtractStockQuantity
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    class SubtractStockQuantity {

        @Test
        void whenStockTrackingEnabled_thenSubtractQuantity() {
            Product p = buildProduct(1L, "Phone", "phone");
            p.setStockTrackingEnabled(true);
            p.setStockQuantity(100L);

            when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(p));

            productService.subtractStockQuantity(List.of(new ProductQuantityPutVm(1L, 30L)));

            assertThat(p.getStockQuantity()).isEqualTo(70L);
        }

        @Test
        void whenSubtractMoreThanAvailable_thenClampToZero() {
            Product p = buildProduct(1L, "Phone", "phone");
            p.setStockTrackingEnabled(true);
            p.setStockQuantity(10L);

            when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(p));

            productService.subtractStockQuantity(List.of(new ProductQuantityPutVm(1L, 100L)));

            assertThat(p.getStockQuantity()).isEqualTo(0L);
        }

        @Test
        void whenStockTrackingDisabled_thenDoNotChangeQuantity() {
            Product p = buildProduct(1L, "Phone", "phone");
            p.setStockTrackingEnabled(false);
            p.setStockQuantity(50L);

            when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(p));

            productService.subtractStockQuantity(List.of(new ProductQuantityPutVm(1L, 20L)));

            assertThat(p.getStockQuantity()).isEqualTo(50L);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // restoreStockQuantity
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    class RestoreStockQuantity {

        @Test
        void whenStockTrackingEnabled_thenAddQuantityBack() {
            Product p = buildProduct(1L, "Phone", "phone");
            p.setStockTrackingEnabled(true);
            p.setStockQuantity(20L);

            when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(p));

            productService.restoreStockQuantity(List.of(new ProductQuantityPutVm(1L, 15L)));

            assertThat(p.getStockQuantity()).isEqualTo(35L);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // setProductImages
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    class SetProductImages {

        @Test
        void whenImageIdsIsEmpty_thenDeleteExistingAndReturnEmptyList() {
            Product p = buildProduct(1L, "Phone", "phone");

            List<ProductImage> result = productService.setProductImages(Collections.emptyList(), p);

            verify(productImageRepository).deleteByProductId(1L);
            assertThat(result).isEmpty();
        }

        @Test
        void whenProductImagesIsNull_thenCreateNewImages() {
            Product p = buildProduct(1L, "Phone", "phone");
            p.setProductImages(null);

            List<ProductImage> result = productService.setProductImages(List.of(10L, 20L), p);

            assertThat(result).hasSize(2);
        }

        @Test
        void whenNewImagesAdded_thenReturnOnlyNewImages() {
            Product p = buildProduct(1L, "Phone", "phone");
            ProductImage existing = new ProductImage();
            existing.setImageId(10L);
            p.setProductImages(List.of(existing));

            List<ProductImage> result = productService.setProductImages(List.of(10L, 20L), p);

            // 20L is new, 10L already exists → only 20L returned
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getImageId()).isEqualTo(20L);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // getProductsByMultiQuery
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    class GetProductsByMultiQuery {

        @Test
        void whenProductsFound_thenReturnPagedThumbnails() {
            Product p = buildProduct(1L, "Phone", "phone");
            p.setThumbnailMediaId(3L);
            Page<Product> page = new PageImpl<>(List.of(p));

            when(productRepository.findByProductNameAndCategorySlugAndPriceBetween(
                anyString(), anyString(), any(), any(), any(Pageable.class))).thenReturn(page);
            when(mediaService.getMedia(3L)).thenReturn(new NoFileMediaVm(3L, "", "", "", "http://img"));

            ProductsGetVm result = productService.getProductsByMultiQuery(0, 10, "Phone", "phones", 50.0, 200.0);
            assertThat(result.productContent()).hasSize(1);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // getListFeaturedProducts
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    class GetListFeaturedProducts {

        @Test
        void whenFeaturedProductsExist_thenReturnFeatureVm() {
            Product p = buildProduct(1L, "Featured", "featured");
            p.setThumbnailMediaId(7L);
            Page<Product> page = new PageImpl<>(List.of(p));

            when(productRepository.getFeaturedProduct(any(Pageable.class))).thenReturn(page);
            when(mediaService.getMedia(7L)).thenReturn(new NoFileMediaVm(7L, "", "", "", "http://img"));

            ProductFeatureGetVm result = productService.getListFeaturedProducts(0, 10);
            assertThat(result.productList()).hasSize(1);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // getFeaturedProductsById
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    class GetFeaturedProductsById {

        @Test
        void whenProductHasThumbnailUrl_thenReturnThumbnailVm() {
            Product p = buildProduct(1L, "Phone", "phone");
            p.setThumbnailMediaId(5L);

            when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(p));
            when(mediaService.getMedia(5L)).thenReturn(new NoFileMediaVm(5L, "", "", "", "http://img.png"));

            List<ProductThumbnailGetVm> result = productService.getFeaturedProductsById(List.of(1L));
            assertThat(result).hasSize(1);
            assertThat(result.get(0).thumbnailUrl()).isEqualTo("http://img.png");
        }

        @Test
        void whenProductHasNoThumbnailAndHasParent_thenUsesParentThumbnail() {
            Product parent = buildProduct(2L, "Parent", "parent");
            parent.setThumbnailMediaId(9L);

            Product variant = buildProduct(1L, "Variant", "variant");
            variant.setParent(parent);
            variant.setThumbnailMediaId(null);

            when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(variant));
            when(mediaService.getMedia(null)).thenReturn(noMedia()); // getMedia(null) = no url
            when(productRepository.findById(2L)).thenReturn(Optional.of(parent));
            when(mediaService.getMedia(9L)).thenReturn(new NoFileMediaVm(9L, "", "", "", "http://parent.png"));

            List<ProductThumbnailGetVm> result = productService.getFeaturedProductsById(List.of(1L));
            assertThat(result.get(0).thumbnailUrl()).isEqualTo("http://parent.png");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // getProductVariationsByParentId
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    class GetProductVariationsByParentId {

        @Test
        void whenProductHasNoOptions_thenReturnEmptyList() {
            Product parent = buildProduct(1L, "Parent", "parent");
            parent.setHasOptions(false);
            when(productRepository.findById(1L)).thenReturn(Optional.of(parent));

            List<ProductVariationGetVm> result = productService.getProductVariationsByParentId(1L);
            assertThat(result).isEmpty();
        }

        @Test
        void whenProductNotFound_thenThrowNotFoundException() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProductVariationsByParentId(99L))
                .isInstanceOf(NotFoundException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // getRelatedProductsStorefront
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    class GetRelatedProductsStorefront {

        @Test
        void whenRelatedProductsExist_thenReturnPublishedOnly() {
            Product mainProduct = buildProduct(1L, "Main", "main");
            when(productRepository.findById(1L)).thenReturn(Optional.of(mainProduct));

            Product published = buildProduct(2L, "Published", "published");
            published.setThumbnailMediaId(3L);

            Product unpublished = buildProduct(3L, "Unpublished", "unpublished");
            unpublished.setPublished(false);

            ProductRelated r1 = new ProductRelated();
            r1.setRelatedProduct(published);
            ProductRelated r2 = new ProductRelated();
            r2.setRelatedProduct(unpublished);

            Page<ProductRelated> page = new PageImpl<>(List.of(r1, r2));
            when(productRelatedRepository.findAllByProduct(any(Product.class), any(Pageable.class)))
                .thenReturn(page);
            when(mediaService.getMedia(3L)).thenReturn(new NoFileMediaVm(3L, "", "", "", "http://pub.png"));

            ProductsGetVm result = productService.getRelatedProductsStorefront(1L, 0, 10);
            // Only the published product passes the filter
            assertThat(result.productContent()).hasSize(1);
            assertThat(result.productContent().get(0).name()).isEqualTo("Published");
        }

        @Test
        void whenProductNotFound_thenThrowNotFoundException() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getRelatedProductsStorefront(99L, 0, 10))
                .isInstanceOf(NotFoundException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // createProduct
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    class CreateProduct {

        @Test
        void whenLengthIsLessThanWidth_thenThrowBadRequestException() {
            ProductPostVm invalidVm = new ProductPostVm(
                "Main Product",
                "MAIN-SLUG",
                1L,
                List.of(100L),
                "Short",
                "Description",
                "Specification",
                "SKU-MAIN",
                "GTIN-MAIN",
                2.5,
                DimensionUnit.CM,
                5.0,
                10.0,
                5.0,
                199.99,
                true,
                true,
                false,
                true,
                true,
                "Meta Title",
                "Meta Keyword",
                "Meta Description",
                11L,
                List.of(21L),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                3L
            );

            assertThatThrownBy(() -> productService.createProduct(invalidVm))
                .isInstanceOf(BadRequestException.class);
        }

        @Test
        void whenNoVariations_thenCreateMainProductAndReturnDetail() {
            ProductPostVm vm = buildProductPostVm(List.of(), List.of(), List.of());
            Brand brand = new Brand();
            brand.setId(1L);
            Category category = new Category();
            category.setId(100L);

            when(productRepository.findBySlugAndIsPublishedTrue(anyString())).thenReturn(Optional.empty());
            when(productRepository.findByGtinAndIsPublishedTrue(anyString())).thenReturn(Optional.empty());
            when(productRepository.findBySkuAndIsPublishedTrue(anyString())).thenReturn(Optional.empty());
            when(productRepository.findAllById(anyList())).thenReturn(List.of());
            when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
            when(categoryRepository.findAllById(List.of(100L))).thenReturn(List.of(category));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
                Product saved = invocation.getArgument(0);
                if (saved.getId() == null) {
                    saved.setId(999L);
                }
                return saved;
            });

            ProductGetDetailVm result = productService.createProduct(vm);

            assertThat(result.id()).isEqualTo(999L);
            assertThat(result.slug()).isEqualTo("main-slug");
            verify(productImageRepository).saveAll(anyList());
            verify(productCategoryRepository).saveAll(anyList());
            verify(productOptionCombinationRepository, never()).saveAll(anyList());
        }

        @Test
        void whenVariationsPresentButNoMatchingOptions_thenThrowBadRequestException() {
            ProductVariationPostVm variation = new ProductVariationPostVm(
                "Variant Red",
                "VARIANT-RED",
                "SKU-RED",
                "GTIN-RED",
                120.0,
                13L,
                List.of(33L),
                Map.of(501L, "Red")
            );
            ProductPostVm vm = buildProductPostVm(
                List.of(variation),
                List.of(new ProductOptionValuePostVm(501L, "TEXT", 1, List.of("Red"))),
                List.of(new ProductOptionValueDisplay(501L, "TEXT", 1, "Red"))
            );
            Brand brand = new Brand();
            brand.setId(1L);
            Category category = new Category();
            category.setId(100L);

            when(productRepository.findBySlugAndIsPublishedTrue(anyString())).thenReturn(Optional.empty());
            when(productRepository.findByGtinAndIsPublishedTrue(anyString())).thenReturn(Optional.empty());
            when(productRepository.findBySkuAndIsPublishedTrue(anyString())).thenReturn(Optional.empty());
            when(productRepository.findAllById(anyList())).thenReturn(List.of());
            when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
            when(categoryRepository.findAllById(List.of(100L))).thenReturn(List.of(category));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
                Product saved = invocation.getArgument(0);
                if (saved.getId() == null) {
                    saved.setId(1000L);
                }
                return saved;
            });
            when(productOptionRepository.findAllByIdIn(List.of(501L))).thenReturn(List.of());

            assertThatThrownBy(() -> productService.createProduct(vm))
                .isInstanceOf(BadRequestException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // updateProduct
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    class UpdateProduct {

        @Test
        void whenOnlyExistingVariantsProvided_thenUpdateAndSkipCombinationCreation() {
            Product mainProduct = buildProduct(1L, "Main", "main");
            Product existingVariant = buildProduct(2L, "Variant", "variant-old");
            mainProduct.setProducts(new ArrayList<>(List.of(existingVariant)));

            ProductVariationPutVm existingVariantVm = new ProductVariationPutVm(
                2L,
                "Variant Updated",
                "VARIANT-UPDATED",
                "SKU-VAR-UPD",
                "GTIN-VAR-UPD",
                150.0,
                99L,
                List.of(41L, 42L),
                Map.of(501L, "Red")
            );
            ProductPutVm putVm = buildProductPutVm(List.of(existingVariantVm));

            ProductOption option = new ProductOption();
            option.setId(501L);

            when(productRepository.findById(1L)).thenReturn(Optional.of(mainProduct));
            when(productRepository.findBySlugAndIsPublishedTrue(anyString())).thenReturn(Optional.empty());
            when(productRepository.findByGtinAndIsPublishedTrue(anyString())).thenReturn(Optional.empty());
            when(productRepository.findBySkuAndIsPublishedTrue(anyString())).thenReturn(Optional.empty());
            when(productRepository.findAllById(anyList())).thenAnswer(invocation -> {
                List<Long> ids = invocation.getArgument(0);
                if (ids.contains(2L)) {
                    return List.of(existingVariant);
                }
                return List.of();
            });
            when(productCategoryRepository.findAllByProductId(1L)).thenReturn(List.of());
            when(productOptionRepository.findAllByIdIn(List.of(501L))).thenReturn(List.of(option));
            when(productOptionValueRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            productService.updateProduct(1L, putVm);

            assertThat(existingVariant.getName()).isEqualTo("Variant Updated");
            assertThat(existingVariant.getSlug()).isEqualTo("variant-updated");
            verify(productOptionCombinationRepository, never()).saveAll(anyList());
            verify(productRepository, never()).save(mainProduct);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // getProductCheckoutList
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    class GetProductCheckoutList {

        @Test
        void whenProductsHaveThumbnail_thenReturnCheckoutVmWithThumbnailUrl() {
            Brand brand = new Brand();
            brand.setId(8L);

            Product p = buildProduct(1L, "Checkout Product", "checkout-product");
            p.setBrand(brand);
            p.setThumbnailMediaId(66L);

            Page<Product> page = new PageImpl<>(List.of(p));
            when(productRepository.findAllPublishedProductsByIds(anyList(), any(Pageable.class))).thenReturn(page);
            when(mediaService.getMedia(66L)).thenReturn(new NoFileMediaVm(66L, "", "", "", "http://thumb.png"));

            ProductGetCheckoutListVm result = productService.getProductCheckoutList(0, 10, List.of(1L));

            assertThat(result.productCheckoutListVms()).hasSize(1);
            assertThat(result.productCheckoutListVms().get(0).thumbnailUrl()).isEqualTo("http://thumb.png");
            assertThat(result.totalElements()).isEqualTo(1);
        }
    }
}
