package com.android.academy.spb.team1;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class CityResult {
    @SerializedName("results")
    private List<PlaceInfo> results;

    public List<PlaceInfo> getResults() {
        return results;
    }


    public class PlaceInfo{

        @SerializedName("types")
        private List<String> types;
        @SerializedName("address_components")
        private List<AddressComponent> components;
        @SerializedName("formatted_address")
        private String formatted_address;

        public List<String> getTypes() {
            return types;
        }
        public List<AddressComponent> getComponents() {
            return components;
        }
        public String getFormatted_address() {
            return formatted_address;
        }
    }

    public class AddressComponent{

        @SerializedName("long_name")
        private String longName;
        @SerializedName("short_name")
        private String shortName;
        @SerializedName("types")
        private List<String> types;

        public String getLongName() {
            return longName;
        }

        public String getShortName() {
            return shortName;
        }

        public List<String> getTypes() {
            return types;
        }
    }


}
