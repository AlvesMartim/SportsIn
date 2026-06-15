package org.SportsIn.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Sous-objet "athlete" dans la réponse token Strava. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StravaAthleteDTO {

    @JsonProperty("id")
    private long id;

    @JsonProperty("firstname")
    private String firstname;

    @JsonProperty("lastname")
    private String lastname;

    @JsonProperty("profile_medium")
    private String profileMedium;

    @JsonProperty("city")
    private String city;

    @JsonProperty("country")
    private String country;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }

    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }

    public String getProfileMedium() { return profileMedium; }
    public void setProfileMedium(String profileMedium) { this.profileMedium = profileMedium; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}
