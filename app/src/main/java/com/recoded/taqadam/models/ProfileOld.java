package com.recoded.taqadam.models;

public class ProfileOld {
    /*
    private static final String TAG = Profile.class.getSimpleName();
    public String firstName, lastName;
    public Uri avatar, govtIdUrl;
    public Date birthDate;
    public Gender gender;
    public String phone;
    public City address;

    public String getPhoneNumberString() {
        String ret = phone.concat(" ");
        if (false) {
            ret += "(Verified)";
        } else {
            ret += "(Not Verified)";
        }
        return ret;
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
    */
}
