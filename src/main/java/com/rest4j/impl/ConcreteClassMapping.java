package com.rest4j.impl;

import com.rest4j.ApiException;
import com.rest4j.ConfigurationException;
import com.rest4j.DynamicMapper;
import com.rest4j.impl.model.Field;
import com.rest4j.impl.model.FieldAccessType;
import com.rest4j.impl.model.Model;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class ConcreteClassMapping {
	private final Model model;
	private final MarshallerImpl marshaller;
	String name;
	Class clz;
	FieldMapping[] fields;
	List<Field> leftoverFields = new ArrayList<Field>();
	Object customMapper;

	public ConcreteClassMapping(MarshallerImpl marshaller, Class clz, Model model, Object customMapper) throws ConfigurationException {
		this.marshaller = marshaller;
		this.name = model.getName();
		this.clz = clz;
		this.customMapper = customMapper;
		this.model = model;

		if (model.getFields() == null) {
			this.fields = new FieldMapping[0];
			return;
		}

		List<FieldMapping> fields = new ArrayList<FieldMapping>(model.getFields().getSimpleAndComplex().size());
		for (Field fld : model.getFields().getSimpleAndComplex()) {
			FieldMapping fieldMapping;

			if (customMapper instanceof DynamicMapper) {
				if (fld.getMappingMethod() != null) {
					throw new ConfigurationException("mapping-method cannot be used along with DynamicMapper");
				}
				fieldMapping = new DynamicFieldMapping(marshaller, fld, (DynamicMapper)customMapper, name /* owner name */);
			} else if (fld.getMappingMethod() == null) {

				if (fld.getProp() != null && fld.getProp().contains(".")) {
					fieldMapping = new NestedFieldMapping(marshaller, fld, name /* owner name */);
				} else {
					fieldMapping = new SimpleFieldMapping(marshaller, fld, name /* owner name */);
				}
			} else {
				fieldMapping = new CustomFieldMapping(marshaller, fld, customMapper, name /* owner name */);
			}
			if (!fieldMapping.initAccessors(clz)) {
				leftoverFields.add(fld);
				continue;
			}
			fields.add(fieldMapping);
		}
		this.fields = fields.toArray(new FieldMapping[fields.size()]);
	}

	void unmarshal(JSONObject object, Object inst) throws ApiException {
		// first unmarshal non-custom-mapping properties, so that we could use them in a custom mapping logic
		for (FieldMapping field : getOrderedFieldsForUnmarshal()) {
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

		for (FieldMapping field : getOrderedFieldsForUnmarshal()) {
			if (object.has(field.name)) {
				Object fieldVal = object.opt(field.name);
				fieldVal = field.unmarshal(fieldVal);
				field.set(patched, fieldVal);
				result.put(field.name, fieldVal);
			}
		}
		return result;
	}

	private ArrayList<FieldMapping> getOrderedFieldsForUnmarshal() {
		// first unmarshal non-custom-mapping properties, so that we could use them in a custom mapping logic
		ArrayList<FieldMapping> ordered = new ArrayList<FieldMapping>();
		for (FieldMapping field : fields) {
			if (field instanceof CustomFieldMapping || field.access == FieldAccessType.READONLY) continue;
			ordered.add(field);
		}
		for (FieldMapping field : fields) {
			if (!(field instanceof CustomFieldMapping) || field.access == FieldAccessType.READONLY) continue;
			ordered.add(field);
		}
		return ordered;
	}

	void link() throws ConfigurationException {
		for (int i = 0; i < fields.length; i++) {
			FieldMapping fieldImpl = fields[i];
			fieldImpl.link(marshaller);
		}

	}

	public List<com.rest4j.type.Field> getFields() {
		ArrayList<com.rest4j.type.Field> list = new ArrayList<com.rest4j.type.Field>();
		for (FieldMapping mapping: fields) {
			list.add(mapping);
		}
		return list;
	}
}
