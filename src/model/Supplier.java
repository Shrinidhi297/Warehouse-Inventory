package model;


/** Stores supplier details. */
public class Supplier {
    private String id;
    private String name;
    private String phone;
    private String city;

    /** Creates a validated supplier. */
    public Supplier(String id, String name, String phone, String city) {
        setId(id);
        setName(name);
        setPhone(phone);
        setCity(city);
    }

    /** Returns the id. */
    public String getId() {
        return id;
    }

    /** Sets the id. */
    public void setId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Invalid id.");
        }
        this.id = id;
    }

    /** Returns the name. */
    public String getName() {
        return name;
    }

    /** Sets the name. */
    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Invalid name.");
        }
        this.name = name;
    }

    /** Returns the phone. */
    public String getPhone() {
        return phone;
    }

    /** Sets the phone. */
    public void setPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Invalid phone.");
        }
        this.phone = phone;
    }

    /** Returns the city. */
    public String getCity() {
        return city;
    }

    /** Sets the city. */
    public void setCity(String city) {
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("Invalid city.");
        }
        this.city = city;
    }

}
