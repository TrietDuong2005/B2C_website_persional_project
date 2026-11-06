package com.dahdt.ecommerce.modules.cart.mapper;

import com.dahdt.ecommerce.modules.cart.dto.CartItemResponseDTO;
import com.dahdt.ecommerce.modules.cart.dto.CartResponseDTO;
import com.dahdt.ecommerce.modules.cart.entity.Cart;
import com.dahdt.ecommerce.modules.cart.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct Mapper chuyển đổi giữa Cart / CartItem Entity và DTO.
 */
@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "variant.id", target = "variantId")
    @Mapping(source = "variant.product.name", target = "productName")
    @Mapping(source = "variant.product.slug", target = "productSlug")
    @Mapping(source = "variant.sku", target = "sku")
    @Mapping(source = "variant.color", target = "color")
    @Mapping(source = "variant.size", target = "size")
    @Mapping(source = "variant.price", target = "unitPrice")
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "lineTotal", ignore = true)
    @Mapping(target = "availableStock", ignore = true)
    @Mapping(target = "outOfStock", ignore = true)
    @Mapping(target = "stockExceeded", ignore = true)
    @Mapping(target = "unavailable", ignore = true)
    @Mapping(target = "warningMessage", ignore = true)
    CartItemResponseDTO toItemDto(CartItem item);

    @Mapping(target = "items", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "totalQuantity", ignore = true)
    @Mapping(target = "isEmpty", ignore = true)
    @Mapping(target = "hasProblem", ignore = true)
    CartResponseDTO toCartDto(Cart cart);
}

