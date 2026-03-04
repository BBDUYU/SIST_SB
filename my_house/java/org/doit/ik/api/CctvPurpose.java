package org.doit.ik.api;

import lombok.Getter;

@Getter
public enum CctvPurpose {
    VIGILANCE("생활방범"),
    MULTIPURPOSE("다목적"),
    CHILD_PROTECTION("어린이보호"),
    TRAFFIC_ENFORCEMENT("교통단속"),
    FACILITY_MANAGEMENT("시설물관리"),
    WASTE_ENFORCEMENT("쓰레기단속"),
    DISASTER_PREVENTION("재난재해"),
    VEHICLE_VIGILANCE("차량방범"),
    TRAFFIC_INFO("교통정보수집"),
    ETC("기타");

    private final String description;

    CctvPurpose(String description) {
        this.description = description;
    }

    // CSV의 문자열을 Enum으로 안전하게 매핑하는 메서드
    public static CctvPurpose fromString(String text) {
        for (CctvPurpose purpose : CctvPurpose.values()) {
            if (purpose.description.equals(text)) {
                return purpose;
            }
        }
        return ETC; // 매칭되는 게 없으면 '기타'로 처리해서 에러 방지
    }
}