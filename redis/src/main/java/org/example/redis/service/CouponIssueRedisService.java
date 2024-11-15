package org.example.redis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.redis.domain.dto.ValidateCouponIssueRequest;
import org.example.redis.exception.RedisException;
import org.example.redis.service.port.CouponIssueCacheStore;
import org.springframework.stereotype.Component;

import static org.example.redis.exception.RedisErrorCode.*;
import static org.example.redis.utils.RedisKeyUtils.getCouponIssueRequestKey;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssueRedisService {

    private final CouponIssueCacheStore couponIssueCacheStore;

    public void checkCouponIssueQuantityAndDuplicate(ValidateCouponIssueRequest validateCouponIssueRequest) {
        Long couponId = validateCouponIssueRequest.couponId();
        String maxQuantity = String.valueOf(validateCouponIssueRequest.maxQuantity());
        String userId = String.valueOf(validateCouponIssueRequest.userId());
        log.info("Checking Total Issued Coupon Quantity and Duplicate Issuance." +
                "Coupon ID: [{}], User ID: [{}]", couponId, userId);

        String couponIssueRequestKey = getCouponIssueRequestKey(couponId);
        Long result = couponIssueCacheStore.checkCouponIssueAvailability(couponIssueRequestKey, maxQuantity, userId);

        switch (result.intValue()) {
            case 0 -> log.info("Coupon Issued Successfully. " +
                    "USER ID: [{}], Coupon ID: [{}]", userId, couponId);
            case 1 -> throw new RedisException(COUPON_ISSUE_QUANTITY_EXCEEDED);
            case 2 -> throw new RedisException(COUPON_ALREADY_ISSUED_BY_USER);
        }
    }

    public Long getIssuedCouponCount(Long couponId) {
        String issuedCountKey = getCouponIssueRequestKey(couponId);
        return couponIssueCacheStore.getIssuedCouponUserCount(issuedCountKey);
    }
}
