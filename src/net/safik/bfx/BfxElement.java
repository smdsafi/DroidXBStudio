package net.safik.bfx;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

public abstract class BfxElement implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long no;
	private String id;
	
	private int parentCount;
	private int childCount;
	private boolean root;
	private Map<String, String> properties = new HashMap<String, String>();
	private Map<BfxKey, Integer> config = new HashMap<BfxKey, Integer>();
	private List<BfxElement> children = new ArrayList<BfxElement>();
	private BfxType type;
	private BfxElement process;
	private BfxElement parent;
	private BfxElement base;

	
	
	public int getConfig(BfxKey k) {
		
		if(config==null)
			Log.e(toString(), "config not available. define key "+k.name()+" in config.properties");
		
		Integer i = config.get(k);
		
		if(i==null)
			Log.e(toString(), "design key "+k.name()+" in config.properties");
		return i;
	}
	
	public void setConfig(BfxKey k, int c) {
		this.config.put(k, c);
	}

	public BfxElement getBase() {
		return base;
	}

	public void setBase(BfxElement base) {
		this.base = base;
	}

	

	public BfxElement(BfxElement parent) {
		this.parent = parent;
		BfxElement p = parent;
		while (p != null) {
			parentCount++;
			p = p.parent;
		}
		this.parent.getChildren().add(this);

	}

	public BfxElement() {
		this.root = true;
	}

	

	public void setNo(long no) {
		this.no = no;
	}

	public long getNo() {
		return no;
	}

	

	public BfxElement getParent() {
		return parent;
	}

	public BfxElement getProcess() {
		return process;
	}

	public void setProcess(BfxElement process) {
		this.process = process;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	

	public int getParentCount() {
		return parentCount;
	}

	public int getChildCount() {

		if (children == null)
			childCount = 0;
		else
			childCount = children.size();

		return childCount;
	}

	public boolean isRoot() {
		return root;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public List<BfxElement> getChildren() {
		return children;
	}

	public void setChildren(List<BfxElement> children) {
		this.children = children;
	}

	public BfxType getType() {
		return type;
	}

	public void setType(BfxType type) {
		this.type = type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((process == null) ? 0 : process.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		BfxElement other = (BfxElement) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (process == null) {
			if (other.process != null)
				return false;
		} else if (!process.equals(other.process))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BfxElement [id=" + id + ", type=" + type + ", process="
				+ process + "]";
	}

	public String toXml() {

		StringBuffer sb = new StringBuffer();
		sb.append("<" + this.type.toString().toLowerCase() + " id=\"" + this.id
				+ "\" ");
		if (properties != null)
			for (String key : properties.keySet()) {
				String value = properties.get(key);
				sb.append(key.toLowerCase() + " =\"" + value + "\" ");
			}
		sb.append(">");
		if (this.children != null)
			for (BfxElement b : this.children) {
				sb.append(b.toXml());
			}
		sb.append("</" + this.type.toString().toLowerCase() + ">");
		return sb.toString();
	}

}
