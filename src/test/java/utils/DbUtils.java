package utils;

import jakarta.persistence.Query;
import model.user.dao.CustomerAddressDao;
import model.user.dao.CustomerDao;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class DbUtils {
    private static SessionFactory sessionFactory;

    public static SessionFactory getDBConnection(){
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder().build();
//        ObjectMapper mapper = new ObjectMapper();
        try {
            if(sessionFactory == null){
               sessionFactory =
                        new MetadataSources(registry)
                                .addAnnotatedClass(CustomerDao.class)
                                .addAnnotatedClass(CustomerAddressDao.class)
                                .buildMetadata()
                                .buildSessionFactory();
            }
        }
        catch (Exception e) {
            StandardServiceRegistryBuilder.destroy(registry);
        }
        return sessionFactory;
    }

    public static CustomerDao getCustomerFromDB(String customerId){
        AtomicReference<CustomerDao> customer = new AtomicReference<>();
        getDBConnection().inTransaction(session -> {
            String hql = "FROM CustomerDao c JOIN FETCH c.addresses WHERE c.id =: id";
            Query query = session.createQuery(hql, CustomerDao.class);
            query.setParameter("id", UUID.fromString(customerId));
            customer.set((CustomerDao) query.getSingleResult());
        });
        return customer.get();
    }
}
