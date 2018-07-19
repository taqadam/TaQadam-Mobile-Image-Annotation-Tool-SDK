
package com.recoded.taqadam.models;

import android.net.Uri;

import com.google.gson.annotations.Expose;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.annotation.Generated;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Profile extends Model {

    @Expose
    private String address;
    @Expose
    private String avatar;
    @Expose
    private Date birthDate;
    @Expose
    private String firstName;
    @Expose
    private String gender;
    @Expose
    private String lastName;
    @Expose
    private String phone;

    public String getAddress() {
        return address;
    }

    public int getAddressId() {
        if (address == null) return 0;
        for (City city : City.values()) {
            if (city.name.equals(address)) {
                return city.getId();
            }
        }
        return 0;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Uri getAvatar() {
        return Uri.parse(avatar);
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getBirthDate() {
        String format = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        return sdf.format(birthDate);
    }

    public Date getBirthDateObject() {
        return birthDate;
    }

    public String getAgeString() {
        String ageString;

        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.setTime(birthDate);
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        String format = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        ageString = sdf.format(dob.getTime()) + " (" + age + " years)";

        return ageString;
    }

    public void setBirthDate(String birthDate) {
        String format = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        try {
            this.birthDate = sdf.parse(birthDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Gender getGenderEnum() {
        return gender.equals("m") || gender.equalsIgnoreCase("male")
                ? Gender.MALE
                : (gender.equals("f") || gender.equalsIgnoreCase("female")
                ? Gender.FEMALE
                : Gender.NOT_SPECIFIED);
    }

    public void setGender(String gender) {
        this.gender = gender.equalsIgnoreCase("Male")
                ? Gender.MALE.getIdentifier()
                : (gender.equalsIgnoreCase("Female")
                ? Gender.FEMALE.getIdentifier()
                : Gender.NOT_SPECIFIED.getIdentifier());
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public enum Gender {
        MALE("m-Male"),
        FEMALE("f-Female"),
        NOT_SPECIFIED("n-Not Disclosed");
        private final String identifier;
        private final String gender;

        Gender(String friendlyName) {
            String[] split = friendlyName.split("-");
            this.identifier = split[0];
            this.gender = split[1];
        }

        public String getName() {
            return gender;
        }

        public String getIdentifier() {
            return identifier;
        }
    }

    public enum City {
        //These ids should be the same as the order of cities
        // in the 'cities_lebanon' array or else it won't work
        //We will set the selected spinner item as the userCity.getId()
        ALEY("Aley", 1),
        BAALBEK("Baalbek", 2),
        BATROUN("Batroun", 3),
        BEIRUT("Beirut", 4),
        BYBLOS("Byblos", 5),
        JOUNIEH("Jounieh", 6),
        NABATIEH("Nabatieh", 7),
        SIDON("Sidon", 8),
        TRIPOLI("Tripoli", 9),
        TYRE("Tyre", 10),
        ZAHLE("Zahle", 11),
        ZGHARTA("Zgharta", 12);

        private final int positionId;
        private final String name;

        City(String name, int id) {
            this.name = name;
            this.positionId = id;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return positionId;
        }
    }

}
