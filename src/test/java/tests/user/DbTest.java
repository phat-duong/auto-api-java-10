package tests.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Query;
import model.user.dao.CustomerAddressDao;
import model.user.dao.CustomerDao;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.Test;
import utils.DbUtils;

import java.util.List;
import java.util.UUID;

public class DbTest {
    @Test
    void checkDBConnection(){
//        // A SessionFactory is set up once for an application!
//        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder().build();
////        ObjectMapper mapper = new ObjectMapper();
//        try {
//           SessionFactory sessionFactory =
//                    new MetadataSources(registry)
//                            .addAnnotatedClass(CustomerDao.class)
//                            .addAnnotatedClass(CustomerAddressDao.class)
//                            .buildMetadata()
//                            .buildSessionFactory();
//            sessionFactory.inTransaction(session -> {
//                String hql = "FROM CustomerDao c JOIN FETCH c.addresses WHERE c.id =: id";
//                Query query = session.createQuery(hql, CustomerDao.class);
//                query.setParameter("id", UUID.fromString("c3abe933-47e3-4c15-b2db-17e458cf3a29"));
////                session.createSelectionQuery("from CustomerDao", CustomerDao.class)
////                        .getResultList()
////                        .forEach(customer -> {
////                            System.out.println("Id" + customer.getId());
////                        });
//                CustomerDao customer = (CustomerDao) query.getSingleResult();
//                System.out.println();
//            });
//        }
//        catch (Exception e) {
//            // The registry would be destroyed by the SessionFactory, but we
//            // had trouble building the SessionFactory so destroy it manually.
//            StandardServiceRegistryBuilder.destroy(registry);
//        }

        CustomerDao customer = DbUtils.getCustomerFromDB("c3abe933-47e3-4c15-b2db-17e458cf3a29");
        System.out.println();
    }
}
