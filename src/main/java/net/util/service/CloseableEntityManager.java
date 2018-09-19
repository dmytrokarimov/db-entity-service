package net.util.service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;

public class CloseableEntityManager implements AutoCloseable {
    private final EntityManager entityManager;

    public CloseableEntityManager(EntityManagerFactory emf) {
        entityManager = emf.createEntityManager();
    }

    @Override
    public void close() {
        // entityManager.flush();
        entityManager.close();
    }

    public void persist(Object entity) {
        entityManager.persist(entity);
    }

    public <T> T merge(T entity) {
        return entityManager.merge(entity);
    }

    public void remove(Object entity) {
        entityManager.remove(entity);
    }

    public void refresh(Object entity) {
        entityManager.refresh(entity);
    }

    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        return entityManager.createQuery(qlString, resultClass);
    }

    public <T> T find(Class<T> entityClass, Object primaryKey) {
        return entityManager.find(entityClass, primaryKey);
    }

    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        return entityManager.createNamedQuery(name, resultClass);
    }

    public EntityTransaction getTransaction() {
        return entityManager.getTransaction();
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }
}
