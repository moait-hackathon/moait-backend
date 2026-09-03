package com.moait.moai.domain.user.entity;

import com.moait.moai.common.enums.HoldingAssetType;

/** {@link InvestmentProfile#holdingAssets} JSON 배열의 요소. */
public record HoldingAsset(HoldingAssetType type, Long amount) {
}
