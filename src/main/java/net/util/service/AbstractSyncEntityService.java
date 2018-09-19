package net.util.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import javax.persistence.EntityManagerFactory;
import javax.swing.SwingUtilities;

import org.hibernate.Session;

import net.util.async.AsyncManager;
import net.util.async.ManagerFutureTask;
import net.util.service.entity.BaseEntity;

/**
 * CRUD service for abstract DB. <br>
 * All methods are executed asynchronously, but each method is waiting while previous won't be finished <br> 
 * 
 * Note! Method {@link AbstractSyncEntityService#initServices} have to be called before use.
 * 
 * @author Dmytro Karimov
 *
 * @param <E> entity
 */
public abstract class AbstractSyncEntityService<E extends BaseEntity> implements EntityService<E> {
	private final Class<E> persistentClass;

	protected static final AsyncManager async = new AsyncManager(SwingUtilities::invokeLater);

	private static EntityManagerFactory entityManagerFactory;

	public static void initServices(Supplier<EntityManagerFactory> entityManagerSupplier) {
		entityManagerFactory = entityManagerSupplier.get();
	}
	
	public AbstractSyncEntityService(Class<E> persistentClass) {
		this.persistentClass = persistentClass;
	}

	@Override
	public ManagerFutureTask<?> lock(Runnable runnable) {
		return async.schedule(runnable);
	}

	@Override
	public ManagerFutureTask<E> save(E entity) {
		return async.schedule(() -> {
			try (CloseableEntityManager em = getEntityManager()){
				em.getTransaction().begin();
				entity.setTimestamp(new Date());

				((Session) em.getEntityManager().getDelegate()).saveOrUpdate(entity);

				em.getTransaction().commit();

				return entity;
			}
		});
	}

	@Override
	public ManagerFutureTask<E> delete(E entity) {
		return async.schedule(() -> {
			try (CloseableEntityManager em = getEntityManager()) {
				em.getTransaction().begin();
				E managedEntity = entity;
				if (!em.getEntityManager().contains(managedEntity)) {
					managedEntity = em.merge(entity);
				}

				em.remove(managedEntity);
				em.getTransaction().commit();

				return managedEntity;
			}
		});
	}

	protected void refresh(E entity) {
		try (CloseableEntityManager em = getEntityManager()) {
			em.getTransaction().begin();
			em.refresh(entity);
			em.getTransaction().rollback();
		}
	}

	protected <T> ManagerFutureTask<T> findAsync(Supplier<T> supplier) {
		AtomicReference<T> result = new AtomicReference<>();
		return async.schedule(() -> {
			result.set(supplier.get());

			return result.get();
		});
	}

	@Override
	public final ManagerFutureTask<E> findById(Long id) {
		return findAsync(() -> {
			try (CloseableEntityManager em = getEntityManager()) {
				em.getTransaction().begin();
				E e = em.find(persistentClass, id);
				em.getTransaction().rollback();
				return e;
			}
		});
	}

	@Override
	public List<E> findAllSync(){
		try (CloseableEntityManager em = getEntityManager()) {
			em.getTransaction().begin();
			List<E> results = em.createQuery("from " + getPersistentClass().getName(), persistentClass).getResultList();
			em.getTransaction().rollback();
			return results;
		}
	}

	@Override
	public ManagerFutureTask<List<E>> findAll(){
		return findAsync(() -> {
			return findAllSync();
		});
	}

	public final void saveAll(Collection<E> entities) {
		try (CloseableEntityManager em = getEntityManager()) {
			em.getTransaction().begin();
			entities.forEach(e -> {
				e.setTimestamp(new Date());
				if (e.isNew()) {
					em.persist(e);
				}
			});
			em.getTransaction().commit();
		}
	}

	protected final CloseableEntityManager getEntityManager() {
		return new CloseableEntityManager(entityManagerFactory);
	}

	public final Class<E> getPersistentClass() {
		return persistentClass;
	}
}
