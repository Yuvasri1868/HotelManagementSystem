package com.hotel.model;

/** Customer extends Person (inheritance). */
public class Customer extends Person {
    private int customerId;
    private String email = "";
    private String address = "";
    private String idProof = "";

    public Customer() { }
    public Customer(int customerId, String name, String phone, String email, String address, String idProof) {
        super(name, phone);
        this.customerId = customerId;
        this.email = email;
        this.address = address;
        this.idProof = idProof;
    }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getIdProof() { return idProof; }
    public void setIdProof(String idProof) { this.idProof = idProof; }

    /** Used in combo boxes. */
    @Override
    public String toString() { return getName() + " (" + getPhone() + ")"; }
}
