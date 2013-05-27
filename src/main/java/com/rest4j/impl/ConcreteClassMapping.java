package com.rest4j.impl;

import com.rest4j.ApiException;
import com.rest4j.ConfigurationException;
import com.rest4j.impl.model.Field;
import com.rest4j.impl.model.FieldAccessType;
import com.rest4j.impl.model.Model;
import org.json.JSONException;
import org.json.JSONObject;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class ConcreteClassMapping {
	private final Model model;
	private final Marshaller marshaller;
	String name;
	Class clz;
	FieldMapping[] fields;
	List<Field> leftoverFields = new ArrayList<Field>();
	Object customMapper;

	public ConcreteClassMapping(Marshaller marshaller, Class clz, Model model, Object customMapper) throws ConfigurationException {
		this.marshaller = marshaller;
		this.name = model.getName();
		this.clz = clz;
		this.customMapper = customMapper;
		this.model = model;
		Map<String, PropertyDescriptor> descriptors = new HashMap<String, PropertyDescriptor>();
		try {
			BeanInfo info = Introspector.getBeanInfo(clz);
			for (PropertyDescriptor descr : info.getPropertyDescriptors()) {
				descriptors.put(descr.getName().toLowerCase(), descr);
			}
		} catch (IntrospectionException e) {
			throw new ConfigurationException("Cannot introspect bean " + clz, e);
		}
		List<FieldMapping> fields = new ArrayList<FieldMapping>(model.getFields().getSimpleAndComplex().size());
		for (Field fld : model.getFields().getSimpleAndComplex()) {
			FieldMapping fieldMapping = new FieldMapping(marshaller, fld, customMapper, name /* owner name */);

			String forClause = " for " + model.getName() + "." + fld.getName();
			if (fld.getMappingMethod() == null) {
				String propName;
				if (fld.getProp() == null) propName = fld.getName();
				else propName = fld.getProp();
				PropertyDescriptor descr = descriptors.get(propName.toLowerCase());
				if (descr == null) {
					if (fld.getAccess() != FieldAccessType.WRITEONLY && !fieldMapping.isConstant()) {
						if (fld.isOptional()) {
							leftoverFields.add(fld);
						} else {
							throw new ConfigurationException("Cannot find property " + propName + " in class " + clz);
						}
						continue;
					}
				} else {
					fieldMapping.propGetter = descr.getReadMethod();
					fieldMapping.propSetter = descr.getWriteMethod();
				}
			} else {
				if (customMapper == null) {
					throw new ConfigurationException("No field mapper specified, but mapping method is set"+forClause);
				}
				for (Method method : customMapper.getClass().getMethods()) {
					if (method.getName().equals(fieldMapping.mapping)) {
						if (method.getParameterTypes().length <= 0 || method.getParameterTypes().length > 2) {
							throw new ConfigurationException("Wrong accessor " + method.getName() + " parameter count: " + method.getParameterTypes().length + forClause + ". Should be one parameter (getter) or two parameters (setter).");
						}
						Class paramType = method.getParameterTypes()[0];
						if (!paramType.isAssignableFrom(clz)) {
							throw new ConfigurationException("Wrong accessor '" + method.getName() + "' parameter type: " + paramType + "; expected " + clz + forClause);
						}
						if (method.getParameterTypes().length == 1) {
							if (fieldMapping.propGetter != null) {
								throw new ConfigurationException("Ambiguous getter '" + method.getName() + "'" + forClause);
							}
							if (method.getReturnType() == void.class) {
								throw new ConfigurationException("Void return value of getter '" + method.getName() + "'" + forClause);
							}
							fieldMapping.propGetter = method;
						} else {
							if (fieldMapping.propSetter != null) {
								throw new ConfigurationException("Ambiguous setter '" + method.getName() + "'" + forClause);
							}
							if (method.getReturnType() != void.class) {
								throw new ConfigurationException("Non-void return value " + method.getReturnType() + " of setter '" + method.getName() + "'" + forClause);
							}
							fieldMapping.propSetter = method;
						}
					}
				}
			}
			if (fieldMapping.propGetter == null && fld.getAccess() != FieldAccessType.WRITEONLY && !fieldMapping.isConstant()) {
				if (fld.isOptional()) {
					leftoverFields.add(fld);
					continue;
				} else {
					throw new ConfigurationException("No getter " + forClause + ", but it is not declared as writeonly. Use access='writeonly' in <complex> and <simple> tags.");
				}
			}
			fields.add(fieldMapping);
		}
		this.fields = fields.toArray(new FieldMapping[fields.size()]);
	}

	void unmarshal(JSONObject object, Object inst) throws ApiException {
		// first unmarshal non-custom-mapping properties, so that we could use them in a custom mapping logic
		for (FieldMapping field : fields) {
			if (field.mapping != null || field.access == FieldAccessType.READONLY) continue;
			Object fieldVal = object.opt(field.name);
			fieldVal = field.unmarshal(fieldVal);
			field.set(inst, fieldVal);
		}
		for (FieldMapping field : fields) {
			if (field.mapping == null || field.access == FieldAccessType.READONLY) continue;
			Object fieldVal = object.opt(field.name);
			fieldVal = field.unmarshal(fieldVal);
			field.set(inst, fieldVal);
		}
	}

	void marshal(JSONObject json, Object val) throws ApiException {
		for (FieldMapping field : fields) {
			if (field.access == FieldAccessType.WRITEONLY) continue;
			Object fieldValue = field.value == null ? field.get(val) : field.value;
			fieldValue = field.marshal(fieldValue);
			if (fieldValue != null) {
				try {
					json.put(field.name, fieldValue);
				} catch (JSONException e) {
					throw new ApiException("Wrong value of field "+name+"."+field.name+": "+e.getMessage()).setHttpStatus(500);
				}
			}
		}
	}

	HashMap<String, Object> unmarshalPatch(JSONObject object, Object patched) throws ApiException {
		HashMap<String, Object> result = new HashMap<String, Object>();

		// first unmarshal non-custom-mapping properties, so that we could use them in a custom mapping logic
		ArrayList<FieldMapping> ordered = new ArrayList<FieldMapping>();
		for (FieldMapping field : fields) {
			if (field.mapping != null || field.access == FieldAccessType.READONLY) continue;
			ordered.add(field);
		}
		for (FieldMapping field : fields) {
			if (field.mapping == null || field.access == FieldAccessType.READONLY) continue;
			ordered.add(field);
		}
		for (FieldMapping field : ordered) {
			if (object.has(field.name)) {
				Object fieldVal = object.opt(field.name);
				fieldVal = field.unmarshal(fieldVal);
				field.set(patched, fieldVal);
				result.put(field.name, fieldVal);
			}
		}
		return result;
	}

	void link() throws ConfigurationException {
		for (int i = 0; i < fields.length; i++) {
			FieldMapping fieldImpl = fields[i];
			fieldImpl.link(marshaller);
		}

	}
}
