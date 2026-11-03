-- V10: Tạo bảng attributes (định nghĩa thuộc tính) và attribute_values (giá trị thuộc tính)
-- Ví dụ: Attribute (Màu sắc) -> Attribute_Value (Đỏ, Xanh)

CREATE TABLE attributes (
    id          BIGINT IDENTITY(1,1) NOT NULL,
    name        NVARCHAR(255)   NOT NULL, -- Ví dụ: Màu sắc, Kích thước, RAM
    input_type  NVARCHAR(50)    NOT NULL DEFAULT 'select', -- select/color/text
    created_at  DATETIME2       NOT NULL DEFAULT GETDATE(),
    updated_at  DATETIME2       NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_attributes PRIMARY KEY (id)
);

CREATE TABLE attribute_values (
    id             BIGINT IDENTITY(1,1) NOT NULL,
    attribute_id   BIGINT          NOT NULL,
    value          NVARCHAR(255)   NOT NULL, -- Ví dụ: Đỏ, Xanh, XXL
    extra_data     NVARCHAR(500)   NULL,     -- Ví dụ: mã hex #FF0000 nếu là màu
    created_at     DATETIME2       NOT NULL DEFAULT GETDATE(),
    updated_at     DATETIME2       NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_attribute_values PRIMARY KEY (id),
    CONSTRAINT FK_attribute_values_attribute FOREIGN KEY (attribute_id) REFERENCES attributes(id)
);

CREATE INDEX IX_attribute_values_attribute_id ON attribute_values(attribute_id);
