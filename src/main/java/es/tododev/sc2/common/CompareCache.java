package es.tododev.sc2.common;

import java.util.Comparator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class CompareCache implements Comparator<Long>{

	private final LoadingCache<Key, Integer> cache = CacheBuilder.newBuilder()
		    .maximumSize(10000)
		    .expireAfterWrite(10, TimeUnit.MINUTES)
		    .build(
		        new CacheLoader<Key, Integer>() {
		          public Integer load(Key key) throws Exception {
		            return key.arg0.compareTo(key.arg1);
		          }
		        });
	
	@Override
	public int compare(Long arg0, Long arg1) {
		Key key = new Key(arg0, arg1);
		try {
			return cache.get(key).intValue();
		} catch (ExecutionException e) {
			throw new RuntimeException("Cannot retrieve value from cache "+key, e);
		}
	}
	
	private static class Key {
		
		private final Long arg0;
		private final Long arg1;
		
		public Key(Long arg0, Long arg1) {
			this.arg0 = arg0;
			this.arg1 = arg1;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((arg0 == null) ? 0 : arg0.hashCode());
			result = prime * result + ((arg1 == null) ? 0 : arg1.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if (arg0 == null) {
				if (other.arg0 != null)
					return false;
			} else if (!arg0.equals(other.arg0))
				return false;
			if (arg1 == null) {
				if (other.arg1 != null)
					return false;
			} else if (!arg1.equals(other.arg1))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Key [arg0=" + arg0 + ", arg1=" + arg1 + "]";
		}
		
	}

}
