//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.09.06 at 02:11:11 PM CEST 
//


package org.lsc.plugins.connectors.obm.generated;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.lsc.plugins.connectors.obm.generated package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.lsc.plugins.connectors.obm.generated
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ObmGroupService }
     * 
     */
    public ObmGroupService createObmGroupService() {
        return new ObmGroupService();
    }

    /**
     * Create an instance of {@link ObmUserService }
     * 
     */
    public ObmUserService createObmUserService() {
        return new ObmUserService();
    }
}
