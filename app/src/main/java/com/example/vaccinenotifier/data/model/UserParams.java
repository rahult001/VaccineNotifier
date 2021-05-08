package com.example.vaccinenotifier.data.model;

public class UserParams {
    String selectedState;
    String selectedCity;
    Integer selectedStateId;
    Integer selectedCityId;
    String selectedAge;

    public UserParams() {

    }

    public String getSelectedState() {
        return selectedState;
    }

    public void setSelectedState(String selectedState) {
        this.selectedState = selectedState;
    }

    public String getSelectedCity() {
        return selectedCity;
    }

    public void setSelectedCity(String selectedCity) {
        this.selectedCity = selectedCity;
    }

    public Integer getSelectedStateId() {
        return selectedStateId;
    }

    public void setSelectedStateId(Integer selectedStateId) {
        this.selectedStateId = selectedStateId;
    }

    public Integer getSelectedCityId() {
        return selectedCityId;
    }

    public void setSelectedCityId(Integer selectedCityId) {
        this.selectedCityId = selectedCityId;
    }

    public String getSelectedAge() {
        return selectedAge;
    }

    public void setSelectedAge(String selectedAge) {
        this.selectedAge = selectedAge;
    }
}
