package service.implementation;

import dao.SupplierDao;
import model.Supplier;
import service.SupplierService;
import util.LogUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * Implementation of SupplierService interface.
 * Handles business logic for supplier operations.
 */
public class SupplierServiceImpl extends UnicastRemoteObject implements SupplierService {
    
    private SupplierDao supplierDao;
    
    /**
     * Constructor
     * 
     * @throws RemoteException If RMI initialization fails
     */
    public SupplierServiceImpl() throws RemoteException {
        super();
        this.supplierDao = new SupplierDao();
        LogUtil.info("SupplierService initialized");
    }
    
    @Override
    public Supplier createSupplier(Supplier supplier) throws RemoteException {
        try {
            // Validate input
            if (supplier == null) {
                LogUtil.warn("Attempted to create null supplier");
                return null;
            }
            
            if (supplier.getSupplierCode() == null || supplier.getSupplierCode().trim().isEmpty()) {
                LogUtil.warn("Attempted to create supplier without supplier code");
                return null;
            }
            
            if (supplier.getEmail() != null && !supplier.getEmail().trim().isEmpty()) {
                if (supplierDao.emailExists(supplier.getEmail())) {
                    LogUtil.warn("Attempted to create supplier with existing email: " + supplier.getEmail());
                    return null;
                }
            }
            
            if (supplierDao.supplierCodeExists(supplier.getSupplierCode())) {
                LogUtil.warn("Attempted to create supplier with existing supplier code: " + supplier.getSupplierCode());
                return null;
            }
            
            return supplierDao.createSupplier(supplier);
        } catch (Exception e) {
            LogUtil.error("Error creating supplier", e);
            throw new RemoteException("Failed to create supplier", e);
        }
    }
    
    @Override
    public Supplier updateSupplier(Supplier supplier) throws RemoteException {
        try {
            if (supplier == null || supplier.getId() <= 0) {
                LogUtil.warn("Attempted to update invalid supplier");
                return null;
            }
            
            return supplierDao.updateSupplier(supplier);
        } catch (Exception e) {
            LogUtil.error("Error updating supplier", e);
            throw new RemoteException("Failed to update supplier", e);
        }
    }
    
    @Override
    public Supplier deleteSupplier(Supplier supplier) throws RemoteException {
        try {
            if (supplier == null || supplier.getId() <= 0) {
                LogUtil.warn("Attempted to delete invalid supplier");
                return null;
            }
            
            return supplierDao.deleteSupplier(supplier);
        } catch (Exception e) {
            LogUtil.error("Error deleting supplier", e);
            throw new RemoteException("Failed to delete supplier", e);
        }
    }
    
    @Override
    public Supplier findSupplierById(int id) throws RemoteException {
        try {
            if (id <= 0) {
                LogUtil.warn("Invalid supplier ID provided: " + id);
                return null;
            }
            
            return supplierDao.findSupplierById(id);
        } catch (Exception e) {
            LogUtil.error("Error finding supplier by ID: " + id, e);
            throw new RemoteException("Failed to find supplier by ID", e);
        }
    }
    
    @Override
    public Supplier findSupplierByCode(String supplierCode) throws RemoteException {
        try {
            if (supplierCode == null || supplierCode.trim().isEmpty()) {
                LogUtil.warn("Invalid supplier code provided");
                return null;
            }
            
            return supplierDao.findSupplierByCode(supplierCode.trim());
        } catch (Exception e) {
            LogUtil.error("Error finding supplier by code: " + supplierCode, e);
            throw new RemoteException("Failed to find supplier by code", e);
        }
    }
    
    @Override
    public List<Supplier> findSuppliersByName(String name) throws RemoteException {
        try {
            if (name == null || name.trim().isEmpty()) {
                LogUtil.warn("Invalid name provided for supplier search");
                return null;
            }
            
            return supplierDao.findSuppliersByName(name.trim());
        } catch (Exception e) {
            LogUtil.error("Error finding suppliers by name: " + name, e);
            throw new RemoteException("Failed to find suppliers by name", e);
        }
    }
    
    @Override
    public Supplier findSupplierByEmail(String email) throws RemoteException {
        try {
            if (email == null || email.trim().isEmpty()) {
                LogUtil.warn("Invalid email provided");
                return null;
            }
            
            return supplierDao.findSupplierByEmail(email.trim());
        } catch (Exception e) {
            LogUtil.error("Error finding supplier by email: " + email, e);
            throw new RemoteException("Failed to find supplier by email", e);
        }
    }
    
    @Override
    public List<Supplier> findAllSuppliers() throws RemoteException {
        try {
            return supplierDao.findAllSuppliers();
        } catch (Exception e) {
            LogUtil.error("Error finding all suppliers", e);
            throw new RemoteException("Failed to find all suppliers", e);
        }
    }
    
    @Override
    public Supplier getSupplierWithProducts(int supplierId) throws RemoteException {
        try {
            if (supplierId <= 0) {
                LogUtil.warn("Invalid supplier ID provided: " + supplierId);
                return null;
            }
            
            return supplierDao.getSupplierWithProducts(supplierId);
        } catch (Exception e) {
            LogUtil.error("Error getting supplier with products: " + supplierId, e);
            throw new RemoteException("Failed to get supplier with products", e);
        }
    }
    
    @Override
    public boolean supplierCodeExists(String supplierCode) throws RemoteException {
        try {
            if (supplierCode == null || supplierCode.trim().isEmpty()) {
                return false;
            }
            
            return supplierDao.supplierCodeExists(supplierCode.trim());
        } catch (Exception e) {
            LogUtil.error("Error checking if supplier code exists: " + supplierCode, e);
            throw new RemoteException("Failed to check supplier code existence", e);
        }
    }
    
    @Override
    public boolean emailExists(String email) throws RemoteException {
        try {
            if (email == null || email.trim().isEmpty()) {
                return false;
            }
            
            return supplierDao.emailExists(email.trim());
        } catch (Exception e) {
            LogUtil.error("Error checking if email exists: " + email, e);
            throw new RemoteException("Failed to check email existence", e);
        }
    }
    
    @Override
    public List<Supplier> findSuppliersByContactPerson(String contactPerson) throws RemoteException {
        try {
            if (contactPerson == null || contactPerson.trim().isEmpty()) {
                LogUtil.warn("Invalid contact person provided for supplier search");
                return null;
            }
            
            return supplierDao.findSuppliersByContactPerson(contactPerson.trim());
        } catch (Exception e) {
            LogUtil.error("Error finding suppliers by contact person: " + contactPerson, e);
            throw new RemoteException("Failed to find suppliers by contact person", e);
        }
    }
}