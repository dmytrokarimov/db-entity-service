# DB entity service

CRUD service for abstract DB.  
All methods are executed asynchronously, but each method is waiting while previous won't be finished  


```java
	class SomeService extend AbstractSyncEntityService<SomeEntity> {
	 ...
	}
	someService = SomeService.getInstance(); //i.e.
```

Here is example with lock:


```java
	someService.lock(() -> Thread.sleep(1000));//locks other calls to DB until lock is not released
	
	someService.findById(...).onDone(entity -> {
		//onDone won't be executed while lock above is not released 
	});
```

Here is async example:

```java	
	someService.findById(0)
		.next(someOtherService::processResults) //let's think this operation will take a lot of time
		.onDone(/*show smth on UI*/)		
		
	someService.findById(1).onDone(/*show smth on UI*/)// will wait while calls above (findById -> next) is not finished

```
