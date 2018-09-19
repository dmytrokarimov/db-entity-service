package net.util.service;

import java.util.List;

import net.util.async.ManagerFutureTask;
import net.util.service.entity.BaseEntity;

public interface EntityService<E extends BaseEntity> {
	
	/**
	 * blocks other calls until lock will be released
	 */
	ManagerFutureTask<?> lock(Runnable runnable);
	
	ManagerFutureTask<E> save(E entity);
	
	ManagerFutureTask<E> delete(E entity);

	ManagerFutureTask<E> findById(Long idPar);

	ManagerFutureTask<List<E>> findAll();

	List<E> findAllSync();
}
