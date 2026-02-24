package org.doit.ik.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class HouseApiDto {
	@JsonProperty("bldgNm") private String title;     // -> Complex.title
    @JsonProperty("addr")   private String address;   // -> Complex.address
    @JsonProperty("lat")    private Double latitude;  // -> Complex.latitude
    @JsonProperty("lng")    private Double longitude; // -> Complex.longitude
    
    @JsonProperty("rentTp") private String rentType;  // -> RoomType.rentType
    @JsonProperty("grnt")   private Integer deposit;  // -> RoomType.deposit
    @JsonProperty("mth")    private Integer monthly;  // -> RoomType.monthlyRent
    @JsonProperty("pnu")    private String apiId;     // -> RoomType.apiOriginId
}
