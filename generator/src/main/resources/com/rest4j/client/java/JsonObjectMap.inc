/*
 * <xsl:value-of select="rest4j:javadocEscape0($copyright)"/>
 */
package <xsl:value-of select="$package"/>.util;
<![CDATA[
import org.json.JSONException;
import org.json.JSONObject;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public abstract class JsonObjectMap<T> extends AbstractMap<String, T> {
	final JSONObject object;

	protected JsonObjectMap(JSONObject object) {
		this.object = object;
	}

	@Override
	public T get(Object key) {
		return read(object.opt((String)key));
	}

	@Override
	public T put(String key, T value) {
		T val = read(object.opt(key));
		try {
			object.put(key, write(value));
		} catch (JSONException e) {
		}
		return val;
	}

	@Override
	public int size() {
		return object.length();
	}

	@Override
	public Set<Entry<String, T>> entrySet() {
		Iterator it = object.keys();
		TreeSet set = new TreeSet();
		while (it.hasNext()) {
			final String key = (String) it.next();
			set.add(new Entry<String, Object>() {
				@Override
				public String getKey() {
					return key;
				}

				@Override
				public Object getValue() {
					return get(key);
				}

				@Override
				public Object setValue(Object value) {
					throw new UnsupportedOperationException();
				}
			});
		}
		return set;
	}

	@Override
	public void clear() {
		Iterator it = object.keys();
		while (it.hasNext()) object.remove((String) it.next());
	}

	@Override
	public T remove(Object key) {
		Object val = get(key);
		object.remove((String) key);
		return read(val);
	}

	protected abstract T read(Object val);

	protected abstract Object write(T element);

}
]]>