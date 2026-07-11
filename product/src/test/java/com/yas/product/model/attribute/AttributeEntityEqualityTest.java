package com.yas.product.model.attribute;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AttributeEntityEqualityTest {

    @Test
    void productAttribute_equalsAndHashCode_workAsEntityIdBased() {
        ProductAttribute a = new ProductAttribute();
        a.setId(1L);
        ProductAttribute b = new ProductAttribute();
        b.setId(1L);
        ProductAttribute c = new ProductAttribute();
        c.setId(2L);
        ProductAttribute noId = new ProductAttribute();

        assertThat(a).isEqualTo(a);
        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo(noId);
        assertThat(a).isNotEqualTo("attribute");
        assertThat(a.hashCode()).isEqualTo(ProductAttribute.class.hashCode());
    }

    @Test
    void productAttributeGroup_equalsAndHashCode_workAsEntityIdBased() {
        ProductAttributeGroup a = new ProductAttributeGroup();
        a.setId(1L);
        ProductAttributeGroup b = new ProductAttributeGroup();
        b.setId(1L);
        ProductAttributeGroup c = new ProductAttributeGroup();
        c.setId(2L);
        ProductAttributeGroup noId = new ProductAttributeGroup();

        assertThat(a).isEqualTo(a);
        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo(noId);
        assertThat(a).isNotEqualTo("group");
        assertThat(a.hashCode()).isEqualTo(ProductAttributeGroup.class.hashCode());
    }

    @Test
    void productTemplate_equalsAndHashCode_workAsEntityIdBased() {
        ProductTemplate a = new ProductTemplate();
        a.setId(1L);
        ProductTemplate b = new ProductTemplate();
        b.setId(1L);
        ProductTemplate c = new ProductTemplate();
        c.setId(2L);
        ProductTemplate noId = new ProductTemplate();

        assertThat(a).isEqualTo(a);
        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo(noId);
        assertThat(a).isNotEqualTo("template");
        assertThat(a.hashCode()).isEqualTo(ProductTemplate.class.hashCode());
    }

    @Test
    void productAttributeValue_settersAndGetters_work() {
        ProductAttributeValue value = new ProductAttributeValue();
        ProductAttribute attribute = new ProductAttribute();
        attribute.setId(5L);

        value.setId(10L);
        value.setProductAttribute(attribute);
        value.setValue("Black");

        assertThat(value.getId()).isEqualTo(10L);
        assertThat(value.getProductAttribute()).isEqualTo(attribute);
        assertThat(value.getValue()).isEqualTo("Black");
    }
}
