import xmlparse.XMLStruct;

import java.util.ArrayList;
import java.util.HashMap;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class XMLModel
{
    private HashMap<String, PropGroup> pgroups;
    private HashMap<String, DoubleProperty> props;
    private ArrayList<Binder> binders;
    private ArrayList<ConvertBinder> angles;
    private HashMap<Double, DoubleProperty> constProps;
    private HashMap<String, ModelParab> parabs;

    private ModelPane mp;
    private String firstGroup;

    public XMLModel(ModelPane _mp)
    {
        pgroups = new HashMap<String, PropGroup>();
        props = new HashMap<String, DoubleProperty>();
        binders = new ArrayList<Binder>();
        angles = new ArrayList<ConvertBinder>();
        constProps = new HashMap<Double, DoubleProperty>();
        parabs = new HashMap<String, ModelParab>();
        mp = _mp;
        firstGroup = null;
    }

    public XMLModel(ModelPane _mp, String fn)
    {
        this(_mp);
        try
        {
            XMLStruct xml = XMLStruct.parseFromFile(fn, false);
            load(xml);
        }
        catch (XMLStruct.BadSyntaxException e)
        {
            System.out.println("Failed to parse XML.");
            System.out.println(e);
        }

        if (firstGroup != null)
        {
            Face.setShownPropGroup(pgroups.get(firstGroup));
        }
    }

    public void load(XMLStruct xml)
    {
        String pgroup = null;
        if (xml.getName().equals("section"))
        {
            pgroup = xml.getAttribValueFromName("pgroup");
        }

        for (int i = 0; i < xml.getNumChildren(); ++i)
        {
            XMLStruct section = xml.getChildElement(i);
            if (section != null)
            {
                if (section.getName().equals("pgroup"))
                {
                    loadPgroup(section);
                }
                else if (section.getName().equals("section"))
                {
                    load(section);
                }
                else
                {
                    loadShape(section, pgroup);
                }
            }
        }
    }

    private void loadPgroup(XMLStruct xml)
    {
        String groupName = xml.getAttribValueFromName("name");
        if (groupName == null)
            return;
        else if (!pgroups.containsKey(groupName))
        {
            pgroups.put(groupName, new PropGroup(groupName));
            if (firstGroup == null)
                firstGroup = groupName;
        }

        for (int i = 0; i < xml.getNumChildren(); ++i)
        {
            XMLStruct prop = xml.getChildElement(i);
            if (prop != null)
            {
                String type = prop.getName();
                if (type.equals("prop"))
                {
                    loadProp(prop, groupName);
                }
                else if (type.equals("val"))
                {
                    loadVal(prop, groupName);
                }
                else if (type.equals("angleval"))
                {
                    loadAngleVal(prop, groupName);
                }
            }
        }
    }

    private void loadProp(XMLStruct xml, String pgroup)
    {
        String name = xml.getAttribValueFromName("name");

        String minStr = xmlAttribOrElement(xml, "min");
        double min = 0;
        try
        {
            min = interpretVal(minStr);
        }
        catch (BadValueException e)
        {
        }

        String maxStr = xmlAttribOrElement(xml, "max");
        double max = 0;
        try
        {
            max = interpretVal(maxStr);
        }
        catch (BadValueException e)
        {
        }

        String defStr = xmlAttribOrElement(xml, "default");
        double def = (min + max) / 2;
        try
        {
            def = interpretVal(defStr);
        }
        catch (BadValueException e)
        {
        }

        pgroups.get(pgroup).add(name, min, max, def);
        DoubleProperty dp = pgroups.get(pgroup).valueProperty(name);
        props.put(pgroup + "." + name, dp);
    }

    private void loadVal(XMLStruct xml, String pgroup)
    {
        String name = xml.getAttribValueFromName("name");

        String str = xml.getAttribValueFromName("value");
        if (str == null)
        {
            for (int i = 0; i < xml.getNumChildren(); ++i)
            {
                str = xml.getChildString(i).trim();
                if (str != null)
                    break;
            }
        }

        if (str != null)
        {
            try
            {
                DoubleProperty dp = resolveProp(str, pgroup);
                props.put(pgroup + "." + name, dp);
            }
            catch (BadValueException e)
            {
            }
        }
    }

    private void loadAngleVal(XMLStruct xml, String pgroup)
    {
        String name = xml.getAttribValueFromName("name");

        String xStr = xmlAttribOrElement(xml, "x");
        DoubleProperty x = null;
        try
        {
            x = resolveProp(xStr, pgroup);
        }
        catch (BadValueException e)
        {
            return;
        }

        String zStr = xmlAttribOrElement(xml, "z");
        DoubleProperty z = null;
        try
        {
            z = resolveProp(zStr, pgroup);
        }
        catch (BadValueException e)
        {
            return;
        }

        ConvertBinder cb = new ConvertBinder(x, z);
        angles.add(cb);
        props.put(pgroup + "." + name + ".radius", cb.getR());
        props.put(pgroup + "." + name + ".theta", cb.getTheta());
    }

    private void loadShape(XMLStruct xml, String pgroup)
    {
        String type = xml.getName();

        if (xml.getAttribValueFromName("pgroup") != null)
            pgroup = xml.getAttribValueFromName("pgroup");

        String reflect = xml.getAttribValueFromName("reflect");
        if (reflect == null)
            reflect = "false";

        String visible = xml.getAttribValueFromName("visible");
        if (visible == null)
            visible = "true";

        int l = (reflect.equals("both") || reflect.equals("true")) ? 0 : 1;
        int u = (reflect.equals("both") || reflect.equals("false")) ? 1 : 0;

        for (int i = l; i <= u; ++i)
        {
            ModelShape ms = null;

            if (type.equals("sphere"))
                ms = loadSphere(xml, pgroup);
            else if (type.equals("bezier"))
                ms = loadBezier(xml, pgroup);
            else if (type.equals("parab"))
                ms = loadParab(xml, pgroup);

            if (ms != null)
            {
                if (pgroups.containsKey(pgroup))
                    ms.setPropGroup(pgroups.get(pgroup));
                if (i == 0)
                    ms.flip();
                if (visible.equals("true"))
                    mp.add(ms);
            }
        }
    }

    private ModelSphere loadSphere(XMLStruct xml, String pgroup)
    {
        XMLStruct center = xml.getChildElement("center");
        if (center == null)
            return null;
        DoubleProperty x = loadNamedProp(center, "x", pgroup);
        DoubleProperty y = loadNamedProp(center, "y", pgroup);
        DoubleProperty z = loadNamedProp(center, "z", pgroup);

        XMLStruct radius = xml.getChildElement("radius");
        if (radius == null)
            return null;
        DoubleProperty r = loadSingleProp(radius, pgroup);

        ModelSphere ms = new ModelSphere(x, y, z, r);
        return ms;
    }

    private ModelBezier loadBezier(XMLStruct xml, String pgroup)
    {
        int res;
        try
        {
            res = Integer.parseInt(xml.getAttribValueFromName("res"));
        }
        catch (Exception e)
        {
            return null;
        }

        ModelBezier mb = new ModelBezier(res);

        for (int i = 0; i < xml.getNumChildren(); ++i)
        {
            XMLStruct point = xml.getChildElement(i);
            if (point != null && point.getName().equals("point"))
            {
                Coord c = loadCoord(point, pgroup);
                if (c != null)
                {
                    mb.addPoint(c);
                }
            }
        }

        return mb;
    }

    private ModelParab loadParab(XMLStruct xml, String pgroup)
    {
        System.out.println("hey");

        int res;
        try
        {
            res = Integer.parseInt(xml.getAttribValueFromName("res"));
        }
        catch (Exception e)
        {
            return null;
        }

        XMLStruct center = xml.getChildElement("center");
        if (center == null)
            return null;
        DoubleProperty x = loadNamedProp(center, "x", pgroup);
        DoubleProperty y = loadNamedProp(center, "y", pgroup);
        DoubleProperty z = loadNamedProp(center, "z", pgroup);

        XMLStruct width = xml.getChildElement("width");
        if (width == null)
            return null;
        DoubleProperty w = loadSingleProp(width, pgroup);

        XMLStruct length = xml.getChildElement("length");
        if (length == null)
            return null;
        DoubleProperty l = loadSingleProp(length, pgroup);

        XMLStruct angle = xml.getChildElement("angle");
        if (angle == null)
            return null;
        DoubleProperty a = loadSingleProp(angle, pgroup);

        ModelParab mp = new ModelParab(res, x, y, z, w, l, a);

        if (xml.getAttribValueFromName("name") != null)
        {
            parabs.put(xml.getAttribValueFromName("name"), mp);
        }

        return mp;
    }

    private DoubleProperty loadSingleProp(XMLStruct xml, String pgroup)
    {
        String attrib = xml.getAttribValueFromName("value");
        String element = xml.getChildString();
        String str = (element != null) ? element.trim() : attrib;

        if (str != null)
        {
            DoubleProperty dp = null;
            try
            {
                dp = resolveProp(str, pgroup);
            }
            catch (BadValueException e)
            {
            }

            return dp;
        }

        return null;
    }

    private DoubleProperty loadNamedProp(XMLStruct xml, String name, String pgroup)
    {
        String str = xmlAttribOrElement(xml, name);
        if (str != null)
        {
            DoubleProperty dp = null;
            try
            {
                dp = resolveProp(str, pgroup);
            }
            catch (BadValueException e)
            {
            }

            return dp;
        }

        return null;
    }

    private Coord loadCoord(XMLStruct xml, String pgroup)
    {
        boolean cyl = false;
        if (xml.getAttribValueFromName("type") != null &&
            xml.getAttribValueFromName("type").equals("cyl"))
            cyl = true;

        Coord c = null;

        if (!cyl)
        {
            DoubleProperty x = loadNamedProp(xml, "x", pgroup);
            DoubleProperty y = loadNamedProp(xml, "y", pgroup);
            DoubleProperty z = loadNamedProp(xml, "z", pgroup);

            if (x != null && y != null && z != null)
                c = new Coord(x,y,z);
        }
        else
        {
            DoubleProperty r = loadNamedProp(xml, "r", pgroup);
            DoubleProperty theta = loadNamedProp(xml, "theta", pgroup);
            DoubleProperty z = loadNamedProp(xml, "z", pgroup);

            if (r != null && theta != null && z != null)
                c = new CylCoord(r,theta,z);
        }

        return c;
    }

    private static String xmlAttribOrElement(XMLStruct xml, String name)
    {
        String attrib = xml.getAttribValueFromName(name);
        XMLStruct element = xml.getChildElement(name);

        if (element == null || element.getChildString() == null)
        {
            if (attrib != null)
                return attrib.trim();
            else
                return null;
        }
        else
        {
            return element.getChildString().trim();
        }
    }

    private double interpretVal(String str) throws BadValueException
    {
        int[] i = new int[1];
        i[0] = 0;
        return interpretVal(str, i);
    }
    private double interpretVal(String str, int[] i) throws BadValueException
    {
        if (str == null)
            throw new BadValueException(str);

        double val = 0;
        char op = '+';
        boolean first = true;

        while (i[0] < str.length())
        {
            int j = skipWS(str, i[0]);
            i[0] = skipUntil(str, j, WS_OR_OP);

            if (i[0] == j)  // single operator
                ++i[0];

            String found = str.substring(j, i[0]);

            if (op == 0)
            {
                // Get the operation to use
                if (found.length() > 1)
                    throw new BadValueException(str);

                op = found.charAt(0);
            }
            else
            {
                double foundVal = 0;
                if ((op == '+' || op == '-') && !first)
                {
                    i[0] = j;
                    foundVal = interpretVal(str, i);
                }
                else
                {
                    if (found.charAt(0) == '-')
                    {
                        j = i[0];
                        i[0] = skipUntil(str, j, WS_OR_OP);
                        found = "-" + str.substring(j, i[0]);
                    }

                    if (found.equals("PI"))
                        foundVal = Math.PI;
                    else if (found.equals("-PI"))
                        foundVal = -Math.PI;
                    else
                    {
                        try
                        {
                            foundVal = Double.parseDouble(found);
                        }
                        catch (Exception e)
                        {
                            throw new BadValueException(str);
                        }
                    }
                }

                if (op == '+')
                    val += foundVal;
                else if (op == '-')
                    val -= foundVal;
                else if (op == '*')
                    val *= foundVal;
                else if (op == '/')
                    val /= foundVal;
                else
                    throw new BadValueException(str);

                op = 0;
            }

            first = false;
        }

        return val;
    }

    private DoubleProperty resolveProp(String str, String context) throws BadValueException
    {
        int[] i = new int[1];
        i[0] = 0;
        return resolveProp(str, context, i);
    }
    private DoubleProperty resolveProp(String str, String context, int[] i) throws BadValueException
    {
        if (str == null)
            throw new BadValueException(str);

        DoubleProperty prop = null;
        char op = '+';
        String unaryOp = null;
        boolean first = true;

        while (i[0] < str.length())
        {
            int j = skipWS(str, i[0]);
            i[0] = skipUntil(str, j, WS_OR_OP);

            if (i[0] == j)  // single operator
                ++i[0];

            String found = str.substring(j, i[0]);
            System.out.println("found=" + found);

            if (op == 0)
            {
                // Get the operation to use
                if (found.length() > 1)
                    throw new BadValueException(str);

                op = found.charAt(0);
            }
            else
            {
                DoubleProperty foundProp = null;
                double foundVal = 0;
                boolean isConst = false;
                boolean neg = false;
                boolean isUnaryOp = false;

                if ((op == '+' || op == '-') && !first)
                {
                    i[0] = j;
                    foundProp = resolveProp(str, context, i);
                    isConst = false;
                }
                else
                {
                    if (found.equals("-") ||
                        found.equals("cos") ||
                        found.equals("sin") ||
                        found.indexOf("ParabR:") == 0 ||
                        found.indexOf("ParabZ:") == 0)
                    {
                        unaryOp = found;
                        isUnaryOp = true;
                    }
                    else
                    {
                        if (found.equals("PI"))
                        {
                            foundVal = Math.PI;
                            isConst = true;
                        }
                        else
                        {
                            try
                            {
                                foundVal = Double.parseDouble(found);
                                isConst = true;
                            }
                            catch (Exception e)
                            {
                            }
                        }

                        if (!isConst)
                        {
                            try
                            {
                                foundProp = resolveSingleProp(found, context);
                                isConst = false;
                            }
                            catch (Exception e)
                            {
                                throw new BadValueException(str);
                            }
                        }
                        else
                        {
                            if (constProps.containsKey(foundVal))
                                foundProp = constProps.get(foundVal);
                            else
                            {
                                foundProp = new SimpleDoubleProperty(foundVal);
                                constProps.put(foundVal, foundProp);
                            }
                        }
                    }
                }

                if (!isUnaryOp)
                {
                    if (unaryOp != null)
                    {
                        if (unaryOp.equals("-"))
                        {
                            Binder b = new Binder(foundProp.multiply(-1));
                            binders.add(b);
                            foundProp = b.valueProperty();
                        }
                        else if (unaryOp.indexOf("Parab") == 0)
                        {
                            char get = unaryOp.charAt(5);
                            String pname = unaryOp.substring(7);

                            if (!parabs.containsKey(pname))
                                throw new BadValueException(str);

                            ModelParab mp = parabs.get(pname);

                            if (get == 'R')
                                foundProp = mp.getRProp(foundProp);
                            else if (get == 'Z')
                                foundProp = mp.getZProp(foundProp);
                            else
                                throw new BadValueException(str);
                        }
                        unaryOp = null;
                    }

                    if (op == '+' && first)
                        prop = foundProp;
                    else if (op == '+' && !first)
                    {
                        Binder b = new SumBinder(prop, foundProp);
                        binders.add(b);
                        prop = b.valueProperty();
                    }
                    else if (op == '-')
                    {
                        Binder b = new DiffBinder(prop, foundProp);
                        binders.add(b);
                        prop = b.valueProperty();
                    }
                    else if (op == '*' && isConst)
                    {
                        Binder b = new Binder(prop.multiply(foundVal));
                        binders.add(b);
                        prop = b.valueProperty();
                    }
                    else if (op == '/' && isConst)
                    {
                        Binder b = new Binder(prop.divide(foundVal));
                        binders.add(b);
                        prop = b.valueProperty();
                    }
                    else
                        throw new BadValueException(str);

                    op = 0;

                    first = false;
                }
            }
        }

        return prop;
    }

    private DoubleProperty resolveSingleProp(String str, String context) throws BadValueException
    {
        if (str == null)
            throw new BadValueException("");

        if (context == null)
            context = "";

        if (str.indexOf(".") == 0)
            throw new BadValueException(str);
        else if (str.indexOf(".") < 0 && context != null)
            str = context + "." + str;

        if (!props.containsKey(str))
        {
            if (context == null)
                throw new BadValueException(str);
            str = context + "." + str;
            if (!props.containsKey(str))
                throw new BadValueException(str);
        }

        return props.get(str);
    }

    private static int skipWS(String str, int i)
    {
        for (; i < str.length(); ++i)
        {
            char c = str.charAt(i);

            switch (c)
            {
                case ' ':
                case '\n':
                case '\r':
                case '\t':
                    break;
                default:
                    return i;
            }
        }

        return i;
    }

    private static int skipUntil(String str, int i, char[] look)
    {
        if (look == null)
            return str.length();

        for (; i < str.length(); ++i)
        {
            char c = str.charAt(i);

            for (int j = 0; j < look.length; ++j)
            {
                if (c == look[j])
                    return i;
            }
        }

        return i;
    }

    private static class BadValueException extends Exception
    {
        public BadValueException(String val)
        {
            super(val);
        }
    }

    private static char[] WS_OR_OP = {' ', '\n', '\r', '\t',
                                      '+', '-', '*', '/'};
}
