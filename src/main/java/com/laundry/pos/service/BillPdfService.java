package com.laundry.pos.service;

import com.laundry.pos.model.Order;
import com.laundry.pos.repository.OrderRepository;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class BillPdfService {

    private final OrderRepository orderRepository;

    private final PDType1Font regularFont =
            new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA
            );

    private final PDType1Font boldFont =
            new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA_BOLD
            );

    public BillPdfService(
            OrderRepository orderRepository
    ) {
        this.orderRepository =
                orderRepository;
    }

    @Transactional(readOnly = true)
    public byte[] generateReceipt(
            UUID orderId
    ) {

        Order order =
                orderRepository
                        .findById(orderId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Order not found"
                                )
                        );

        try (
                PDDocument document =
                        new PDDocument();

                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {

            PDPage page =
                    new PDPage(
                            PDRectangle.A4
                    );

            document.addPage(
                    page
            );

            try (
                    PDPageContentStream content =
                            new PDPageContentStream(
                                    document,
                                    page
                            )
            ) {

                float pageWidth =
                        page.getMediaBox()
                                .getWidth();

                float left =
                        45;

                float right =
                        pageWidth - 45;

                float y =
                        790;

                drawCenteredText(
                        content,
                        "VENKATESHWARA FABRIC WORKS",
                        boldFont,
                        18,
                        pageWidth,
                        y
                );

                y -= 22;

                drawCenteredText(
                        content,
                        "Laundry & Fabric Care Receipt",
                        regularFont,
                        10,
                        pageWidth,
                        y
                );

                y -= 25;

                drawLine(
                        content,
                        left,
                        y,
                        right,
                        y
                );

                y -= 25;

                drawText(
                        content,
                        "Invoice No:",
                        boldFont,
                        10,
                        left,
                        y
                );

                drawText(
                        content,
                        buildInvoiceNumber(
                                order.getOrderNumber()
                        ),
                        regularFont,
                        10,
                        left + 85,
                        y
                );

                drawText(
                        content,
                        "Order No:",
                        boldFont,
                        10,
                        330,
                        y
                );

                drawText(
                        content,
                        safe(
                                order.getOrderNumber()
                        ),
                        regularFont,
                        10,
                        395,
                        y
                );

                y -= 20;

                drawText(
                        content,
                        "Customer:",
                        boldFont,
                        10,
                        left,
                        y
                );

                drawText(
                        content,
                        safe(
                                order.getCustomer()
                                        .getName()
                        ),
                        regularFont,
                        10,
                        left + 85,
                        y
                );

                drawText(
                        content,
                        "Mobile:",
                        boldFont,
                        10,
                        330,
                        y
                );

                drawText(
                        content,
                        safe(
                                order.getCustomer()
                                        .getPhone()
                        ),
                        regularFont,
                        10,
                        395,
                        y
                );

                y -= 20;

                drawText(
                        content,
                        "Created:",
                        boldFont,
                        10,
                        left,
                        y
                );

                drawText(
                        content,
                        order.getCreatedAt() != null
                                ? order.getCreatedAt()
                                .format(
                                        DateTimeFormatter.ofPattern(
                                                "dd-MM-yyyy hh:mm a"
                                        )
                                )
                                : "-",
                        regularFont,
                        10,
                        left + 85,
                        y
                );

                y -= 30;

                drawLine(
                        content,
                        left,
                        y,
                        right,
                        y
                );

                y -= 20;

                drawText(
                        content,
                        "Item",
                        boldFont,
                        10,
                        left,
                        y
                );

                drawText(
                        content,
                        "Service",
                        boldFont,
                        10,
                        220,
                        y
                );

                drawText(
                        content,
                        "Qty",
                        boldFont,
                        10,
                        370,
                        y
                );

                drawText(
                        content,
                        "Rate",
                        boldFont,
                        10,
                        420,
                        y
                );

                drawText(
                        content,
                        "Amount",
                        boldFont,
                        10,
                        485,
                        y
                );

                y -= 12;

                drawLine(
                        content,
                        left,
                        y,
                        right,
                        y
                );

                y -= 20;

                for (
                        Order.OrderItem item :
                        order.getItems()
                ) {

                    if (
                            y < 180
                    ) {
                        break;
                    }

                    drawText(
                            content,
                            truncate(
                                    item.getProductName(),
                                    24
                            ),
                            regularFont,
                            9,
                            left,
                            y
                    );

                    drawText(
                            content,
                            truncate(
                                    item.getServiceName(),
                                    20
                            ),
                            regularFont,
                            9,
                            220,
                            y
                    );

                    drawText(
                            content,
                            money(
                                    item.getQuantity()
                            ),
                            regularFont,
                            9,
                            370,
                            y
                    );

                    drawText(
                            content,
                            "Rs. "
                                    + money(
                                    item.getUnitPrice()
                            ),
                            regularFont,
                            9,
                            420,
                            y
                    );

                    drawText(
                            content,
                            "Rs. "
                                    + money(
                                    item.getLineTotal()
                            ),
                            regularFont,
                            9,
                            485,
                            y
                    );

                    y -= 18;
                }

                y -= 5;

                drawLine(
                        content,
                        left,
                        y,
                        right,
                        y
                );

                y -= 25;

                float labelX =
                        350;

                float valueX =
                        470;

                drawText(
                        content,
                        "Subtotal",
                        regularFont,
                        10,
                        labelX,
                        y
                );

                drawText(
                        content,
                        "Rs. "
                                + money(
                                order.getSubtotal()
                        ),
                        regularFont,
                        10,
                        valueX,
                        y
                );

                y -= 18;

                drawText(
                        content,
                        "Discount",
                        regularFont,
                        10,
                        labelX,
                        y
                );

                drawText(
                        content,
                        "- Rs. "
                                + money(
                                order.getDiscountAmount()
                        ),
                        regularFont,
                        10,
                        valueX,
                        y
                );

                y -= 18;

                drawText(
                        content,
                        "Express Charge",
                        regularFont,
                        10,
                        labelX,
                        y
                );

                drawText(
                        content,
                        "Rs. "
                                + money(
                                order.getExpressChargeAmount()
                        ),
                        regularFont,
                        10,
                        valueX,
                        y
                );

                y -= 20;

                drawLine(
                        content,
                        labelX,
                        y,
                        right,
                        y
                );

                y -= 22;

                drawText(
                        content,
                        "Grand Total",
                        boldFont,
                        11,
                        labelX,
                        y
                );

                drawText(
                        content,
                        "Rs. "
                                + money(
                                order.getTotalAmount()
                        ),
                        boldFont,
                        11,
                        valueX,
                        y
                );

                y -= 20;

                drawText(
                        content,
                        "Paid Amount",
                        regularFont,
                        10,
                        labelX,
                        y
                );

                drawText(
                        content,
                        "Rs. "
                                + money(
                                order.getPaidAmount()
                        ),
                        regularFont,
                        10,
                        valueX,
                        y
                );

                y -= 18;

                drawText(
                        content,
                        "Balance Due",
                        boldFont,
                        10,
                        labelX,
                        y
                );

                drawText(
                        content,
                        "Rs. "
                                + money(
                                order.getBalanceAmount()
                        ),
                        boldFont,
                        10,
                        valueX,
                        y
                );

                y -= 35;

                drawLine(
                        content,
                        left,
                        y,
                        right,
                        y
                );

                y -= 22;

                drawCenteredText(
                        content,
                        "Thank you for choosing Venkateshwara Fabric Works",
                        boldFont,
                        10,
                        pageWidth,
                        y
                );

                y -= 18;

                drawCenteredText(
                        content,
                        "Please keep this receipt for reference.",
                        regularFont,
                        9,
                        pageWidth,
                        y
                );
            }

            document.save(
                    outputStream
            );

            return outputStream
                    .toByteArray();

        } catch (
                IOException exception
        ) {

            throw new RuntimeException(
                    "Failed to generate receipt PDF",
                    exception
            );
        }
    }

    private void drawText(
            PDPageContentStream content,
            PDType1Font font,
            float fontSize,
            float x,
            float y,
            String text
    ) throws IOException {

        content.beginText();

        content.setFont(
                font,
                fontSize
        );

        content.newLineAtOffset(
                x,
                y
        );

        content.showText(
                safe(text)
        );

        content.endText();
    }

    private void drawText(
            PDPageContentStream content,
            String text,
            PDType1Font font,
            float fontSize,
            float x,
            float y
    ) throws IOException {

        drawText(
                content,
                font,
                fontSize,
                x,
                y,
                text
        );
    }

    private void drawCenteredText(
            PDPageContentStream content,
            String text,
            PDType1Font font,
            float fontSize,
            float pageWidth,
            float y
    ) throws IOException {

        float width =
                font.getStringWidth(text)
                        / 1000
                        * fontSize;

        float x =
                (pageWidth - width)
                        / 2;

        drawText(
                content,
                text,
                font,
                fontSize,
                x,
                y
        );
    }

    private void drawLine(
            PDPageContentStream content,
            float x1,
            float y1,
            float x2,
            float y2
    ) throws IOException {

        content.moveTo(
                x1,
                y1
        );

        content.lineTo(
                x2,
                y2
        );

        content.stroke();
    }

    private String buildInvoiceNumber(
            String orderNumber
    ) {

        if (
                orderNumber == null ||
                orderNumber.isBlank()
        ) {

            return "-";
        }

        if (
                orderNumber.startsWith(
                        "LAUNDRY-"
                )
        ) {

            return "INV-"
                    + orderNumber.substring(
                    "LAUNDRY-".length()
            );
        }

        return "INV-"
                + orderNumber;
    }

    private String money(
            BigDecimal value
    ) {

        if (
                value == null
        ) {

            return "0.00";
        }

        return value
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                )
                .toPlainString();
    }

    private String truncate(
            String value,
            int maxLength
    ) {

        if (
                value == null
        ) {

            return "-";
        }

        if (
                value.length()
                        <= maxLength
        ) {

            return value;
        }

        return value.substring(
                0,
                maxLength - 3
        ) + "...";
    }

    private String safe(
            String value
    ) {

        if (
                value == null ||
                value.isBlank()
        ) {

            return "-";
        }

        return value;
    }
}