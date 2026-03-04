package org.doit.ik.complex;

import java.util.List;
import org.doit.ik.api.CctvRepository;
import org.doit.ik.api.SafeReturnPath;
import org.doit.ik.api.SafeReturnPathRepository;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper; // ✅ 추가
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SafetyService {

    private final CctvRepository cctvRepository;
    private final SafeReturnPathRepository safeReturnPathRepository;
    private final ObjectMapper objectMapper;

    public int calculateSafetyScore(Double lat, Double lng, String address) {
        // 1. CCTV 점수 (40점 만점)
        Integer cctvCount = cctvRepository.countCctvNearby(lat, lng);
        int cctvScore = Math.min((cctvCount != null ? cctvCount : 0) * 2, 40); 
        
        int policeScore = 20; 
        int extraScore = 0;

        if (address != null && address.contains("서울")) {
            // ✅ 수정: bjdName을 따로 뽑지 않고 address를 그대로 넘깁니다.
            double distance = getSafePathDistance(lat, lng, address);
            
            if (distance <= 0) extraScore = 0;
            else if (distance <= 300) extraScore = 30; // 300m 이내
            else if (distance <= 500) extraScore = 20; // 500m 이내
            else extraScore = 10;
            
            return Math.min(cctvScore + policeScore + extraScore, 100);
        } else {
            return (int)((cctvScore + policeScore) * 1.42);
        }
    }

    public int getSafePathDistance(Double userLat, Double userLng, String address) {
        if (address == null) return 0;
        
        String bjdName = extractBjdName(address); 
        if (bjdName == null) return 0;

        List<SafeReturnPath> paths = safeReturnPathRepository.findByBjdName(bjdName);
        double minDistance = Double.MAX_VALUE;

        for (SafeReturnPath path : paths) {
            try {
                JsonNode coords = objectMapper.readTree(path.getPathCoordinates());
                if (coords.isArray()) {
                    for (JsonNode point : coords) {
                        // ✅ 중요: GeoJSON 좌표 순서 확인 [경도, 위도]
                        double targetLng = point.get(0).asDouble(); 
                        double targetLat = point.get(1).asDouble(); 
                        
                        // ✅ 위도는 위도끼리(userLat, targetLat), 경도는 경도끼리(userLng, targetLng)
                        double dist = calculateHaversine(userLat, userLng, targetLat, targetLng);
                        if (dist < minDistance) minDistance = dist;
                    }
                }
            } catch (Exception e) { continue; }
        }
        if (minDistance == Double.MAX_VALUE) return -1; // 데이터 자체가 없음
        
        int distanceInMeter = (int)(minDistance * 1000);
        
        // 1km(1000m)가 넘으면 사실상 주변에 없는 것으로 간주
        if (distanceInMeter > 1000) return -2; // 너무 멂
        
        return distanceInMeter;
    }

    private double calculateHaversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; 
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private String extractBjdName(String address) {
        if (address == null) return null;
        String[] parts = address.split(" ");
        for (String part : parts) {
            if (part.endsWith("동") || part.endsWith("가") || part.endsWith("로")) {
                return part;
            }
        }
        return null;
    }

    public int getCctvCount(Double lat, Double lng) {
        Integer count = cctvRepository.countCctvNearby(lat, lng);
        return (count != null) ? count : 0;
    }
}