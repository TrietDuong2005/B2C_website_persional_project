-- V12: Tạo bảng variant_attribute_values (bảng trung gian)
-- Gắn 1 biến thể với tổ hợp các giá trị thuộc tính (Ví dụ: SKU-01 = Đỏ + XL)

CREATE TABLE variant_attribute_values (
    variant_id          BIGINT NOT NULL,
    attribute_value_id  BIGINT NOT NULL,

    CONSTRAINT PK_variant_attribute_values PRIMARY KEY (variant_id, attribute_value_id),
    CONSTRAINT FK_vav_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id),
    CONSTRAINT FK_vav_attribute_value FOREIGN KEY (attribute_value_id) REFERENCES attribute_values(id)
);
