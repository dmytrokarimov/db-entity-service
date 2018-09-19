package net.util.service;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Supplier;

import net.util.async.ManagerFutureTask;
import net.util.service.entity.BaseEntity;

/**
 * Can be used for storing settings in DB
 * 
 * @author Dmytro Karimov
 *
 * @param <E> settings entity 
 */
public class AbstractSettingsService<E extends BaseEntity> extends AbstractSyncEntityService<E> {

	private E settings;
	
	public AbstractSettingsService(Class<E> persistentClass) {
		super(persistentClass);
	}

	private E newInstant() {
		try {
			return getPersistentClass().getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	protected ManagerFutureTask<E> findSingleSetting(Supplier<E> newSettingSupplier) {
		return findAsync(() -> {
			return findSingleSettingSync(newSettingSupplier);
		});
	}

	protected E findSingleSettingSync(Supplier<E> newSettingSupplier) {
		List<E> allSettings = findAllSync();
		if (allSettings.isEmpty()) {
			E newSettings = newSettingSupplier.get();
			save(newSettings);
			return newSettings;
		}
		return allSettings.get(0);
	}

	/**
	 * Return cached settings or read from DB
	 */
	public ManagerFutureTask<E> getSettings() {
		if (settings != null) {
			return async.schedule(() -> settings);
		} else {
			return findSingleSetting(this::newInstant);
		}
	}
	
	public E getSettingsSync() {
		if (settings == null) {
			settings = findSingleSettingSync(this::newInstant);
		}
		return settings;
	}
	
	public void saveSettings() {
		this.getSettings().onDone(this::save);
	}
}
