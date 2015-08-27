package org.daisy.pipeline.client.models.script;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.daisy.pipeline.client.Pipeline2Client;
import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.models.script.Argument;
import org.daisy.pipeline.client.persistence.Context;
import org.daisy.pipeline.client.utils.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** An argument of type "string" */
public class Argument {
	
	/** The name of the option. This isn't necessarily unique; since inputs and options can have the same name. */
	public String name;
	
	/** This is the value from the px:role="name" in the script documentation. */
	public String niceName;
	
	/** A description of the option. */
	public String desc;
	
	/** whether or not this option is required */
	public Boolean required;
	
	/** whether or not multiple selections can be made */
	public Boolean sequence;
	
	/** MIME types accepted (only relevant if type=anyDirURI or anyFileURI) */
	public List<String> mediaTypes;
	
	/** Options with a output value of "result" or "temp" will only be included when the framework is running in local mode. */
	public Output output;
	public enum Output { result, temp };
	
	/** Type of underlying option. Either "input", "option" or "output". ("parameters" currently not supported) */
	public Kind kind;
	public enum Kind { input, /*parameters,*/ option, output };
	
	/** whether or not the ordering matters (only relevant if sequence==true) */
	public Boolean ordered;
	
	/** XSD type */
	public String type;
    
	private List<String> values = null;
	
	Context context;
    
	/** Create option instance from option node. */
	public Argument(Node optionNode) throws Pipeline2Exception {
		this.name = parseTypeString(XPath.selectText("@name", optionNode, Pipeline2Client.ns));
		
		this.niceName = parseTypeString(XPath.selectText("@nicename", optionNode, Pipeline2Client.ns));
		if (this.niceName == null || "".equals(this.niceName))
			this.niceName = this.name;
		
		this.desc = parseTypeString(XPath.selectText("@desc", optionNode, Pipeline2Client.ns));
		if (this.desc == null)
			this.desc = "";
		
		this.required = parseTypeBoolean(XPath.selectText("@required", optionNode, Pipeline2Client.ns));
		if (this.required == null)
			this.required = true;
		
		this.sequence = parseTypeBoolean(XPath.selectText("@sequence", optionNode, Pipeline2Client.ns));
		if (this.sequence == null)
			this.sequence = false;
		
		this.mediaTypes = parseTypeMediaTypes(XPath.selectText("@mediaType", optionNode, Pipeline2Client.ns));

		try {
			this.output = Output.valueOf(parseTypeString(XPath.selectText("@outputType", optionNode, Pipeline2Client.ns)));
		} catch (IllegalArgumentException e) {
		} catch (NullPointerException e) {
		} finally {
			this.output = null;
		}

		try {
			this.kind = Kind.valueOf(optionNode.getLocalName()); // TODO "parameters": how to determine that a port is a parameter port?
		} catch (IllegalArgumentException e) {
		} catch (NullPointerException e) {
		} finally {
			this.kind = null;
		}

		this.ordered = parseTypeBoolean(XPath.selectText("@ordered", optionNode, Pipeline2Client.ns));
		if (this.ordered == null)
			this.ordered = true;

		this.type = parseTypeString(XPath.selectText("@type", optionNode, Pipeline2Client.ns));
		if (this.type == null)
			this.type = "string";
		
		
		if (this.kind == Kind.input || this.kind == Kind.output) {
			this.type = "anyFileURI";
			
			if (this.mediaTypes.size() == 0)
				this.mediaTypes.add("application/xml");
		}
		
		if ("output".equals(this.kind)) {
			this.required = false;
			if (this.output == null)
				this.output = Output.result;
		}
	}
	
	/** Helper function for the Script(Document) constructor */
	private static String parseTypeString(String string) {
		if (!(string instanceof String))
			return null;
		string = string.replaceAll("\"", "'").replaceAll("\\n", " ");
		if ("".equals(string)) return null;
		else return string;
	}
	
	/** Helper function for the Script(Document) constructor */
	private static Boolean parseTypeBoolean(String bool) {
		if (!(bool instanceof String))
			return null;
		if ("false".equals(bool))
			return false;
		if ("true".equals(bool))
			return true;
		return null;
	}
	
	/** Helper function for the Script(Document) constructor */
	private static List<String> parseTypeMediaTypes(String mediaTypesString) {
		if (!(mediaTypesString instanceof String))
			return new ArrayList<String>();
		mediaTypesString = parseTypeString(mediaTypesString);
		String[] mediaTypes = (mediaTypesString==null?"":mediaTypesString).split(" ");
		List<String> mediaTypesList = new ArrayList<String>();
		for (String mediaType : mediaTypes) {
			if ("".equals(mediaType))
				continue;
			
			if ("text/xml".equals(mediaType))
				mediaTypesList.add("application/xml");
			else
				mediaTypesList.add(mediaType);
		}
		return mediaTypesList;
	}
	
	/**
     * Returns an XML Element representation of the option, input or output, compatible with a jobRequest document.
     * 
     * Examples:
     * 
     * <d:option name="language">
     *     <d:item value="en"/>
     * </d:option>
     * 
     * <d:option name="colors">
     *     <d:item value="red"/>
     *     <d:item value="green"/>
     *     <d:item value="blue"/>
     * </d:option>
     * 
     * <d:option name="include-illustrations">
     * 	   <d:item value="true"/>
     * </d:option>
     * 
     * <d:option name="stylesheet">
     * 	   <d:item value="main.css"/>
     * </d:option>
     * 
     * <d:input name="source">
     *     <d:item value="content.xhtml"/>
     * </d:input>
     * 
     * @param document The document used to create the element.
     * @return
     */
    public Element asDocumentElement(Document document) {
    	if (values.size() == 0)
    		return null;

    	Element element = document.createElement(kind == null ? "option" : kind.toString());
    	element.setAttribute("name", name);

        if (Boolean.FALSE.equals(required) && values.size() == 0) {
        	return null;
        	
        } else {
	        for (String value : values) {
	            Element item = document.createElement("item");
	            item.setAttribute("value", value);
	            element.appendChild(item);
	        }
        }
        
        return element;
    }
    
    /**
     * Populates the argument with the values from the provided document element.
     * This is the inverse of the "Element asDocumentElement(Document)" method.
     * 
     * @param option
     * @return
     * @throws Pipeline2Exception
     */
    public void parseFromJobRequest(Node jobRequest) throws Pipeline2Exception {
    	values.clear();
    	List<Node> items = XPath.selectNodes("/*/*[@name='"+name+"']/d:item", jobRequest, Pipeline2Client.ns);
    	for (Node item : items) {
	        String href = XPath.selectText("@value", item, Pipeline2Client.ns);
	        add(href);
    	}
    	if (size() == 0) {
    		String value = XPath.selectText("/*/*[@name='"+name+"']", jobRequest, Pipeline2Client.ns);
    		set(value);
    	}
    }

    /**
     * Returns the number of values defined for the option or input.
     * 
     * @param name the name of the option or input
     * @return number of values
     */
    public int size() {
		lazyLoad();
		if (values == null) {
			return 0;

		} else {
			return values.size();
		}
    }
    
	/**
	 * Unset the given option or input.
	 * 
	 * This is different from clearing the option in that it will no longer be defined.
	 * 
	 * An option that is cleared but not unset is submitted as an empty list of
	 * values to the Web API. An option that is unset are not submitted to the Web API,
	 * which leaves the Web API or the Pipeline 2 script free to use a default value.
	 */
	public void unset() {
		lazyLoad();
		values.clear();
		values = null;
	}
	
	/**
	 * Unset the given option or input.
	 * 
	 * This is different from clearing the option in that it will no longer be defined.
	 * 
	 * An option that is cleared but not unset is submitted as an empty list of
	 * values to the Web API. An option that is unset are not submitted to the Web API,
	 * which leaves the Web API or the Pipeline 2 script free to use a default value.
	 * 
	 * @return True if the argument is defined/set. False otherwise.
	 */
	public boolean isDefined() {
		return values != null;
	}
	
	/**
	 * Clear the given option or input.
	 * 
	 * This is different from unsetting the option in that it will still be defined.
	 * 
	 * An option that is cleared but not unset is submitted as an empty list of
	 * values to the Web API. An option that is unset are not submitted to the Web API,
	 * which leaves the Web API or the Pipeline 2 script free to use a default value.
	 */
	public void clear() {
		lazyLoad();
		if (values == null) {
			values = new ArrayList<String>();
		} else {
			values.clear();
		}
	}
	
	private void lazyLoad() {
		// TODO
	}
	
	/** Replace the value at the given position with the provided Integer value.
	 *  @param position
	 *  @param value the value to use */
	public void set(int position, Integer value) {
		if (value == null) {
			clear();
		} else {
			set(position, value+"");
		}
	}
	
	/** Replace the value at the given position with the provided Long value.
	 *  @param position
	 *  @param value the value to use */
	public void set(int position, Long value) {
		if (value == null) {
			clear();
		} else {
			set(position, value+"");
		}
	}
	
	/** Replace the value at the given position with the provided Double value.
	 *  @param position
	 *  @param value the value to use */
	public void set(int position, Double value) {
		if (value == null) {
			clear();
		} else {
			set(position, value+"");
		}
	}
	
	/** Replace the value at the given position with the provided Boolean value.
	 *  @param position
	 *  @param value the value to use */
	public void set(int position, Boolean value) {
		if (value == null) {
			clear();
		} else {
			set(position, value+"");
		}
	}
	
	/** Replace the value at the given position with the provided File value.
	 *  @param position
	 *  @param file the value to use */
	public void set(int position, File file) {
		if (file == null) {
			clear();
		} else {
			context.addFile(file, file.getName());
			set(position, context.getPath(file));
		}
	}
	
	/** Replace the value at the given position with the provided String value.
	 *  @param position
	 *  @param value the value to use */
	public void set(int position, String value) {
		if (value == null) {
			clear();
		} else {
			lazyLoad();
			if (values != null && values.size() > position) {
				values.set(position, value);
			}
		}
	}

	/** Replace the value with the provided Integer value.
	 *  @param value the value to use */
	public void set(Integer value) {
		if (value == null) {
			clear();
		} else {
			set(value+"");
		}
	}
	
	/** Replace the value with the provided Long value.
	 *  @param value the value to use */
	public void set(Long value) {
		if (value == null) {
			clear();
		} else {
			set(value+"");
		}
	}
	
	/** Replace the value with the provided Double value.
	 *  @param value the value to use */
	public void set(Double value) {
		if (value == null) {
			clear();
		} else {
			set(value+"");
		}
	}
	
	/** Replace the value with the provided Boolean value.
	 *  @param value the value to use */
	public void set(Boolean value) {
		if (value == null) {
			clear();
		} else {
			set(value+"");
		}
	}
	
	/** Replace the value with the provided File value.
	 *  @param file the value to use */
	public void set(File file) {
		if (file == null) {
			clear();
		} else {
			context.addFile(file, file.getName());
			set(context.getPath(file));
		}
	}
	
//  public void set(String value) {
//  href = value == null ? null : value.toString();
//  if (System.getProperty("os.name").startsWith("Windows") && href != null && href.startsWith("file:") && href.contains("~")) {
//      try {
//          href = new File(new File(new URI(href)).getCanonicalPath()).toURI().toURL().toString();
//      } catch (MalformedURLException e) {
//          // ignore
//      } catch (IOException e) {
//          // ignore
//      } catch (URISyntaxException e) {
//          // ignore
//      }
//  }
//}
	
	/** Replace the value with the provided String value.
	 *  @param value the value to use */
	public void set(String value) {
		clear();
		if (value != null) {
			values.add(value);
		}
	}
	
	/** Replace the values with all the provided String values.
	 *  @param values the value to use */
	public void setAll(Collection<String> values) {
		clear();
		if (values != null) {
			values.addAll(values);
		}
	}

	/** Add to the list of values the provided Integer value.
	 *  @param value the value to use */
	public void add(Integer value) {
		if (value != null) {
			add(value+"");
		}
	}
	
	/** Add to the list of values the provided Long value.
	 *  @param value the value to use */
	public void add(Long value) {
		if (value != null) {
			add(value+"");
		}
	}
	
	/** Add to the list of values the provided Double value.
	 *  @param value the value to use */
	public void add(Double value) {
		if (value != null) {
			add(value+"");
		}
	}
	
	/** Add to the list of values the provided Boolean value.
	 *  @param value the value to use */
	public void add(Boolean value) {
		if (value != null) {
			add(value+"");
		}
	}
	
	/** Add to the list of values the provided File value.
	 *  @param file the value to use */
	public void add(File file) {
		if (file != null) {
			lazyLoad();
			context.addFile(file, file.getName());
			add(context.getPath(file));
		}
	}
	
	/** Add to the list of values the provided String value.
	 *  @param value the value to use */
	public void add(String value) {
		if (value != null) {
			lazyLoad();
			values.add(value);
		}
	}
	
	/** Add to the list of values all the provided String values.
	 *  @param values the value to use */
	public void addAll(Collection<String> values) {
		if (values != null) {
			values.addAll(values);
		}
	}
	
	/** Remove all occurences of the provided Integer value from the list of values.
	 *  @param value the value to use */
	public void remove(Integer value) {
		if (value != null) {
			remove(value+"");
		}
	}
	
	/** Remove all occurences of the provided Long value from the list of values.
	 *  @param value the value to use */
	public void remove(Long value) {
		if (value != null) {
			remove(value+"");
		}
	}
	
	/** Remove all occurences of the provided Double value from the list of values.
	 *  @param value the value to use */
	public void remove(Double value) {
		if (value != null) {
			remove(value+"");
		}
	}
	
	/** Remove all occurences of the provided Boolean value from the list of values.
	 *  @param value the value to use */
	public void remove(Boolean value) {
		if (value != null) {
			remove(value+"");
		}
	}
	
	/** Remove all occurences of the provided File value from the list of values.
	 *  @param file the value to use */
	public void remove(File file) {
		if (file != null) {
			remove(context.getPath(file));
		}
	}
	
	/** Remove all occurences of the provided String value from the list of values.
	 *  @param value the value to use */
	public void remove(String value) {
		if (value != null && values != null) {
			for (int i = values.size() - 1; i >= 0 ; i--) {
				if (value.equals(values.get(i))) {
					values.remove(i);
				}
			}
		}
	}
	
	/** Remove the first occurences of all the provided String values from the list of values.
	 *  @param values the value to use */
	public void removeAll(Collection<String> values) {
		if (values != null) {
			values.removeAll(values);
		}
	}

	/** Get the value as a Integer.
	 *
	 *  Returns the first value if there are more than one.
	 *  Returns null if the value cannot be parsed as a Integer, or if the value is not set.
	 *  If the option or input is a sequence, you should use {@link #getAsIntegerList() getAsIntegerList} to get all values instead.
	 *  
	 *  @return the value as a Integer
	 */
	public Integer getAsInteger() {
		lazyLoad();
		try {
			return Integer.parseInt(get());

		} catch (Exception e) {
			return null;
		}
	}
	
	/** Get the value as a Long.
	 *
	 *  Returns the first value if there are more than one.
	 *  Returns null if the value cannot be parsed as a Long, or if the value is not set.
	 *  
	 *  @return the value as a Long
	 */
	public Long getAsLong() {
		lazyLoad();
		try {
			return Long.parseLong(get());

		} catch (Exception e) {
			return null;
		}
	}
	
	/** Get the value as a Double.
	 *
	 *  Returns the first value if there are more than one.
	 *  Returns null if the value cannot be parsed as a Double, or if the value is not set.
	 *  If the option or input is a sequence, you should use {@link #getAsDoubleList(defaultValue) getAsDoubleList} to get all values instead.
	 *  
	 *  @return the value as a Double
	 */
	public Double getAsDouble() {
		lazyLoad();
		try {
			return Double.parseDouble(get());

		} catch (Exception e) {
			return null;
		}
	}
	
	/** Get the value as a Boolean.
	 *
	 *  Returns the first value if there are more than one.
	 *  Returns null if the value cannot be parsed as a Boolean, or if the value is not set.
	 *  If the option or input is a sequence, you should use {@link #getAsBooleanList() getAsBooleanList} to get all values instead.
	 *  
	 *  @return the value as a Boolean
	 */
	public Boolean getAsBoolean() {
		lazyLoad();
		String value = get();
		if (value != null && ("true".equals(value.toLowerCase()) || "false".equals(value.toLowerCase()))) {
			return Boolean.parseBoolean(get());

		} else {
			return null;
		}
	}
	
	/** Get the value as a File.
	 * 
	 *  Returns the first value if there are more than one.
	 *  Returns null if the value cannot be parsed as a File, or if the value is not set.
	 *  
	 *  @return the value as a File
	 */
	public File getAsFile() {
		lazyLoad();
		if (values == null || values.size() == 0) {
			return null;
		} else {
			return context.getFile(values.get(0));
		}
	}
	
	/** Get the value as a String.
	 * 
	 *  Returns the first value if there are more than one.
	 *  Returns null if the value is not set.
	 *  If the option or input is a sequence, you should use {@link #getAsList() getAsList} to get all values instead.
	 *  
	 * @return the value as a String
	 */
	public String get() {
		lazyLoad();
		if (values == null || values.size() == 0) {
			return null;
		} else {
			return values.get(0);
		}
	}

	/** Get all the values as a List of Strings.
	 * 
	 * @return null if the value is not set. */
	public List<String> getAsList() {
		lazyLoad();
		return values;
	}
	
	/** Get all the values as a List of Files.
	 * 
	 *  @return null if any of the values cannot be parsed as a File, or if the value is not set. */
	public List<File> getAsFileList() {
		lazyLoad();
		if (values != null) {
			List<File> contextFiles = new ArrayList<File>();
			assert(contextFiles != null);
			for (String value : values) {
				File contextFile = context.getFile(value);
				contextFiles.add(contextFile);
			}
			return contextFiles;

		} else {
			return null;
		}
	}
	
	/**
	 * Move a value from one position in the value list to another.
	 * 
	 * @param from which value to move
	 * @param to which position to move the value
	 */
	public void moveTo(int from, int to) {
		if (from < 0 || from >= values.size()) {
			return;
		}
		if (to < 0 || to >= values.size()) {
			return;
		}
		int shiftDistance = -1;
		if (from > to) {
			int rememberMe = from;
			from = to;
			to = rememberMe;
			shiftDistance = 1;
		}
		Collections.rotate(values.subList(from, to+1), shiftDistance);
	}

}
