package com.yas.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.Brand;
import com.yas.product.model.Category;
import com.yas.product.model.Product;
import com.yas.product.model.ProductCategory;
import com.yas.product.model.ProductImage;
import com.yas.product.model.ProductOption;
import com.yas.product.model.ProductOptionCombination;
import com.yas.product.model.attribute.ProductAttribute;
import com.yas.product.model.attribute.ProductAttributeValue;
import com.yas.product.repository.ProductOptionCombinationRepository;
import com.yas.product.repository.ProductRepository;
import com.yas.product.viewmodel.NoFileMediaVm;
import com.yas.product.viewmodel.product.ProductDetailInfoVm;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductDetailServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private MediaService mediaService;
    @Mock
    private ProductOptionCombinationRepository productOptionCombinationRepository;

    @InjectMocks
    private ProductDetailService productDetailService;

    private Product product(long id, String name, String slug, boolean published) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setSlug(slug);
        p.setPublished(published);
        p.setAllowedToOrder(true);
        p.setFeatured(false);
        p.setVisibleIndividually(true);
        p.setStockTrackingEnabled(true);
        p.setPrice(99.0);
        p.setSku("SKU-" + id);
        p.setGtin("GTIN-" + id);
        p.setAttributeValues(new ArrayList<>());
        p.setProducts(new ArrayList<>());
        p.setProductImages(new ArrayList<>());
        p.setProductCategories(new ArrayList<>());
        return p;
    }

    @Test
    void getProductDetailById_whenProductMissing_thenThrowNotFoundException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productDetailService.getProductDetailById(999L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getProductDetailById_whenProductUnpublished_thenThrowNotFoundException() {
        Product unpublished = product(1L, "Phone", "phone", false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(unpublished));

        assertThatThrownBy(() -> productDetailService.getProductDetailById(1L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getProductDetailById_whenHasOptions_thenMapPublishedVariationAndOptions() {
        Product main = product(1L, "Main", "main", true);
        main.setHasOptions(true);
        main.setThumbnailMediaId(11L);
        main.setTaxClassId(2L);

        Brand brand = new Brand();
        brand.setId(10L);
        brand.setName("Acme");
        main.setBrand(brand);

        Category category = new Category();
        category.setId(100L);
        category.setName("Phones");
        ProductCategory productCategory = new ProductCategory();
        productCategory.setCategory(category);
        productCategory.setProduct(main);
        main.setProductCategories(List.of(productCategory));

        ProductAttribute attribute = new ProductAttribute();
        attribute.setId(500L);
        attribute.setName("Color");
        ProductAttributeValue attributeValue = new ProductAttributeValue();
        attributeValue.setId(700L);
        attributeValue.setProduct(main);
        attributeValue.setProductAttribute(attribute);
        attributeValue.setValue("Black");
        main.setAttributeValues(List.of(attributeValue));

        ProductImage mainImage = new ProductImage();
        mainImage.setImageId(12L);
        mainImage.setProduct(main);
        main.setProductImages(List.of(mainImage));

        Product publishedVariation = product(2L, "Main Red", "main-red", true);
        publishedVariation.setThumbnailMediaId(21L);
        ProductImage variationImage = new ProductImage();
        variationImage.setImageId(22L);
        variationImage.setProduct(publishedVariation);
        publishedVariation.setProductImages(List.of(variationImage));

        Product unpublishedVariation = product(3L, "Main Hidden", "main-hidden", false);
        main.setProducts(List.of(publishedVariation, unpublishedVariation));

        ProductOption option = new ProductOption();
        option.setId(501L);
        ProductOptionCombination combination = new ProductOptionCombination();
        combination.setProduct(publishedVariation);
        combination.setProductOption(option);
        combination.setValue("Red");

        when(productRepository.findById(1L)).thenReturn(Optional.of(main));
        when(productOptionCombinationRepository.findAllByProduct(publishedVariation)).thenReturn(List.of(combination));
        when(mediaService.getMedia(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return new NoFileMediaVm(id, "", "", "", "http://img/" + id);
        });

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBrandId()).isEqualTo(10L);
        assertThat(result.getBrandName()).isEqualTo("Acme");
        assertThat(result.getCategories()).hasSize(1);
        assertThat(result.getAttributeValues()).hasSize(1);
        assertThat(result.getThumbnail().url()).isEqualTo("http://img/11");
        assertThat(result.getProductImages()).hasSize(1);
        assertThat(result.getVariations()).hasSize(1);
        assertThat(result.getVariations().get(0).options()).containsEntry(501L, "Red");
    }

    @Test
    void getProductDetailById_whenNoOptionsAndNoMedia_thenReturnEmptyCollectionsAndNoVariationQuery() {
        Product main = product(5L, "Simple", "simple", true);
        main.setHasOptions(false);
        main.setBrand(null);
        main.setThumbnailMediaId(null);
        main.setProductCategories(null);
        main.setProductImages(null);

        when(productRepository.findById(5L)).thenReturn(Optional.of(main));

        ProductDetailInfoVm result = productDetailService.getProductDetailById(5L);

        assertThat(result.getBrandId()).isNull();
        assertThat(result.getBrandName()).isNull();
        assertThat(result.getCategories()).isEmpty();
        assertThat(result.getThumbnail()).isNull();
        assertThat(result.getProductImages()).isEmpty();
        assertThat(result.getVariations()).isEmpty();
        verify(productOptionCombinationRepository, never()).findAllByProduct(org.mockito.ArgumentMatchers.any());
    }
}
