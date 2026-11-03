-- V8: Tạo bảng brands (thương hiệu)

CREATE TABLE brands (
    id          BIGINT IDENTITY(1,1) NOT NULL,
    name        NVARCHAR(255)   NOT NULL,
    slug        NVARCHAR(255)   NOT NULL,
    logo_url    NVARCHAR(500)   NULL,
    created_at  DATETIME2       NOT NULL DEFAULT GETDATE(),
    updated_at  DATETIME2       NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_brands PRIMARY KEY (id),
    CONSTRAINT UQ_brands_slug UNIQUE (slug)
);
