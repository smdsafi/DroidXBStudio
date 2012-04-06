package net.safik.bfx;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.os.Message;

public class BfxFactory {

	private final static Properties config = new Properties();

	static {
		try {

			config.load(BfxFactory.class
					.getResourceAsStream("config.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static BfxElement configureElement(DBHelper helper, String id,
			String types) {

		BfxType type = BfxType.valueOf(types.toUpperCase());

		BfxElement element = createElement(id, type);

		String xml = helper.cursorSelectXml(element);
		if (xml != null || !xml.isEmpty())
			parseXml(helper, element, xml);
		else
			helper.insert(element);

		return element;
	}

	private static void parseXml(DBHelper helper, BfxElement element,
			String string) {

		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(string));
		try {
			Document d = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().parse(is);

			Element e = d.getDocumentElement();
			processAttr(element, e);
			NodeList nl = e.getChildNodes();
			processChild(helper, element, nl);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void processChild(DBHelper helper, BfxElement bfxElement,
			NodeList nl) {
		if (nl != null)
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE) {

					BfxElement element = createElement(null,
							BfxType.valueOf(n.getNodeName().toUpperCase()),
							bfxElement);
					processAttr(element, n);
					if (isRefRoot(element.getType())) {
						BfxElement ba = createElement(element.getId(),
								getBaseType(element.getType()));
						element.setBase(ba);
					}
					processChild(helper, element, n.getChildNodes());

				}

			}

	}

	private static void processAttr(BfxElement bfxElement, Node e) {

		bfxElement.setType(BfxType.valueOf(e.getNodeName().toUpperCase()));

		NamedNodeMap m = e.getAttributes();
		for (int i = 0; i < m.getLength(); i++) {
			Node a = m.item(i);
			if (a.getNodeName().equalsIgnoreCase("id")) {
				bfxElement.setId(a.getNodeValue());
			} else {
				bfxElement.getProperties().put(a.getNodeName(),
						a.getNodeValue());
			}
		}

	}

	public static BfxElement createElement(String name, BfxType type) {
		return createElement(name, type, null);
	}

	public static BfxElement createElement(String name, BfxType type,
			BfxElement parent) {

		BfxElement element = null;

		if (parent != null)
			element = new BfxElement(parent) {
			};
		else
			element = new BfxElement() {
			};

		element.setType(type);

		element.setId(name);

		String expr = config.getProperty(type.name().toLowerCase());
		if (expr != null) {
			
			


			StringTokenizer tokenizer = new StringTokenizer(expr, ",')([]");
			while (tokenizer.hasMoreElements()) {
				String string = (String) tokenizer.nextElement();
				element.setConfig(BfxKey.valueOf(string), getResId(string
						+ type.name().toLowerCase()));
			}

		}

		if (isRefRoot(type)) {
			element.setBase(createElement(name, getBaseType(type)));
		}

		return element;

	}

	private static int getResId(String string) {

		Class cls = null;
		int r = 0;

		if (string.startsWith("menu_")) {
			cls = R.menu.class;
		} else if (string.startsWith("layout_")) {
			cls = R.layout.class;
		} else if (string.startsWith("array_")) {
			cls = R.string.class;
		} else {
			cls = R.drawable.class;
		}

		try {
			if (cls != null)
				r = cls.getField(string).getInt(cls.newInstance());
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return r;
	}

	public static boolean isRefRoot(BfxType type) {
		List<BfxType> list = new ArrayList<BfxType>();
		Collections.addAll(list, BfxType.PROCESS, BfxType.FORM, BfxType.ENTITY,
				BfxType.ACTION);

		if (type.toString().toLowerCase().endsWith("ref")) {
			String t = type.name().substring(0, type.name().length() - 3);
			BfxType bt = BfxType.valueOf(t);
			return list.contains(bt);
		}

		return false;
	}

	public static BfxType getBaseType(BfxType type) {
		if (type.toString().toLowerCase().endsWith("ref")) {
			String t = type.name().substring(0, type.name().length() - 3);
			BfxType bt = BfxType.valueOf(t);
			return bt;
		}
		return null;
	}

	private static int getProperty(BfxType type) {

		try {
			Class arrayCls = R.array.class;
			Object arrObj = R.array.class.newInstance();
			Field fld = arrayCls.getField(type.toString().toLowerCase());
			return fld.getInt(arrObj);

		} catch (Exception e) {

		}

		return 0;

	}

	private static int getMenu(BfxType type) {

		try {
			Class menuCls = R.menu.class;
			Object menuObj = R.menu.class.newInstance();
			Field fld = menuCls.getField(type.toString().toLowerCase());
			return fld.getInt(menuObj);

		} catch (Exception e) {

		}
		return 0;
	}

	public static int getMenu(String type) {

		try {
			Class menuCls = R.menu.class;
			Object menuObj = R.menu.class.newInstance();
			Field fld = menuCls.getField(type.toLowerCase());
			return fld.getInt(menuObj);

		} catch (Exception e) {

		}
		return 0;
	}

	private static int getIcon(BfxType type) {

		try {
			Class menuCls = R.drawable.class;
			Object menuObj = R.drawable.class.newInstance();
			Field fld = menuCls.getField(type.toString().toLowerCase());
			return fld.getInt(menuObj);

		} catch (Exception e) {

		}
		return 0;
	}

	private static int getLayout(BfxType type) {

		try {
			Class layCls = R.layout.class;
			Object layObj = R.layout.class.newInstance();
			Field fld = layCls.getField(type.toString().toLowerCase());
			return fld.getInt(layObj);

		} catch (Exception e) {

		}
		return 0;
	}

	public static int getLayout(String type) {

		try {
			Class layCls = R.layout.class;
			Object layObj = R.layout.class.newInstance();
			Field fld = layCls.getField(type.toString().toLowerCase());
			return fld.getInt(layObj);

		} catch (Exception e) {

		}
		return 0;
	}

	public static int getStringArray(String type) {

		try {
			Class arrayCls = R.array.class;
			Object arrObj = R.array.class.newInstance();
			Field fld = arrayCls.getField(type.toString().toLowerCase());
			return fld.getInt(arrObj);

		} catch (Exception e) {

		}

		return 0;
	}

	public static String getOgnlValue(String expr, Object obj) {

		 // Create or retrieve a JexlEngine
        JexlEngine jexl = new JexlEngine();
        // Create an expression object
        String jexlExp = "x.id";
        Expression e = jexl.createExpression( expr );

        // Create a context and add data
        JexlContext jc = new MapContext();
        jc.set("x",obj );

        // Now evaluate the expression, getting the result
        Object o = e.evaluate(jc);

		return (String)o;

	}

	public static List getOgnlList(String expr, Object obj) {
		
		 // Create or retrieve a JexlEngine
        JexlEngine jexl = new JexlEngine();
        // Create an expression object
        String jexlExp = "x.id";
        Expression e = jexl.createExpression( expr );

        // Create a context and add data
        JexlContext jc = new MapContext();
        jc.set("x",obj );

        // Now evaluate the expression, getting the result
        Object o = e.evaluate(jc);

		return (List)o;

	}

	public static BfxType getRefType(BfxType basety) {
		Map<BfxType, BfxType> map = new HashMap<BfxType, BfxType>();
		map.put(BfxType.FORM, BfxType.FORMREF);
		map.put(BfxType.PROCESS, BfxType.PROCESSREF);
		return map.get(basety);
	}

}
