package com.laundry.pos.service;

import com.laundry.pos.model.ExpressCharge;
import com.laundry.pos.repository.ExpressChargeRepository;
import com.laundry.pos.request.ExpressChargeRequest;
import com.laundry.pos.response.ExpressChargeResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ExpressChargeService {

    private final ExpressChargeRepository expressChargeRepository;

    public ExpressChargeService(
            ExpressChargeRepository expressChargeRepository
    ) {
        this.expressChargeRepository = expressChargeRepository;
    }

    public ExpressChargeResponse createExpressCharge(
            ExpressChargeRequest request
    ) {

        validateRequest(request);

        if (
                expressChargeRepository
                        .existsByPercentage(request.percentage())
        ) {
            throw new RuntimeException(
                    "Express charge percentage already exists"
            );
        }

        ExpressCharge expressCharge =
                new ExpressCharge();

        expressCharge.setName(
                request.name().trim()
        );

        expressCharge.setPercentage(
                request.percentage()
        );

        expressCharge.setActive(
                request.active()
        );

        ExpressCharge savedCharge =
                expressChargeRepository.save(
                        expressCharge
                );

        return toResponse(savedCharge);
    }

    public ExpressChargeResponse getExpressChargeById(
            UUID id
    ) {

        ExpressCharge expressCharge =
                expressChargeRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Express charge not found"
                                )
                        );

        return toResponse(expressCharge);
    }

    public ExpressChargeResponse updateExpressCharge(
            UUID id,
            ExpressChargeRequest request
    ) {

        validateRequest(request);

        ExpressCharge expressCharge =
                expressChargeRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Express charge not found"
                                )
                        );

        expressChargeRepository
                .findByPercentage(
                        request.percentage()
                )
                .ifPresent(existingCharge -> {

                    if (
                            !existingCharge
                                    .getId()
                                    .equals(id)
                    ) {
                        throw new RuntimeException(
                                "Express charge percentage already exists"
                        );
                    }
                });

        expressCharge.setName(
                request.name().trim()
        );

        expressCharge.setPercentage(
                request.percentage()
        );

        expressCharge.setActive(
                request.active()
        );

        ExpressCharge updatedCharge =
                expressChargeRepository.save(
                        expressCharge
                );

        return toResponse(updatedCharge);
    }

    public ExpressChargeResponse updateStatus(
            UUID id,
            boolean active
    ) {

        ExpressCharge expressCharge =
                expressChargeRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Express charge not found"
                                )
                        );

        expressCharge.setActive(active);

        ExpressCharge updatedCharge =
                expressChargeRepository.save(
                        expressCharge
                );

        return toResponse(updatedCharge);
    }

    public void deleteExpressCharge(
            UUID id
    ) {

        ExpressCharge expressCharge =
                expressChargeRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Express charge not found"
                                )
                        );

        expressCharge.setActive(false);

        expressChargeRepository.save(
                expressCharge
        );
    }

    public ExpressChargeResponse
            .ExpressChargeListResponse
    getAllExpressCharges() {

        List<ExpressChargeResponse> expressCharges =
                expressChargeRepository
                        .findAll()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        if (expressCharges.isEmpty()) {

            return new ExpressChargeResponse
                    .ExpressChargeListResponse(
                    "No express charges available",
                    expressCharges
            );
        }

        return new ExpressChargeResponse
                .ExpressChargeListResponse(
                "Express charges fetched successfully",
                expressCharges
        );
    }

    private void validateRequest(
            ExpressChargeRequest request
    ) {

        if (
                request.name() == null ||
                request.name().isBlank()
        ) {
            throw new RuntimeException(
                    "Charge name is required"
            );
        }

        if (
                request.percentage() == null
        ) {
            throw new RuntimeException(
                    "Charge percentage is required"
            );
        }

        if (
                request.percentage()
                        .compareTo(
                                BigDecimal.ZERO
                        ) <= 0
        ) {
            throw new RuntimeException(
                    "Charge percentage must be greater than 0"
            );
        }

        if (
                request.percentage()
                        .compareTo(
                                BigDecimal.valueOf(100)
                        ) > 0
        ) {
            throw new RuntimeException(
                    "Charge percentage cannot exceed 100"
            );
        }
    }

    private ExpressChargeResponse toResponse(
            ExpressCharge expressCharge
    ) {

        return new ExpressChargeResponse(
                expressCharge.getId(),
                expressCharge.getName(),
                expressCharge.getPercentage(),
                expressCharge.isActive()
        );
    }
}