package com.laundry.pos.service;

import com.laundry.pos.model.Product;
import com.laundry.pos.repository.ProductRepository;
import com.laundry.pos.request.ProductRequest;
import com.laundry.pos.response.BulkProductResponse;
import com.laundry.pos.response.ProductResponse;

import jakarta.persistence.EntityManager;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class ProductService {

    private static final long MAX_PDF_SIZE =
            10L * 1024L * 1024L;

    private static final float REFERENCE_PAGE_WIDTH =
            841.89f;

    private static final float SERIAL_END =
            70.0f;

    private static final float NAME_END =
            190.0f;

    private static final float TYPE_END =
            325.0f;

    private static final float DRY_CLEAN_END =
            380.0f;

    private static final float WASH_FOLD_END =
            445.0f;

    private static final float WASH_IRON_END =
            510.0f;

    private static final float PREMIUM_LAUNDRY_END =
            600.0f;

    private static final float SHOE_CLEANING_END =
            685.0f;

    private static final float STEAM_PRESS_END =
            745.0f;

    private static final Pattern NUMBER_PATTERN =
            Pattern.compile(
                    "\\d+(?:\\.\\d+)?"
            );

    private final ProductRepository productRepository;

    private final EntityManager entityManager;


    public ProductService(
            ProductRepository productRepository,
            EntityManager entityManager
    ) {

        this.productRepository =
                productRepository;

        this.entityManager =
                entityManager;
    }


    @Transactional
    public ProductResponse createProduct(
            ProductRequest request
    ) {

        validateRequest(
                request
        );

        String name =
                request.name()
                        .trim();

        if (
                productRepository
                        .existsByNameIgnoreCase(
                                name
                        )
        ) {

            throw new RuntimeException(
                    "Product already exists"
            );
        }

        Product product =
                new Product();

        applyProductDetails(
                product,
                request
        );

        Product savedProduct =
                productRepository.save(
                        product
                );

        return toResponse(
                savedProduct
        );
    }


    @Transactional(readOnly = true)
    public ProductResponse.ProductListResponse
    getAllProducts() {

        List<ProductResponse> products =
                productRepository
                        .findAll()
                        .stream()
                        .map(
                                this::toResponse
                        )
                        .toList();

        if (
                products.isEmpty()
        ) {

            return new ProductResponse
                    .ProductListResponse(
                    "No products available",
                    products
            );
        }

        return new ProductResponse
                .ProductListResponse(
                "Products fetched successfully",
                products
        );
    }


    @Transactional(readOnly = true)
    public ProductResponse getProductById(
            UUID id
    ) {

        Product product =
                productRepository
                        .findById(
                                id
                        )
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Product not found"
                                        )
                        );

        return toResponse(
                product
        );
    }


    @Transactional
    public ProductResponse updateProduct(
            UUID id,
            ProductRequest request
    ) {

        validateRequest(
                request
        );

        Product product =
                productRepository
                        .findById(
                                id
                        )
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Product not found"
                                        )
                        );

        String name =
                request.name()
                        .trim();

        productRepository
                .findByNameIgnoreCase(
                        name
                )
                .ifPresent(
                        existingProduct -> {

                            if (
                                    !existingProduct
                                            .getId()
                                            .equals(
                                                    id
                                            )
                            ) {

                                throw new RuntimeException(
                                        "Product already exists"
                                );
                            }
                        }
                );

        product.setName(
                name
        );

        product.setIcon(
                normalizeIcon(
                        request.icon()
                )
        );

        product.setUnit(
                request.unit()
        );

        product.setActive(
                request.active()
        );

        replaceProductTypes(
                product,
                request.types()
        );

        Product updatedProduct =
                productRepository
                        .saveAndFlush(
                                product
                        );

        return toResponse(
                updatedProduct
        );
    }


    @Transactional
    public BulkProductResponse bulkUploadProductsPdf(
            MultipartFile file
    ) {

        validatePdfFile(
                file
        );

        try (
                PDDocument document =
                        Loader.loadPDF(
                                file.getBytes()
                        )
        ) {

            if (
                    document.getNumberOfPages() == 0
            ) {

                throw new RuntimeException(
                        "PDF does not contain any pages"
                );
            }

            PositionCollector collector =
                    new PositionCollector();

            collector.setSortByPosition(
                    true
            );

            collector.setShouldSeparateByBeads(
                    false
            );

            collector.getText(
                    document
            );

            List<PdfProductRow> rows =
                    extractProductRows(
                            collector.getPagePositions()
                    );

            if (
                    rows.isEmpty()
            ) {

                throw new RuntimeException(
                        "No product rows found. Please upload a supported price list PDF."
                );
            }

            List<ProductRequest> requests =
                    buildProductRequestsFromPdf(
                            rows
                    );

            if (
                    requests.isEmpty()
            ) {

                throw new RuntimeException(
                        "No valid products found in PDF"
                );
            }

            return bulkUpsertProducts(
                    requests
            );

        } catch (
                IOException exception
        ) {

            throw new RuntimeException(
                    "Unable to read PDF file",
                    exception
            );
        }
    }


    @Transactional
    public BulkProductResponse bulkUpsertProducts(
            List<ProductRequest> requests
    ) {

        if (
                requests == null ||
                requests.isEmpty()
        ) {

            throw new RuntimeException(
                    "At least one product is required"
            );
        }

        Set<String> requestProductNames =
                new HashSet<>();

        for (
                ProductRequest request :
                requests
        ) {

            validateRequest(
                    request
            );

            String normalizedName =
                    normalizeKey(
                            request.name()
                    );

            if (
                    !requestProductNames.add(
                            normalizedName
                    )
            ) {

                throw new RuntimeException(
                        "Duplicate product in bulk request: "
                                + request.name()
                );
            }
        }

        int createdCount =
                0;

        int updatedCount =
                0;

        List<ProductResponse> responses =
                new ArrayList<>();

        for (
                ProductRequest request :
                requests
        ) {

            String name =
                    request.name()
                            .trim();

            Product existingProduct =
                    productRepository
                            .findByNameIgnoreCase(
                                    name
                            )
                            .orElse(
                                    null
                            );

            if (
                    existingProduct == null
            ) {

                Product product =
                        new Product();

                applyProductDetails(
                        product,
                        request
                );

                Product savedProduct =
                        productRepository
                                .saveAndFlush(
                                        product
                                );

                responses.add(
                        toResponse(
                                savedProduct
                        )
                );

                createdCount++;

            } else {

                existingProduct.setName(
                        name
                );

                String incomingIcon =
                        normalizeIcon(
                                request.icon()
                        );

                if (
                        incomingIcon != null
                ) {

                    existingProduct.setIcon(
                            incomingIcon
                    );
                }

                existingProduct.setUnit(
                        request.unit()
                );

                existingProduct.setActive(
                        request.active()
                );

                replaceProductTypes(
                        existingProduct,
                        request.types()
                );

                Product updatedProduct =
                        productRepository
                                .saveAndFlush(
                                        existingProduct
                                );

                responses.add(
                        toResponse(
                                updatedProduct
                        )
                );

                updatedCount++;
            }
        }

        return new BulkProductResponse(
                "Bulk product upload completed successfully",
                requests.size(),
                createdCount,
                updatedCount,
                responses
        );
    }


    @Transactional
    public void deleteProduct(
            UUID id
    ) {

        Product product =
                productRepository
                        .findById(
                                id
                        )
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Product not found"
                                        )
                        );

        product.setActive(
                false
        );

        productRepository.save(
                product
        );
    }


    private void validatePdfFile(
            MultipartFile file
    ) {

        if (
                file == null ||
                file.isEmpty()
        ) {

            throw new RuntimeException(
                    "PDF file is required"
            );
        }

        if (
                file.getSize() >
                        MAX_PDF_SIZE
        ) {

            throw new RuntimeException(
                    "PDF file size cannot exceed 10 MB"
            );
        }

        String fileName =
                file.getOriginalFilename();

        if (
                fileName == null ||
                !fileName
                        .toLowerCase(
                                Locale.ROOT
                        )
                        .endsWith(
                                ".pdf"
                        )
        ) {

            throw new RuntimeException(
                    "Only PDF files are allowed"
            );
        }
    }


    private List<PdfProductRow> extractProductRows(
            Map<Integer, List<TextPosition>>
                    pagePositions
    ) {

        List<PdfProductRow> rows =
                new ArrayList<>();

        for (
                Map.Entry<
                        Integer,
                        List<TextPosition>
                        > pageEntry :
                pagePositions.entrySet()
        ) {

            List<TextPosition> positions =
                    pageEntry
                            .getValue()
                            .stream()
                            .filter(
                                    this::isUsableTablePosition
                            )
                            .toList();

            List<RowAnchor> anchors =
                    findRowAnchors(
                            positions
                    );

            if (
                    anchors.isEmpty()
            ) {

                continue;
            }

            for (
                    int index = 0;
                    index < anchors.size();
                    index++
            ) {

                RowAnchor current =
                        anchors.get(
                                index
                        );

                float top =
                        calculateRowTop(
                                anchors,
                                index
                        );

                float bottom =
                        calculateRowBottom(
                                anchors,
                                index
                        );

                List<TextPosition> rowPositions =
                        positions
                                .stream()
                                .filter(
                                        position -> {

                                            float y =
                                                    position
                                                            .getYDirAdj();

                                            return y >= top &&
                                                    y < bottom;
                                        }
                                )
                                .toList();

                PdfProductRow row =
                        buildPdfRow(
                                current.number(),
                                rowPositions
                        );

                if (
                        row != null
                ) {

                    rows.add(
                            row
                    );
                }
            }
        }

        rows.sort(
                Comparator.comparingInt(
                        PdfProductRow::serialNumber
                )
        );

        return rows;
    }


    private boolean isUsableTablePosition(
            TextPosition position
    ) {

        if (
                position == null
        ) {

            return false;
        }

        String unicode =
                position.getUnicode();

        if (
                unicode == null ||
                unicode.isBlank()
        ) {

            return false;
        }

        if (
                position.getYDirAdj() <
                        105.0f
        ) {

            return false;
        }

        return position.getFontSizeInPt()
                <= 14.0f;
    }


    private List<RowAnchor> findRowAnchors(
            List<TextPosition> positions
    ) {

        List<TextPosition> serialPositions =
                positions
                        .stream()
                        .filter(
                                position -> {

                                    float scale =
                                            getScale(
                                                    position
                                            );

                                    float x =
                                            position
                                                    .getXDirAdj();

                                    return x >=
                                            20.0f * scale &&
                                            x <
                                                    SERIAL_END * scale;
                                }
                        )
                        .sorted(
                                Comparator
                                        .comparingDouble(
                                                TextPosition::getYDirAdj
                                        )
                                        .thenComparingDouble(
                                                TextPosition::getXDirAdj
                                        )
                        )
                        .toList();

        List<TextLine> lines =
                groupPositionsByLine(
                        serialPositions,
                        2.0f
                );

        List<RowAnchor> anchors =
                new ArrayList<>();

        for (
                TextLine line :
                lines
        ) {

            String value =
                    joinSingleLine(
                            line.positions()
                    )
                            .replaceAll(
                                    "\\s+",
                                    ""
                            );

            if (
                    !value.matches(
                            "\\d+"
                    )
            ) {

                continue;
            }

            int serialNumber;

            try {

                serialNumber =
                        Integer.parseInt(
                                value
                        );

            } catch (
                    NumberFormatException exception
            ) {

                continue;
            }

            if (
                    serialNumber <= 0 ||
                    serialNumber > 10000
            ) {

                continue;
            }

            anchors.add(
                    new RowAnchor(
                            serialNumber,
                            line.y()
                    )
            );
        }

        anchors.sort(
                Comparator.comparingDouble(
                        RowAnchor::y
                )
        );

        return anchors;
    }


    private float calculateRowTop(
            List<RowAnchor> anchors,
            int index
    ) {

        RowAnchor current =
                anchors.get(
                        index
                );

        if (
                index == 0
        ) {

            if (
                    anchors.size() == 1
            ) {

                return current.y() -
                        12.0f;
            }

            float nextY =
                    anchors.get(
                            index + 1
                    )
                            .y();

            return current.y() -
                    (
                            nextY -
                                    current.y()
                    ) / 2.0f;
        }

        float previousY =
                anchors.get(
                        index - 1
                )
                        .y();

        return (
                previousY +
                        current.y()
        ) / 2.0f;
    }


    private float calculateRowBottom(
            List<RowAnchor> anchors,
            int index
    ) {

        RowAnchor current =
                anchors.get(
                        index
                );

        if (
                index ==
                        anchors.size() - 1
        ) {

            if (
                    anchors.size() == 1
            ) {

                return current.y() +
                        12.0f;
            }

            float previousY =
                    anchors.get(
                            index - 1
                    )
                            .y();

            return current.y() +
                    (
                            current.y() -
                                    previousY
                    ) / 2.0f;
        }

        float nextY =
                anchors.get(
                        index + 1
                )
                        .y();

        return (
                current.y() +
                        nextY
        ) / 2.0f;
    }


    private PdfProductRow buildPdfRow(
            int serialNumber,
            List<TextPosition> positions
    ) {

        List<TextPosition> namePositions =
                new ArrayList<>();

        List<TextPosition> typePositions =
                new ArrayList<>();

        List<TextPosition> dryCleanPositions =
                new ArrayList<>();

        List<TextPosition> washFoldPositions =
                new ArrayList<>();

        List<TextPosition> washIronPositions =
                new ArrayList<>();

        List<TextPosition> premiumLaundryPositions =
                new ArrayList<>();

        List<TextPosition> shoeCleaningPositions =
                new ArrayList<>();

        List<TextPosition> steamPressPositions =
                new ArrayList<>();

        List<TextPosition> starchingPositions =
                new ArrayList<>();

        for (
                TextPosition position :
                positions
        ) {

            float scale =
                    getScale(
                            position
                    );

            float x =
                    position
                            .getXDirAdj();

            if (
                    x <
                            SERIAL_END * scale
            ) {

                continue;
            }

            if (
                    x <
                            NAME_END * scale
            ) {

                namePositions.add(
                        position
                );

            } else if (
                    x <
                            TYPE_END * scale
            ) {

                typePositions.add(
                        position
                );

            } else if (
                    x <
                            DRY_CLEAN_END * scale
            ) {

                dryCleanPositions.add(
                        position
                );

            } else if (
                    x <
                            WASH_FOLD_END * scale
            ) {

                washFoldPositions.add(
                        position
                );

            } else if (
                    x <
                            WASH_IRON_END * scale
            ) {

                washIronPositions.add(
                        position
                );

            } else if (
                    x <
                            PREMIUM_LAUNDRY_END * scale
            ) {

                premiumLaundryPositions.add(
                        position
                );

            } else if (
                    x <
                            SHOE_CLEANING_END * scale
            ) {

                shoeCleaningPositions.add(
                        position
                );

            } else if (
                    x <
                            STEAM_PRESS_END * scale
            ) {

                steamPressPositions.add(
                        position
                );

            } else {

                starchingPositions.add(
                        position
                );
            }
        }

        String productName =
                normalizeCellText(
                        joinCellPositions(
                                namePositions
                        )
                );

        String typeName =
                normalizeCellText(
                        joinCellPositions(
                                typePositions
                        )
                );

        if (
                productName.isBlank()
        ) {

            return null;
        }

        if (
                typeName.isBlank() ||
                "-".equals(
                        typeName
                )
        ) {

            typeName =
                    "Default";
        }

        return new PdfProductRow(
                serialNumber,
                productName,
                typeName,
                parsePrice(
                        dryCleanPositions
                ),
                parsePrice(
                        washFoldPositions
                ),
                parsePrice(
                        washIronPositions
                ),
                parsePrice(
                        premiumLaundryPositions
                ),
                parsePrice(
                        shoeCleaningPositions
                ),
                parsePrice(
                        steamPressPositions
                ),
                parsePrice(
                        starchingPositions
                )
        );
    }


    private BigDecimal parsePrice(
            List<TextPosition> positions
    ) {

        String value =
                joinCellPositions(
                        positions
                );

        Matcher matcher =
                NUMBER_PATTERN
                        .matcher(
                                value
                        );

        if (
                !matcher.find()
        ) {

            return null;
        }

        return new BigDecimal(
                matcher.group()
        );
    }


    private List<ProductRequest> buildProductRequestsFromPdf(
            List<PdfProductRow> rows
    ) {

        Map<String, PdfProductDraft>
                products =
                new LinkedHashMap<>();

        for (
                PdfProductRow row :
                rows
        ) {

            String productKey =
                    normalizeKey(
                            row.productName()
                    );

            Product.PricingUnit unit =
                    determinePricingUnit(
                            row.productName()
                    );

            PdfProductDraft product =
                    products.computeIfAbsent(
                            productKey,
                            ignored ->
                                    new PdfProductDraft(
                                            row.productName(),
                                            unit
                                    )
                    );

            String typeKey =
                    normalizeKey(
                            row.typeName()
                    );

            PdfTypeDraft type =
                    product.types()
                            .computeIfAbsent(
                                    typeKey,
                                    ignored ->
                                            new PdfTypeDraft(
                                                    row.typeName()
                                            )
                            );

            putPdfService(
                    type,
                    "Dry Clean",
                    row.dryClean()
            );

            putPdfService(
                    type,
                    "Wash & Fold",
                    row.washFold()
            );

            putPdfService(
                    type,
                    "Wash & Iron",
                    row.washIron()
            );

            putPdfService(
                    type,
                    "Premium Laundry",
                    row.premiumLaundry()
            );

            putPdfService(
                    type,
                    "Shoe Cleaning",
                    row.shoeCleaning()
            );

            putPdfService(
                    type,
                    "Steam Press",
                    row.steamPress()
            );

            putPdfService(
                    type,
                    "Starching",
                    row.starching()
            );
        }

        List<ProductRequest> requests =
                new ArrayList<>();

        for (
                PdfProductDraft product :
                products.values()
        ) {

            List<ProductRequest.TypeRequest>
                    types =
                    new ArrayList<>();

            for (
                    PdfTypeDraft type :
                    product.types()
                            .values()
            ) {

                List<ProductRequest.ServiceRequest>
                        services =
                        type.services()
                                .entrySet()
                                .stream()
                                .map(
                                        entry ->
                                                new ProductRequest
                                                        .ServiceRequest(
                                                        entry.getKey(),
                                                        entry.getValue()
                                                )
                                )
                                .toList();

                if (
                        services.isEmpty()
                ) {

                    continue;
                }

                types.add(
                        new ProductRequest
                                .TypeRequest(
                                type.name(),
                                services
                        )
                );
            }

            if (
                    types.isEmpty()
            ) {

                continue;
            }

            requests.add(
                    new ProductRequest(
                            product.name(),
                            null,
                            product.unit(),
                            true,
                            types
                    )
            );
        }

        return requests;
    }


    private void putPdfService(
            PdfTypeDraft type,
            String serviceName,
            BigDecimal price
    ) {

        if (
                price == null
        ) {

            return;
        }

        type.services()
                .put(
                        serviceName,
                        price
                );
    }


    private Product.PricingUnit determinePricingUnit(
            String productName
    ) {

        String normalized =
                normalizeKey(
                        productName
                );

        if (
                normalized.contains(
                        "laundry by weight"
                ) ||
                normalized.contains(
                        "by weight"
                )
        ) {

            return Product.PricingUnit.KG;
        }

        return Product.PricingUnit.PC;
    }


    private void replaceProductTypes(
            Product product,
            List<ProductRequest.TypeRequest>
                    typeRequests
    ) {

        product.getTypes()
                .clear();

        productRepository
                .saveAndFlush(
                        product
                );

        entityManager.flush();

        List<Product.ProductType> newTypes =
                buildTypes(
                        typeRequests
                );

        for (
                Product.ProductType type :
                newTypes
        ) {

            product.addType(
                    type
            );
        }
    }


    private void applyProductDetails(
            Product product,
            ProductRequest request
    ) {

        product.setName(
                request.name()
                        .trim()
        );

        product.setIcon(
                normalizeIcon(
                        request.icon()
                )
        );

        product.setUnit(
                request.unit()
        );

        product.setActive(
                request.active()
        );

        List<Product.ProductType> types =
                buildTypes(
                        request.types()
                );

        product.setTypes(
                types
        );
    }


    private List<Product.ProductType>
    buildTypes(
            List<ProductRequest.TypeRequest>
                    typeRequests
    ) {

        List<Product.ProductType> types =
                new ArrayList<>();

        Set<String> typeNames =
                new HashSet<>();

        for (
                ProductRequest.TypeRequest typeRequest :
                typeRequests
        ) {

            String typeName =
                    typeRequest
                            .name()
                            .trim();

            String normalizedTypeName =
                    normalizeKey(
                            typeName
                    );

            if (
                    !typeNames.add(
                            normalizedTypeName
                    )
            ) {

                throw new RuntimeException(
                        "Duplicate product type: "
                                + typeName
                );
            }

            Product.ProductType type =
                    new Product.ProductType();

            type.setName(
                    typeName
            );

            List<Product.ProductServicePrice> services =
                    buildServices(
                            typeRequest.services()
                    );

            type.setServices(
                    services
            );

            types.add(
                    type
            );
        }

        return types;
    }


    private List<Product.ProductServicePrice>
    buildServices(
            List<ProductRequest.ServiceRequest>
                    serviceRequests
    ) {

        List<Product.ProductServicePrice> services =
                new ArrayList<>();

        Set<String> serviceNames =
                new HashSet<>();

        for (
                ProductRequest.ServiceRequest serviceRequest :
                serviceRequests
        ) {

            String serviceName =
                    serviceRequest
                            .name()
                            .trim();

            String normalizedServiceName =
                    normalizeKey(
                            serviceName
                    );

            if (
                    !serviceNames.add(
                            normalizedServiceName
                    )
            ) {

                throw new RuntimeException(
                        "Duplicate service: "
                                + serviceName
                );
            }

            Product.ProductServicePrice service =
                    new Product.ProductServicePrice();

            service.setName(
                    serviceName
            );

            service.setPrice(
                    serviceRequest.price()
            );

            services.add(
                    service
            );
        }

        return services;
    }


    private void validateRequest(
            ProductRequest request
    ) {

        if (
                request == null
        ) {

            throw new RuntimeException(
                    "Product request is required"
            );
        }

        if (
                request.name() == null ||
                request.name().isBlank()
        ) {

            throw new RuntimeException(
                    "Product name is required"
            );
        }

        if (
                request.unit() == null
        ) {

            throw new RuntimeException(
                    "Pricing unit is required"
            );
        }

        if (
                request.types() == null ||
                request.types().isEmpty()
        ) {

            throw new RuntimeException(
                    "At least one product type is required"
            );
        }

        for (
                ProductRequest.TypeRequest type :
                request.types()
        ) {

            if (
                    type.name() == null ||
                    type.name().isBlank()
            ) {

                throw new RuntimeException(
                        "Product type name is required"
                );
            }

            if (
                    type.services() == null ||
                    type.services().isEmpty()
            ) {

                throw new RuntimeException(
                        "At least one service is required for product type "
                                + type.name()
                );
            }

            for (
                    ProductRequest.ServiceRequest service :
                    type.services()
            ) {

                if (
                        service.name() == null ||
                        service.name().isBlank()
                ) {

                    throw new RuntimeException(
                            "Service name is required"
                    );
                }

                if (
                        service.price() == null
                ) {

                    throw new RuntimeException(
                            "Service price is required"
                    );
                }

                if (
                        service.price()
                                .compareTo(
                                        BigDecimal.ZERO
                                ) < 0
                ) {

                    throw new RuntimeException(
                            "Service price cannot be negative"
                    );
                }
            }
        }
    }


    private String normalizeIcon(
            String icon
    ) {

        if (
                icon == null ||
                icon.isBlank()
        ) {

            return null;
        }

        return icon.trim();
    }


    private String normalizeCellText(
            String value
    ) {

        if (
                value == null
        ) {

            return "";
        }

        return value
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim();
    }


    private String normalizeKey(
            String value
    ) {

        return normalizeCellText(
                value
        )
                .toLowerCase(
                        Locale.ROOT
                );
    }


    private float getScale(
            TextPosition position
    ) {

        float pageWidth =
                position.getPageWidth();

        if (
                pageWidth <= 0
        ) {

            return 1.0f;
        }

        return pageWidth /
                REFERENCE_PAGE_WIDTH;
    }


    private List<TextLine> groupPositionsByLine(
            List<TextPosition> positions,
            float tolerance
    ) {

        List<TextPosition> sorted =
                positions
                        .stream()
                        .sorted(
                                Comparator
                                        .comparingDouble(
                                                TextPosition::getYDirAdj
                                        )
                                        .thenComparingDouble(
                                                TextPosition::getXDirAdj
                                        )
                        )
                        .toList();

        List<TextLine> lines =
                new ArrayList<>();

        for (
                TextPosition position :
                sorted
        ) {

            TextLine matchingLine =
                    null;

            for (
                    int index =
                            lines.size() - 1;
                    index >= 0;
                    index--
            ) {

                TextLine line =
                        lines.get(
                                index
                        );

                if (
                        Math.abs(
                                line.y() -
                                        position
                                                .getYDirAdj()
                        ) <= tolerance
                ) {

                    matchingLine =
                            line;

                    break;
                }

                if (
                        line.y() <
                                position.getYDirAdj() -
                                        tolerance
                ) {

                    break;
                }
            }

            if (
                    matchingLine == null
            ) {

                List<TextPosition> linePositions =
                        new ArrayList<>();

                linePositions.add(
                        position
                );

                lines.add(
                        new TextLine(
                                position.getYDirAdj(),
                                linePositions
                        )
                );

            } else {

                matchingLine
                        .positions()
                        .add(
                                position
                        );
            }
        }

        return lines;
    }


    private String joinCellPositions(
            List<TextPosition> positions
    ) {

        if (
                positions == null ||
                positions.isEmpty()
        ) {

            return "";
        }

        List<TextLine> lines =
                groupPositionsByLine(
                        positions,
                        2.2f
                );

        StringBuilder result =
                new StringBuilder();

        for (
                TextLine line :
                lines
        ) {

            String lineText =
                    joinSingleLine(
                            line.positions()
                    );

            if (
                    lineText.isBlank()
            ) {

                continue;
            }

            if (
                    !result.isEmpty()
            ) {

                result.append(
                        ' '
                );
            }

            result.append(
                    lineText
            );
        }

        return normalizeCellText(
                result.toString()
        );
    }


    private String joinSingleLine(
            List<TextPosition> positions
    ) {

        List<TextPosition> sorted =
                positions
                        .stream()
                        .sorted(
                                Comparator.comparingDouble(
                                        TextPosition::getXDirAdj
                                )
                        )
                        .toList();

        StringBuilder result =
                new StringBuilder();

        TextPosition previous =
                null;

        for (
                TextPosition current :
                sorted
        ) {

            String unicode =
                    current.getUnicode();

            if (
                    unicode == null ||
                    unicode.isEmpty()
            ) {

                continue;
            }

            if (
                    previous != null
            ) {

                float previousEnd =
                        previous.getXDirAdj() +
                                previous.getWidthDirAdj();

                float gap =
                        current.getXDirAdj() -
                                previousEnd;

                float spacingThreshold =
                        Math.max(
                                1.5f,
                                previous.getWidthDirAdj() *
                                        0.35f
                        );

                if (
                        gap >
                                spacingThreshold &&
                        !Character.isWhitespace(
                                result.charAt(
                                        result.length() - 1
                                )
                        )
                ) {

                    result.append(
                            ' '
                    );
                }
            }

            result.append(
                    unicode
            );

            previous =
                    current;
        }

        return result
                .toString()
                .trim();
    }


    private ProductResponse toResponse(
            Product product
    ) {

        List<ProductResponse.TypeResponse> types =
                product.getTypes()
                        .stream()
                        .map(
                                type -> {

                                    List<ProductResponse.ServiceResponse>
                                            services =
                                            type.getServices()
                                                    .stream()
                                                    .map(
                                                            service ->
                                                                    new ProductResponse
                                                                            .ServiceResponse(
                                                                            service.getId(),
                                                                            service.getName(),
                                                                            service.getPrice()
                                                                    )
                                                    )
                                                    .toList();

                                    return new ProductResponse
                                            .TypeResponse(
                                            type.getId(),
                                            type.getName(),
                                            services
                                    );
                                }
                        )
                        .toList();

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getIcon(),
                product.getUnit(),
                product.isActive(),
                types
        );
    }


    private record RowAnchor(
            int number,
            float y
    ) {
    }


    private record TextLine(
            float y,
            List<TextPosition> positions
    ) {
    }


    private record PdfProductRow(
            int serialNumber,
            String productName,
            String typeName,
            BigDecimal dryClean,
            BigDecimal washFold,
            BigDecimal washIron,
            BigDecimal premiumLaundry,
            BigDecimal shoeCleaning,
            BigDecimal steamPress,
            BigDecimal starching
    ) {
    }


    private record PdfProductDraft(
            String name,
            Product.PricingUnit unit,
            Map<String, PdfTypeDraft> types
    ) {

        private PdfProductDraft(
                String name,
                Product.PricingUnit unit
        ) {

            this(
                    name,
                    unit,
                    new LinkedHashMap<>()
            );
        }
    }


    private record PdfTypeDraft(
            String name,
            Map<String, BigDecimal> services
    ) {

        private PdfTypeDraft(
                String name
        ) {

            this(
                    name,
                    new LinkedHashMap<>()
            );
        }
    }


    private static class PositionCollector
            extends PDFTextStripper {

        private final Map<
                Integer,
                List<TextPosition>
                > pagePositions =
                new LinkedHashMap<>();


        @Override
        protected void writeString(
                String text,
                List<TextPosition> textPositions
        ) throws IOException {

            pagePositions
                    .computeIfAbsent(
                            getCurrentPageNo(),
                            ignored ->
                                    new ArrayList<>()
                    )
                    .addAll(
                            textPositions
                    );

            super.writeString(
                    text,
                    textPositions
            );
        }


        public Map<
                Integer,
                List<TextPosition>
                > getPagePositions() {

            return pagePositions;
        }
    }
}