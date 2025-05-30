package model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a product supplier in the business management system.
 */
@Entity
@Table(name = "suppliers")
public class Supplier implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(name = "supplier_code", unique = true, nullable = false, length = 50)
    private String supplierCode;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(name = "contact_person", length = 150)
    private String contactPerson;
    
    @Column(length = 150)
    private String email;
    
    @Column(length = 20)
    private String phone;
    
    @Column(length = 500)
    private String address;
    
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();
    
    /**
     * Default constructor
     */
    public Supplier() {
    }
    
    /**
     * Constructor with essential fields
     * 
     * @param supplierCode Unique supplier code
     * @param name Supplier company name
     * @param contactPerson Primary contact person at the supplier
     * @param email Contact email address
     */
    public Supplier(String supplierCode, String name, String contactPerson, String email) {
        this.supplierCode = supplierCode;
        this.name = name;
        this.contactPerson = contactPerson;
        this.email = email;
    }
    
    /**
     * Full constructor
     * 
     * @param id Database ID
     * @param supplierCode Unique supplier code
     * @param name Supplier company name
     * @param contactPerson Primary contact person at the supplier
     * @param email Contact email address
     * @param phone Contact phone number
     * @param address Supplier's address
     */
    public Supplier(int id, String supplierCode, String name, String contactPerson, 
                    String email, String phone, String address) {
        this.id = id;
        this.supplierCode = supplierCode;
        this.name = name;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    // Getters and Setters
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
    
    /**
     * Add a product to this supplier
     * 
     * @param product The product to add
     */
    public void addProduct(Product product) {
        this.products.add(product);
        product.setSupplier(this);
    }
    
    @Override
    public String toString() {
        return "Supplier [id=" + id + ", code=" + supplierCode + ", name=" + name + 
               ", contact=" + contactPerson + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Supplier supplier = (Supplier) obj;
        return id == supplier.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}