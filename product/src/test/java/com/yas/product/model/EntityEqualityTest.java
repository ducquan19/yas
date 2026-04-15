package com.yas.product.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EntityEqualityTest {

    @Test
    void brand_equalsAndHashCode_workAsEntityIdBased() {
        Brand a = new Brand();
        a.setId(1L);
        Brand b = new Brand();
        b.setId(1L);
        Brand c = new Brand();
        c.setId(2L);
        Brand noId = new Brand();

        assertThat(a).isEqualTo(a);
        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo(noId);
        assertThat(a).isNotEqualTo("brand");
        assertThat(a.hashCode()).isEqualTo(Brand.class.hashCode());
    }

    @Test
    void category_equalsAndHashCode_workAsEntityIdBased() {
        Category a = new Category();
        a.setId(1L);
        Category b = new Category();
        b.setId(1L);
        Category c = new Category();
        c.setId(2L);
        Category noId = new Category();

        assertThat(a).isEqualTo(a);
        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo(noId);
        assertThat(a).isNotEqualTo("category");
        assertThat(a.hashCode()).isEqualTo(Category.class.hashCode());
    }

    @Test
    void product_equalsAndHashCode_workAsEntityIdBased() {
        Product a = new Product();
        a.setId(1L);
        Product b = new Product();
        b.setId(1L);
        Product c = new Product();
        c.setId(2L);
        Product noId = new Product();

        assertThat(a).isEqualTo(a);
        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo(noId);
        assertThat(a).isNotEqualTo("product");
        assertThat(a.hashCode()).isEqualTo(Product.class.hashCode());
    }

    @Test
    void productOption_equalsAndHashCode_workAsEntityIdBased() {
        ProductOption a = new ProductOption();
        a.setId(1L);
        ProductOption b = new ProductOption();
        b.setId(1L);
        ProductOption c = new ProductOption();
        c.setId(2L);
        ProductOption noId = new ProductOption();

        assertThat(a).isEqualTo(a);
        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo(noId);
        assertThat(a).isNotEqualTo("option");
        assertThat(a.hashCode()).isEqualTo(ProductOption.class.hashCode());
    }

    @Test
    void productOptionCombination_equalsAndHashCode_workAsEntityIdBased() {
        ProductOptionCombination a = new ProductOptionCombination();
        a.setId(1L);
        ProductOptionCombination b = new ProductOptionCombination();
        b.setId(1L);
        ProductOptionCombination c = new ProductOptionCombination();
        c.setId(2L);
        ProductOptionCombination noId = new ProductOptionCombination();

        assertThat(a).isEqualTo(a);
        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo(noId);
        assertThat(a).isNotEqualTo("combination");
        assertThat(a.hashCode()).isEqualTo(ProductOptionCombination.class.hashCode());
    }

    @Test
    void productOptionValue_equalsAndHashCode_workAsEntityIdBased() {
        ProductOptionValue a = new ProductOptionValue();
        a.setId(1L);
        ProductOptionValue b = new ProductOptionValue();
        b.setId(1L);
        ProductOptionValue c = new ProductOptionValue();
        c.setId(2L);
        ProductOptionValue noId = new ProductOptionValue();

        assertThat(a).isEqualTo(a);
        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo(noId);
        assertThat(a).isNotEqualTo("value");
        assertThat(a.hashCode()).isEqualTo(ProductOptionValue.class.hashCode());
    }

    @Test
    void productRelated_equalsAndHashCode_workAsEntityIdBased() {
        ProductRelated a = new ProductRelated();
        a.setId(1L);
        ProductRelated b = new ProductRelated();
        b.setId(1L);
        ProductRelated c = new ProductRelated();
        c.setId(2L);
        ProductRelated noId = new ProductRelated();

        assertThat(a).isEqualTo(a);
        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo(noId);
        assertThat(a).isNotEqualTo("related");
        assertThat(a.hashCode()).isEqualTo(ProductRelated.class.hashCode());
    }
}
