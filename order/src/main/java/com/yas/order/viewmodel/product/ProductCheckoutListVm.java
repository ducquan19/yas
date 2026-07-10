package com.yas.order.viewmodel.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCheckoutListVm {
    Long id;
    String name;
    Double price;
    Long taxClassId;
}