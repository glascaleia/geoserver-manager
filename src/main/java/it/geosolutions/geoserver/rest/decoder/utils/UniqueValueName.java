package it.geosolutions.geoserver.rest.decoder.utils;

import org.jdom.Element;

/**
 * Created by fizzi on 11/11/16.
 */
public class UniqueValueName  extends  NameLinkElem{
    private final Element elem;

    public UniqueValueName(Element elem) {
        super(elem);
        this.elem = elem;
    }

    public String getName() {
        return elem.getChildText("string");
    }

}
